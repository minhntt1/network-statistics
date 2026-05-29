package com.home.network.statistic.admin.web;

import com.home.network.statistic.common.util.NetworkUtil;
import com.home.network.statistic.vendor.VendorEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

@Table(name = "device_wlan_connections_fact")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class DeviceWlanConnectionsFact {
    public static class Id implements Serializable {

        private Integer dateKey;
        private Integer timeKey;
        private Integer deviceKey;
        private Integer deviceIpKey;
        private Integer apKey;
        private Integer ifaceKey;
        private Integer vendorKey;
        private Integer apVendorKey;
        private Integer connectionStatusKey;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Id id = (Id) o;
            return Objects.equals(dateKey, id.dateKey) && Objects.equals(timeKey, id.timeKey) && Objects.equals(deviceKey, id.deviceKey) && Objects.equals(deviceIpKey, id.deviceIpKey) && Objects.equals(apKey, id.apKey) && Objects.equals(ifaceKey, id.ifaceKey) && Objects.equals(vendorKey, id.vendorKey) && Objects.equals(apVendorKey, id.apVendorKey) && Objects.equals(connectionStatusKey, id.connectionStatusKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dateKey, timeKey, deviceKey, deviceIpKey, apKey, ifaceKey, vendorKey, apVendorKey, connectionStatusKey);
        }
    }

    @EmbeddedId
    private Id key;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("dateKey")
    @JoinColumn(name = "date_key")
    private DateDim dateKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("timeKey")
    @JoinColumn(name = "time_key")
    private TimeDim timeKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deviceKey")
    @JoinColumn(name = "device_key")
    private DeviceDim deviceKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("deviceIpKey")
    @JoinColumn(name = "device_ip_key")
    private IpDim deviceIpKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("apKey")
    @JoinColumn(name = "ap_key")
    private ApDim apKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ifaceKey")
    @JoinColumn(name = "iface_key")
    private IfaceDim ifaceKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vendorKey")
    @JoinColumn(name = "vendor_key")
    private VendorEntity vendorKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("apVendorKey")
    @JoinColumn(name = "ap_vendor_key")
    private VendorEntity apVendorKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("connectionStatusKey")
    @JoinColumn(name = "cnt_status_key")
    private ConnectionStatusDim connectionStatusKey;

    public String toUTC7DateTime() {
        return ZonedDateTime.of(dateKey.getDate(), timeKey.timeToLocalTime(), ZoneOffset.UTC)
                .withZoneSameInstant(ZoneOffset.ofHours(7))
                .toLocalDateTime().toString();
    }


    public String toDeviceMacHex() {
        return NetworkUtil.convertMacLongToString(deviceKey.getDeviceMac());
    }

    public String toIfaceMacHex() {
        return NetworkUtil.convertMacLongToString(ifaceKey.getIfaceMac());
    }

    public String toClientIp4String() {
        return NetworkUtil.convertIpIntToString(deviceIpKey.getIpv4());
    }
}
