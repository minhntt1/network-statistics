package com.home.network.statistic.admin.web;

import com.home.network.statistic.common.util.NetworkUtil;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
public class DeviceDimDTO {
    private Integer deviceKey;
    private String deviceMac;
    private String deviceName;
    private Integer deviceIfaceWifi;

    public DeviceDimDTO(DeviceDim deviceDim) {
        this.deviceKey = deviceDim.getDeviceKey();
        this.deviceMac = NetworkUtil.convertMacLongToString(deviceDim.getDeviceMac());
        this.deviceName = deviceDim.getDeviceName();
        this.deviceIfaceWifi = deviceDim.getDeviceIfaceWifi();
    }
}
