package com.home.network.statistic.poller.tplink.deco.in;

import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoRepo;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.channels.ClosedChannelException;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    // todo: impl way to securely to credentials
    private final WebUiCredentials webUiCredentials = WebUiCredentials.createDecoCredentials("Minhtuan10", "tplinkdeco.net");
    private WebRequestExtra webRequestExtra;

    public FetchTelemetryServiceImpl(
            @Qualifier("virtualThreadPool") ExecutorService threadPool,
            HttpClient httpClient,
            ClientDeviceInfoRepo clientDeviceInfoRepo,
            DeviceInfoRepo deviceInfoRepo) {
        this.threadPool = threadPool;
        this.httpClient = httpClient;
        this.clientDeviceInfoRepo = clientDeviceInfoRepo;
        this.deviceInfoRepo = deviceInfoRepo;
    }

    public void handleConnectionException(Exception e) {
        log.error("error", e);
        // when connection to router closed
        // -> consider it down and put empty record into db to detect connect/disconnect status later
        if (e instanceof ClosedChannelException) {
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
    public WebResponse getClientByDeviceMac(String mac) {
        return
            WebResponseEncrypted.from(
                    httpClient.send(webRequestExtra.getRequestFindClientList(mac), HttpResponse.BodyHandlers.ofString()).body())
                        .toJsonDecryptAES(
                            webRequestExtra.getWebEncryptor());
    }

    @SneakyThrows
    public WebResponse getWlanInfo() {
        return
                WebResponseEncrypted.from(
                        httpClient.send(webRequestExtra.getRequestFindWlan(), HttpResponse.BodyHandlers.ofString()).body())
                        .toJsonDecryptAES(webRequestExtra.getWebEncryptor());
    }

    public boolean initRequestMetadata() throws Exception {
        log.info("doing authentication");

        webRequestExtra = new WebRequestExtra();

        // create temp web encryptor
        WebEncryptor encryptor = new WebEncryptor();
        encryptor.initCredentials(webUiCredentials);
        webRequestExtra.setWebEncryptor(encryptor);

        // get rsa keys for pass encryption
        Future<WebResponse> respAuthKeys =
            threadPool.submit(() -> WebResponse.from(
                httpClient.send(
                    webRequestExtra.getRequestAuthKeys(),
                    HttpResponse.BodyHandlers.ofString()).body()
            ));

        // get rsa keys for data encryption
        Future<WebResponse> respDataKeys =
            threadPool.submit(() -> WebResponse.from(
                httpClient.send(
                        webRequestExtra.getRequestDataKeys(),
                        HttpResponse.BodyHandlers.ofString()).body()
                ));

        // if one of credentials contains error, break
        if (respAuthKeys.get().hasError() ||
            respDataKeys.get().hasError()) {
            return false;
        }

        // init a complete encryptor
        encryptor.init(respAuthKeys.get(), respDataKeys.get());

        // login to get stok and auth header
        var respLogin =
            httpClient.send(
                    webRequestExtra.getRequestLogin(),
                    HttpResponse.BodyHandlers.ofString());
        var loginBody = WebResponseEncrypted.from(respLogin.body()).toJsonDecryptAES(encryptor);

        if (loginBody.hasError()) {
            return false;
        }

        webRequestExtra.setStok(loginBody.getStok());
        webRequestExtra.putCookieHeader(respLogin.headers());

        return true;
    }

    @Override
    public void fetchClientAndWlanInfo() {
        log.info("start");

        // fetch login info if there is no param
        if (webRequestExtra == null) {
            try {
                if (!initRequestMetadata()) {
                    log.info("error while init security credentials");
                    return;
                }
            } catch (Exception e){
                handleConnectionException(e);
                return;
            }
        }

        // get device list
        WebResponseExtra deviceList;

        try {
            deviceList = new WebResponseExtra(httpClient.send(webRequestExtra.getRequestFindDevices(), HttpResponse.BodyHandlers.ofString()));
        } catch (Exception e) {
            handleConnectionException(e);
            return;
        }

        // at this step, if there are token and k,v, but still have error in request
        // have to reauth again
        // handle case when body string is empty
        if (deviceList.hasErrorStringStatus()) {
            try {
                if (!initRequestMetadata()) {
                    log.info("failure during reauth");
                    return;
                }
            } catch (Exception e) {
                handleConnectionException(e);
                return;
            }
        }

        var bodyDeviceList = WebResponseEncrypted.from(deviceList.getStringBody()).toJsonDecryptAES(webRequestExtra.getWebEncryptor());
        var deviceMac = bodyDeviceList.getDeviceMacs();

        // because web request is slow, have to request in parallel
        // map device mac with respective client list
        var pollTime = LocalDateTime.now();
        var mapDeviceToClient = new HashMap<String, Future<WebResponse>>();
        var futureWlanInfo = threadPool.submit(this::getWlanInfo);

        for (var mac : deviceMac)
            mapDeviceToClient.put(mac, threadPool.submit(() -> this.getClientByDeviceMac(mac)));

        // after request done, map it to output object to process later in db
        var deviceInfoRaws = bodyDeviceList.extractDeviceInfoRaws(pollTime);
        ClientDeviceInfoRaw clientDeviceInfoRaw = null;

        try {
            var wlanInfoRaw = futureWlanInfo.get().extractWlanInfoRaws();
            clientDeviceInfoRaw = new ClientDeviceInfoRaw();
            clientDeviceInfoRaw.setPollTime(pollTime);
            clientDeviceInfoRaw.setWlanInfoRaw(wlanInfoRaw);

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

        log.info("end");
    }
}
