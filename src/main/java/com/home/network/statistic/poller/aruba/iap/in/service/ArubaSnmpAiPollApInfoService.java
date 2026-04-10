package com.home.network.statistic.poller.aruba.iap.in.service;

import com.home.network.statistic.poller.aruba.iap.in.*;
import com.home.network.statistic.poller.aruba.iap.out.*;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.authentication.AuthDataProcessor;
import com.home.network.statistic.poller.authentication.AuthDataRepo;
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
public class ArubaSnmpAiPollApInfoService implements AuthDataProcessor<ArubaSnmpAiTarget> {
    private final Snmp snmp;
    private final ArubaAiApInfoRepository arubaAiApInfoRepository;
    private final AuthDataRepo authDataRepo;

    @Timed(value = "aruba.iap.in.polling.apinfo")
    @Transactional(value = "appJpaTx")
    public void pollApInfo() {
        fetchBulkAndProcess(log, false, ArubaSnmpAiTarget.class);
    }

    @Override
    public void process(AuthData authData) {
        ArubaSnmpAiTarget arubaSnmpAiTarget = authData.extractCredentialAbstract(ArubaSnmpAiTarget.class);
        TableUtils tableUtils = new TableUtils(snmp, arubaSnmpAiTarget.obtainPduFactory());

        log.info("start polling ap info");
        List<ArubaAiApInfoEntity> arubaAiApInfoEntities = new ArubaSnmpAiAccessPointRequest()
                .getResponse(arubaSnmpAiTarget, tableUtils)
                .stream()
                .map(ArubaSnmpAiAccessPointResponse::toApInfo)
                .toList();
        log.info("end polling ap info");

        log.info("persisting ap info");
        arubaAiApInfoRepository.saveAll(arubaAiApInfoEntities);
        log.info("completed persisting ap info");
    }

    @Override
    public AuthDataRepo extractAuthDataRepo() {
        return authDataRepo;
    }
}
