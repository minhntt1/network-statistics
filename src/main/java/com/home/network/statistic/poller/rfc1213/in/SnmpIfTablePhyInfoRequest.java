package com.home.network.statistic.poller.rfc1213.in;

import org.snmp4j.smi.OID;

public class SnmpIfTablePhyInfoRequest extends SnmpIfTableRequest {
    @Override
    public OID[] getRequestColumns() {
        return new OID[]{
            new OID(ifDescr),
            new OID(ifPhysAddress)
        };
    }
}
