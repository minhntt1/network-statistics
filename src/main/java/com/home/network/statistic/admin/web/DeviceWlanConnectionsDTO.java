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

    public DeviceWlanConnectionsDTO(DeviceWlanConnectionsFactView deviceWlanConnectionsFactView) {
        this.dateTimeUTC7 = deviceWlanConnectionsFactView.toUTC7DateTime();
        this.connectStatus = deviceWlanConnectionsFactView.getConnectionStatus();
        this.deviceName = deviceWlanConnectionsFactView.getClientName();
        this.deviceMac = deviceWlanConnectionsFactView.toDeviceMacHex();
        this.deviceIpv4 = deviceWlanConnectionsFactView.toClientIp4String();
        this.ifaceName = deviceWlanConnectionsFactView.getIfaceName();
        this.ifaceMac = deviceWlanConnectionsFactView.toIfaceMacHex();
        this.deviceVendor = deviceWlanConnectionsFactView.getClientVendor();
        this.ifaceVendor = deviceWlanConnectionsFactView.getApVendor();
    }
}
