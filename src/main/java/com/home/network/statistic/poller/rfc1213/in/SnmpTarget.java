package com.home.network.statistic.poller.rfc1213.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.snmp.BaseTarget;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnmpTarget extends BaseTarget {
    static {
        AuthData.VALID_SUBCLASSES.add(new SnmpTarget());
    }

    public SnmpTarget(String address) {
        super(
                60_000,
                5,
                address);
    }
}
