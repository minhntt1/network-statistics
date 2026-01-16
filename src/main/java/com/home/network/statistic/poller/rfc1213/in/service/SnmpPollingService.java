package com.home.network.statistic.poller.rfc1213.in.service;

import com.home.network.statistic.poller.rfc1213.in.*;
import com.home.network.statistic.poller.rfc1213.out.IftableTrafficEntity;
import com.home.network.statistic.poller.rfc1213.out.IftableTrafficEntityRepo;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.Snmp;
import org.snmp4j.util.TableUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class SnmpPollingService {
    private final Snmp snmp;
    private final IftableTrafficEntityRepo iftableTrafficEntityRepo;
    //todo: implement way to securely store credentials
    private final List<SnmpTarget> snmpTargets = List.of(
        new SnmpTarget("udp:192.168.100.1/161")
    );

    @Timed(value = "rfc1213.in.polling.iftraffic")
    public void pollIfTraffic() {

        for (SnmpTarget snmpTarget : snmpTargets) {
            TableUtils tableUtils = new TableUtils(snmp, snmpTarget.obtainPduFactory());

            log.info("Polling RFC1213 target {}", snmpTarget);
            List<SnmpIpAddrTableResponse> rfc1213SnmpIpAddrTableRespons = new SnmpIpAddrTableRequest()
                    .getResponse(snmpTarget, tableUtils);
            List<SnmpIfTableResponse> rfc1213SnmpIfTableRespons = new SnmpIfTableRequest()
                    .getResponse(snmpTarget, tableUtils);
            log.info("End polling RFC1213 target {}", snmpTarget);

            List<IftableTrafficEntity> rfc1213IgateIftableTrafficEntities = rfc1213SnmpIfTableRespons
                    .stream()
                    .map(x -> x.toRfc1213IgateIftableTrafficEntity(rfc1213SnmpIpAddrTableRespons))
                    .toList();

            log.info("Persisting target {}", snmpTarget);
            iftableTrafficEntityRepo.saveAll(rfc1213IgateIftableTrafficEntities);
            log.info("End persisting target {}", snmpTarget);
        }
    }
}
