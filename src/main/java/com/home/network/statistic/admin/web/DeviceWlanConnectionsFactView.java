package com.home.network.statistic.admin.web;

import com.home.network.statistic.common.util.NetworkUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * View class holding data from raw sql
 */
@Getter
@ToString
@Setter     // setter required for reflection
public class DeviceWlanConnectionsFactView {

    private String date;
    private String connectionStatus;
    private Integer time;
    private String clientName;
    private Integer clientIpv4;
    private Long clientMac;
    private String apName;
    private String apMac;
    private String ifaceName;
    private String ifacePhyName;
    private Long ifaceMac;
    private String clientVendor;
    private String apVendor;

    public String toUTC7DateTime() {
        return ZonedDateTime.of(
                    LocalDate.parse(date, DateTimeFormatter.ISO_DATE),
                    LocalTime.ofSecondOfDay(time),
                    ZoneOffset.UTC)
                .withZoneSameInstant(ZoneOffset.ofHours(7))
                .toLocalDateTime().toString();
    }


    public String toDeviceMacHex() {
        return NetworkUtil.convertMacLongToString(clientMac);
    }

    public String toIfaceMacHex() {
        return NetworkUtil.convertMacLongToString(ifaceMac);
    }

    public String toClientIp4String() {
        return NetworkUtil.convertIpIntToString(clientIpv4);
    }
}
