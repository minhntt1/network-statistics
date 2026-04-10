package com.home.network.statistic.poller.aruba.iap.in.service;

import com.home.network.statistic.poller.aruba.iap.in.*;
import com.home.network.statistic.poller.aruba.iap.out.*;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.authentication.AuthDataProcessor;
import com.home.network.statistic.poller.authentication.AuthDataRepo;
import com.home.network.statistic.poller.tplink.deco.in.WebUiCredentials;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.Snmp;
import org.snmp4j.util.TableUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class ArubaSnmpAiPollClientService implements AuthDataProcessor<ArubaSnmpAiTarget> {
    private final Snmp snmp;
    private final ArubaAiClientInfoRepository arubaAiClientInfoRepository;
    private final AuthDataRepo authDataRepo;

    @Timed(value = "aruba.iap.in.polling.clientinfo")
    @Transactional(value = "appJpaTx")
    public void pollClientInfo() {
        fetchBulkAndProcess(log, false, ArubaSnmpAiTarget.class);
    }

    @Override
    public void process(AuthData authData) {
        ArubaSnmpAiTarget arubaSnmpAiTarget = authData.extractCredentialAbstract(ArubaSnmpAiTarget.class);
        TableUtils tableUtils = new TableUtils(snmp, arubaSnmpAiTarget.obtainPduFactory());

        log.info("start polling client info");
        List<ArubaAiClientInfoEntity> arubaAiClientInfoEntities = new ArubaSnmpAiClientRequest()
                .getResponse(arubaSnmpAiTarget, tableUtils)
                .stream()
                .map(ArubaSnmpAiClientResponse::toClientInfo)
                .toList();
        log.info("end polling client info");

        log.info("persisting client info");
        arubaAiClientInfoRepository.saveAll(arubaAiClientInfoEntities);
        log.info("completed persisting client info");
    }

    @Override
    public AuthDataRepo extractAuthDataRepo() {
        return authDataRepo;
    }
}
