package com.home.network.statistic.admin.web;

import lombok.Getter;


@Getter
public class DeviceWlanConnectionsDTO {
    private String dateTimeUTC7;
    private String connectStatus;
    private String deviceName;
    private String deviceMac;
    private String deviceIpv4;
    private String ifaceName;
    private String ifaceMac;
    private String deviceVendor;
    private String ifaceVendor;

    public DeviceWlanConnectionsDTO(DeviceWlanConnectionsFact fact) {
        this.dateTimeUTC7 = fact.toUTC7DateTime();
        this.connectStatus = fact.getConnectionStatusKey().getStatus();
        this.deviceName = fact.getDeviceKey().getDeviceName();
        this.deviceMac = fact.toDeviceMacHex();
        this.deviceIpv4 = fact.toClientIp4String();
        this.ifaceName = fact.getIfaceKey().getIfaceName();
        this.ifaceMac = fact.toIfaceMacHex();
        this.deviceVendor = fact.getVendorKey().getVendorName();
        this.ifaceVendor = fact.getApVendorKey().getVendorName();
    }
}
