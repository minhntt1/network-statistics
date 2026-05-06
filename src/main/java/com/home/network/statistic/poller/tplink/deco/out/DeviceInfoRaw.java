package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.common.util.NetworkUtil;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApInfo;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApConnLostCnt;
import com.home.network.statistic.poller.tplink.deco.out.etl.IpNormalized;
import lombok.*;

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
    private String inetStatus;  // internet conn status?
    @JsonProperty("f")
    private String groupStatus; // mesh group status ?

    public static DeviceInfoRaw from(String jsonString) {
        return JsonUtil.fromJson(jsonString, DeviceInfoRaw.class);
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public String extractDateFromEvent() {
        return pollTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public ApConnLostCnt toApConnLostCnt() {
        return ApConnLostCnt.builder()
                .apInfo(toApInfo())
                .ipNormalized(toIpNormalized())
                .lostCnt(0)
                .startWeek(extractDateFromEvent())
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

    public boolean checkToOfflineInetStat(DeviceInfoRaw prev) {
        return !"online".equals(inetStatus) &&
                "online".equals(prev.inetStatus);
    }

    public boolean checkToDisconnectGroupStat(DeviceInfoRaw prev) {
        return !"connected".equals(groupStatus) &&
                "connected".equals(prev.groupStatus);
    }

    public boolean checkConnLost(DeviceInfoRaw prev) {
        return checkToDisconnectGroupStat(prev) ||
                checkToOfflineInetStat(prev);
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
