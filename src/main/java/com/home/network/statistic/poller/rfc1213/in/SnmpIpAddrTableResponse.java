package com.home.network.statistic.poller.rfc1213.in;

import com.home.network.statistic.poller.util.VariableBindingUtil;
import lombok.Getter;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.TableEvent;

import java.time.Clock;

@Getter
public class SnmpIpAddrTableResponse extends SnmpResponse {
    private Integer ipAdEntAddr;
    private Integer ipAdEntIfIndex;

    public SnmpIpAddrTableResponse(TableEvent tableEvent) {
        super(Clock.systemUTC().millis());

        for (VariableBinding variableBinding : tableEvent.getColumns()) {
            OID oid = variableBinding.getOid();
            if (SnmpIpAddrTableRequest.isOidIpAdEntAddr(oid))
                this.ipAdEntAddr = VariableBindingUtil.parseIPAddress(variableBinding);
            else if(SnmpIpAddrTableRequest.isOidIpAdEntIfIndex(oid))
                this.ipAdEntIfIndex = VariableBindingUtil.parseInt(variableBinding);
        }
    }
}
