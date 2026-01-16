package com.home.network.statistic.poller.rfc1213.in;

import com.home.network.statistic.poller.snmp.BaseTarget;

public class SnmpTarget extends BaseTarget {
    public SnmpTarget(String address) {
        super(
                60_000,
                5,
                address);
    }
}
