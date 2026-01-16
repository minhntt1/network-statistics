package com.home.network.statistic.poller.rfc1213.in;

import org.snmp4j.smi.OID;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import java.util.List;

public class SnmpIpAddrTableRequest extends SnmpRequest<SnmpIpAddrTableResponse> {
    private static final String ipAdEntAddr = "1.3.6.1.2.1.4.20.1.1.";
    private static final String ipAdEntIfIndex = "1.3.6.1.2.1.4.20.1.2.";

    public static boolean isOidIpAdEntAddr(OID oid) {
        return oid.toString().startsWith(ipAdEntAddr);
    }

    public static boolean isOidIpAdEntIfIndex(OID oid) {
        return oid.toString().startsWith(ipAdEntIfIndex);
    }

    @Override
    public OID[] getRequestColumns() {
        return new OID[] {
            new OID(ipAdEntAddr),
            new OID(ipAdEntIfIndex),
        };
    }

    @Override
    public List<SnmpIpAddrTableResponse> getResponse(SnmpTarget target, TableUtils tableUtils) {
        List<TableEvent> tableEvents = tableUtils.getTable(
                target.buildTarget(),
                this.getRequestColumns(),
                null,null
        );
        return tableEvents.stream()
                .map(SnmpIpAddrTableResponse::new)
                .toList();
    }
}
