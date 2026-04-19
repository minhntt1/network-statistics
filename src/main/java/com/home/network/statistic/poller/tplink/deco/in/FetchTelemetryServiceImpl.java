package com.home.network.statistic.poller.tplink.deco.in;

import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.authentication.AuthDataRepo;
import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoRepo;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpResponse;
import java.nio.channels.ClosedChannelException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class FetchTelemetryServiceImpl implements FetchTelemetryService {
    private final ExecutorService threadPool;
    private final HttpClient httpClient;
    private final ClientDeviceInfoRepo clientDeviceInfoRepo;
    private final DeviceInfoRepo deviceInfoRepo;
    private final AuthDataRepo authDataRepo;

    public FetchTelemetryServiceImpl(
            @Qualifier("virtualThreadPool") ExecutorService threadPool,
            HttpClient httpClient,
            ClientDeviceInfoRepo clientDeviceInfoRepo,
            DeviceInfoRepo deviceInfoRepo, AuthDataRepo authDataRepo) {
        this.threadPool = threadPool;
        this.httpClient = httpClient;
        this.clientDeviceInfoRepo = clientDeviceInfoRepo;
        this.deviceInfoRepo = deviceInfoRepo;
        this.authDataRepo = authDataRepo;
    }

    public void handleConnectionException(Exception e) {
        log.error("error", e);
        // when connection to router timeout
        // -> consider it down and put empty record into db to detect connect/disconnect status later
        if (e instanceof HttpConnectTimeoutException) {
            log.error("ap connection error");
            insertEmptyRecordsToDb();
        }
    }

    // insert empty records to db to improve disconnection/reboot detection,
    // because if device down at timestamp 2, data fetch from time 1 to 3
    // empty data fetch from list indicate device is down at time 2
    // if theres no empty data -> fetch list has 1,3 data, no empty record -> can't determine disconnect/reboot time
    public void insertEmptyRecordsToDb() {
        clientDeviceInfoRepo.save(ClientDeviceInfoRaw.initEmptyRecord().toClientDeviceInfoEntity());
        deviceInfoRepo.save(DeviceInfoRaw.toEmptyDeviceInfoEntity());
    }

    @SneakyThrows
    public WebResponse getClientByDeviceMac(String mac, WebRequestExtra webRequestExtra) {
        return
            WebResponseEncrypted.from(
                    httpClient.send(webRequestExtra.extRequestFindClientList(mac), HttpResponse.BodyHandlers.ofString()).body())
                        .toJsonDecryptAES(
                            webRequestExtra.getWebEncryptor());
    }

    @SneakyThrows
    public WebResponse getWlanInfo(WebRequestExtra webRequestExtra) {
        return
                WebResponseEncrypted.from(
                        httpClient.send(webRequestExtra.extRequestFindWlan(), HttpResponse.BodyHandlers.ofString()).body())
                        .toJsonDecryptAES(webRequestExtra.getWebEncryptor());
    }

    public Future<WebResponse> getFutureWlanInfo(WebRequestExtra webRequestExtra) {
        return threadPool.submit(() -> getWlanInfo(webRequestExtra));
    }

    public Future<WebResponse> getFutureClientByDeviceMac(WebRequestExtra webRequestExtra, String mac) {
        return threadPool.submit(() -> this.getClientByDeviceMac(mac, webRequestExtra));
    }

    public WebRequestExtra initRequestMetadata(WebUiCredentials webUiCredentials) throws Exception {
        log.info("doing authentication");

        WebRequestExtra webRequestExtra = new WebRequestExtra(webUiCredentials);

        // get rsa keys for pass encryption
        Future<WebResponse> respAuthKeys =
            threadPool.submit(() -> WebResponse.from(
                httpClient.send(
                    webRequestExtra.extRequestAuthKeys(),
                    HttpResponse.BodyHandlers.ofString()).body()
            ));

        // get rsa keys for data encryption
        Future<WebResponse> respDataKeys =
            threadPool.submit(() -> WebResponse.from(
                httpClient.send(
                        webRequestExtra.extRequestDataKeys(),
                        HttpResponse.BodyHandlers.ofString()).body()
                ));

        // if one of credentials contains error, break
        if (respAuthKeys.get().hasError() || respDataKeys.get().hasError()) {
            return null;
        }

        // init a complete encryptor
        webRequestExtra.initWebEncryptorKeys(respAuthKeys.get(), respDataKeys.get());

        // login to get stok and auth header
        var respLogin =
            httpClient.send(
                    webRequestExtra.extRequestLogin(),
                    HttpResponse.BodyHandlers.ofString());
        var loginBody = WebResponseEncrypted.from(respLogin.body()).toJsonDecryptAES(webRequestExtra);

        if (loginBody.hasError()) {
            return null;
        }

        webRequestExtra.setStok(loginBody.getStok());
        webRequestExtra.putCookieHeader(respLogin.headers());

        return webRequestExtra;
    }

    @Override
    @Transactional(value = "appJpaTx")
    public void fetchClientAndWlanInfo() {
        fetchBulkAndProcess(log, true, WebUiCredentials.class);
    }

    @Override
    public void process(AuthData authData) {
        // fetch login info if there is no param / temp auth data
        var cred = authData.extractCredentialAbstract(WebUiCredentials.class);
        WebRequestExtra webRequestExtra;

        if (!authData.checkTempData(WebRequestExtra.class)) {
            try {
                if ((webRequestExtra = initRequestMetadata(cred)) == null) {
                    log.info("error while init security credentials");
                    return;
                }
            } catch (Exception e){
                handleConnectionException(e);
                return;
            }

            // update to db because temp data is changed
            webRequestExtra.updateAuthEntity(authData);
        } else {
            webRequestExtra = WebRequestExtra.fromJson(authData.getTempData());
        }

        // get device list
        WebResponseExtra deviceList;

        try {
            deviceList = new WebResponseExtra(httpClient.send(webRequestExtra.extRequestFindDevices(), HttpResponse.BodyHandlers.ofString()));
        } catch (Exception e) {
            handleConnectionException(e);
            return;
        }

        // at this step, if there are token and k,v, but still have error in request
        // have to reauth again, and update it to db
        if (deviceList.hasErrorBody()) {
            try {
                if ((webRequestExtra = initRequestMetadata(cred)) == null) {
                    log.info("failure during reauth");
                    return;
                }
            } catch (Exception e) {
                handleConnectionException(e);
                return;
            }

            // update to db because temp data is changed
            webRequestExtra.updateAuthEntity(authData);
            return;
        }

        var bodyDeviceList = deviceList.toWebResponseEncrypted().toJsonDecryptAES(webRequestExtra);
        var deviceMac = bodyDeviceList.getDeviceMacs();

        // because web request is slow, have to request in parallel
        // map device mac with respective client list
        var pollTime = LocalDateTime.now();
        var mapDeviceToClient = new HashMap<String, Future<WebResponse>>();
        var futureWlanInfo = this.getFutureWlanInfo(webRequestExtra);

        for (var mac : deviceMac) {
            mapDeviceToClient.put(mac, this.getFutureClientByDeviceMac(webRequestExtra, mac));
        }

        // after request done, map it to output object to process later in db
        var deviceInfoRaws = bodyDeviceList.extractDeviceInfoRaws(pollTime);
        ClientDeviceInfoRaw clientDeviceInfoRaw = null;

        try {
            var wlanInfoRaw = futureWlanInfo.get().extractWlanInfoRaws();
            clientDeviceInfoRaw = new ClientDeviceInfoRaw(pollTime, wlanInfoRaw);

            for (var mac : deviceMac) {
                var clientList = mapDeviceToClient.get(mac).get().extractClientInfoRaws();
                clientDeviceInfoRaw.putToMapDeviceToClient(mac, clientList);
            }
        } catch (Exception e) {
            handleConnectionException(e);
            return;
        }

        // 2 entities: clientdevice, device
        var deviceInfoEntity = DeviceInfoRaw.toDeviceInfoEntity(deviceInfoRaws);
        var clientDeviceInfoEntity = clientDeviceInfoRaw.toClientDeviceInfoEntity();

        // save to db
        deviceInfoRepo.save(deviceInfoEntity);
        clientDeviceInfoRepo.save(clientDeviceInfoEntity);
    }

    @Override
    public AuthDataRepo extractAuthDataRepo() {
        return authDataRepo;
    }
}
