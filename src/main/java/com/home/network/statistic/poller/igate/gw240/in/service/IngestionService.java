package com.home.network.statistic.poller.igate.gw240.in.service;

import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.authentication.AuthDataProcessor;
import com.home.network.statistic.poller.authentication.AuthDataRepo;
import com.home.network.statistic.poller.igate.gw240.in.WebRequestInfo;
import com.home.network.statistic.poller.igate.gw240.in.WebResponse;
import com.home.network.statistic.poller.igate.gw240.in.IngestionCredentials;
import com.home.network.statistic.poller.igate.gw240.out.StatusWifiStationRepo;
import com.home.network.statistic.poller.rfc1213.in.SnmpIfTableResponse;
import com.home.network.statistic.poller.rfc1213.in.SnmpTarget;
import com.home.network.statistic.poller.rfc1213.in.SnmpIfTablePhyInfoRequest;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.Snmp;
import org.snmp4j.util.TableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class IngestionService implements AuthDataProcessor<IngestionCredentials> {
    private final Snmp snmp;
    private final RestClient restClient;
    private final WebAuthService webAuthService;
    private final ExecutorService virtualThreadPool;
    private final StatusWifiStationRepo statusWifiStationRepo;
    private final AuthDataRepo authDataRepo;

    @Autowired
    public IngestionService(
            @Qualifier("virtualThreadPool") ExecutorService executorService,
            Snmp snmp,
            RestClient restClient,
            WebAuthService webAuthService,
            StatusWifiStationRepo statusWifiStationRepo, AuthDataRepo authDataRepo) {
        this.snmp = snmp;
        this.restClient = restClient;
        this.webAuthService = webAuthService;
        this.statusWifiStationRepo = statusWifiStationRepo;
        this.virtualThreadPool = executorService;
        this.authDataRepo = authDataRepo;
    }

    private Future<ResponseEntity<String>> futureStatusWifiStationRouter(WebRequestInfo info) {
        return virtualThreadPool.submit(() -> getStatusWifiStationRouter(info));
    }

    private ResponseEntity<String> getStatusWifiStationRouter(WebRequestInfo info) {
        return restClient.get().uri(info.obtainHostUrlWifiStation()).headers(info::addHeader).retrieve().toEntity(String.class);
    }

    @Timed(value = "igate.gw240.in.status_wifi_station")
    @Transactional(value = "appJpaTx")
    public void pollStatusWifiStation() {
        fetchBulkAndProcess(log, true, IngestionCredentials.class);
    }

    @Override
    @SneakyThrows
    public void process(AuthData authData) {
        var cred = authData.extractCredentialAbstract(IngestionCredentials.class);

        WebRequestInfo info;

        // retrieve auth + rest api info from db, if data is corrupted or not exist
        if (!authData.checkTempData(WebRequestInfo.class)) info = webAuthService.obtainInfo(cred);
        else info = WebRequestInfo.fromJson(authData.getTempData());

        // query snmp client to get table info
        var snmpTg = cred.getSnmpCred();

        // define tableutils
        TableUtils tableUtils = new TableUtils(snmp, snmpTg.obtainPduFactory());

        // return future for parallel calling, return header as well to reauth later
        var webFuture = futureStatusWifiStationRouter(info);

        // limit the columns to get in SnmpIfTableRequest in the response by creating SnmpIfTablePhyInfoRequest, to reduce latency
        // return future for parallel calling
        var snmpFuture = virtualThreadPool.submit(() -> new SnmpIfTablePhyInfoRequest().getResponse(snmpTg, tableUtils));

        WebResponse webResponse = null;

        // reauth always request to get token multime to get rid modem bug
        // if return body fail do reauth
        if (webFuture.get().getStatusCode().is4xxClientError()) {
            info = webAuthService.obtainInfo(cred);
            webResponse = new WebResponse(getStatusWifiStationRouter(info).getBody());
        }
        // if server errr
        else if (webFuture.get().getStatusCode().is5xxServerError()) {
            log.error("fetch from server modem err : {}", cred.getHost());
            return;
        }
        // if response success
        else {
            log.info("get from web success {}", cred.getHost());
            webResponse = new WebResponse(webFuture.get().getBody());
        }

        // at this time, auth success, update data to db
        info.updateTempAuthData(authData);

        List<SnmpIfTableResponse> snmpResponse = null;
        try {
            snmpResponse = snmpFuture.get();
        } catch (Exception e) {
            log.error("error when fetching snmp data ", e);
            return;
        }

        // save web and snmp data in a single raw
        // how to serialize
        // -> save data in another raw class in out package
        // then serialize the class to raw in entity class
        // package dependency follows: in -> out
        // etl -> out
        var statusWifiStationEntity = webResponse.toStatusWifiStationEntity(snmpResponse);

        // save entity including web response + snmp response to db
        statusWifiStationRepo.save(statusWifiStationEntity);
    }

    @Override
    public AuthDataRepo extractAuthDataRepo() {
        return authDataRepo;
    }
}
