package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.common.util.NetworkUtil;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApInfo;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApRebootCnt;
import com.home.network.statistic.poller.tplink.deco.out.etl.IpNormalized;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceInfoRaw {
    @JsonIgnore
    private LocalDateTime pollTime;
    @JsonProperty("a")
    private String deviceModel;
    @JsonProperty("b")
    private String deviceIp;
    @JsonProperty("c")
    private String nickName;
    @JsonProperty("d")
    private String deviceMac;
    @JsonProperty("e")
    private String inetStatus;  // not sure
    @JsonProperty("f")
    private String groupStatus; // not sure

    public String extractWeekFromEvent() {
        return pollTime.toLocalDate().with(DayOfWeek.MONDAY)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public ApRebootCnt toApRebootCnt() {
        return ApRebootCnt.builder()
                .apInfo(toApInfo())
                .ipNormalized(toIpNormalized())
                .rebootCnt(0)
                .startWeek(extractWeekFromEvent())
                .build();
    }

    public IpNormalized toIpNormalized() {
        return IpNormalized.builder()
                .ipv4(extractIp())
                .build();
    }

    public Long extractNormMac() {
        return NetworkUtil.convertMacStringToLong(deviceMac);
    }

    public int extractIp() {
        return NetworkUtil.convertIpv4StringToInt(deviceIp);
    }

    public ApInfo toApInfo() {
        return ApInfo.builder()
                .apMac(extractNormMac())
                .apName(extractFullDeviceName())
                .build();
    }

    public String extractFullDeviceName() {
        return "%s(%s-%s)".formatted(nickName, deviceModel, "tplink");
    }

    public static DeviceInfoEntity toEmptyDeviceInfoEntity() {
        return DeviceInfoEntity.builder()
                .rawData(JsonUtil.toJson(Collections.emptyList()))
                .pollTime(LocalDateTime.now())
                .build();
    }

    public static DeviceInfoEntity toDeviceInfoEntity(List<DeviceInfoRaw> rawList) {
        var pollTime = rawList.getFirst().pollTime;
        return DeviceInfoEntity.builder()
                .pollTime(pollTime)
                .rawData(JsonUtil.toJson(rawList))
                .build();
    }
}
