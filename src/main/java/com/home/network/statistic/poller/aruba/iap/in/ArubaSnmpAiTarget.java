package com.home.network.statistic.poller.aruba.iap.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.snmp.BaseTarget;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArubaSnmpAiTarget extends BaseTarget {
    static {
        AuthData.VALID_SUBCLASSES.add(new ArubaSnmpAiTarget());
    }
}
