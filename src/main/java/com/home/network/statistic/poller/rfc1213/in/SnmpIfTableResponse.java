package com.home.network.statistic.poller.rfc1213.in;

import com.home.network.statistic.poller.igate.gw240.out.SnmpIfTablePhyInfoResponseRaw;
import com.home.network.statistic.poller.rfc1213.out.IftableTrafficEntity;
import com.home.network.statistic.poller.util.VariableBindingUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.TableEvent;

import java.time.Clock;
import java.util.List;

@Getter
@RequiredArgsConstructor
enum IfStatus {
    UP("1"),
    DOWN("2"),
    TEST("3");
    private final String value;
    public static IfStatus getIfStatus(int value) {
        if (value == 1)
            return UP;
        if (value == 2)
            return DOWN;
        return TEST;
    }
}

@Getter
public class SnmpIfTableResponse extends SnmpResponse {
    private Integer ifIndex;
    private String ifDescr;
    private Long ifPhysAddress;
    private IfStatus ifAdminStatus;
    private IfStatus ifOperStatus;
    private Long ifInOctets;
    private Long ifOutOctets;

    public SnmpIfTableResponse(TableEvent event) {
        super(Clock.systemUTC().millis());

        for (VariableBinding variableBinding : event.getColumns()) {
            OID oid = variableBinding.getOid();

            if (SnmpIfTableRequest.isOidIfIndex(oid))
                this.ifIndex = VariableBindingUtil.parseInt(variableBinding);
            else if (SnmpIfTableRequest.isOidIfDescr(oid))
                this.ifDescr = variableBinding.toValueString();
            else if (SnmpIfTableRequest.isOidIfPhysAddress(oid))
                this.ifPhysAddress = VariableBindingUtil.parseMACAddress(variableBinding);
            else if (SnmpIfTableRequest.isOidIfAdminStatus(oid))
                this.ifAdminStatus = IfStatus.getIfStatus(VariableBindingUtil.parseInt(variableBinding));
            else if (SnmpIfTableRequest.isOidIfOperStatus(oid))
                this.ifOperStatus = IfStatus.getIfStatus(VariableBindingUtil.parseInt(variableBinding));
            else if (SnmpIfTableRequest.isOidIfInOctets(oid))
                this.ifInOctets = VariableBindingUtil.parseRxTx(variableBinding);
            else if (SnmpIfTableRequest.isOidIfOutOctets(oid))
                this.ifOutOctets = VariableBindingUtil.parseRxTx(variableBinding);
        }
    }

    public SnmpIfTablePhyInfoResponseRaw toSnmpIfTablePhyInfoResponseRaw() {
        return SnmpIfTablePhyInfoResponseRaw.builder()
                .snmpIfDescr(ifDescr)
                .snmpIfPhysAddress(ifPhysAddress)
                .build();
    }

    public IftableTrafficEntity toRfc1213IgateIftableTrafficEntity(
            List<SnmpIpAddrTableResponse> rfc1213SnmpIpAddrTableRespons
    ) {
        IftableTrafficEntity iftableTrafficEntity = new IftableTrafficEntity();
        iftableTrafficEntity.setPollTime(this.toCurrentLdt());
        iftableTrafficEntity.setIfIndex(this.ifIndex);
        iftableTrafficEntity.setIfDescr(this.ifDescr);
        iftableTrafficEntity.setIfPhysAddress(this.ifPhysAddress);
        iftableTrafficEntity.setIfAdminStatus(this.ifAdminStatus.getValue());
        iftableTrafficEntity.setIfOperStatus(this.ifOperStatus.getValue());
        iftableTrafficEntity.setIfInOctets(this.ifInOctets);
        iftableTrafficEntity.setIfOutOctets(this.ifOutOctets);

        for (SnmpIpAddrTableResponse rfc1213SnmpIpAddrTableResponse : rfc1213SnmpIpAddrTableRespons) {
            if (this.ifIndex.equals(rfc1213SnmpIpAddrTableResponse.getIpAdEntIfIndex())) {
                iftableTrafficEntity.setIpAdEntAddr(rfc1213SnmpIpAddrTableResponse.getIpAdEntAddr());
                break;
            }
        }

        return iftableTrafficEntity;
    }
}
