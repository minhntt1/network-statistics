package com.home.network.statistic.poller.rfc1213.in;

import org.snmp4j.smi.OID;
import org.snmp4j.util.TableUtils;

import java.util.List;

public abstract class SnmpRequest<T extends SnmpResponse> {
    public abstract OID[] getRequestColumns();
    public abstract List<T> getResponse(SnmpTarget target, TableUtils tableUtils);
}
