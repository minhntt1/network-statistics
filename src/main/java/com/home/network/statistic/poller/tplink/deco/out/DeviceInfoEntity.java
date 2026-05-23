package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApInfo;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApConnLostCnt;
import com.home.network.statistic.poller.tplink.deco.out.etl.IpNormalized;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "tplink_deco_device_info_stg_ingest")
@NoArgsConstructor
@Getter
@Setter
// REMEMBER CAREFULLY: IF PERSIST OBJECT TO DB, BUT DOESN’T DECLARE SETTER ON OBJECT, HIBERNATE USES REFLECTION TO CREATE OBJECT -> NULL ATTRIBUTE VALUE
@Builder
@AllArgsConstructor
public class DeviceInfoEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime pollTime;
    private String rawData;

    public Map<String, DeviceInfoRaw> toMapStkToState() {
        return extractDeviceInfoRaw()
                .stream()
                .collect(Collectors.toMap(raw -> raw.toApConnLostCnt().extractKeyConnLostState(), Function.identity()));
    }

    public Map<String, ApConnLostCnt> toApConnLost() {
        return extractDeviceInfoRaw()
                .stream()
                .map(DeviceInfoRaw::toApConnLostCnt)
                .collect(Collectors.toMap(ApConnLostCnt::extractKeyConnLostState, Function.identity()));
    }

    public List<Object[]> toInsertableIps() {
        return extractDeviceInfoRaw()
                .stream()
                .map(DeviceInfoRaw::toIpNormalized)
                .map(IpNormalized::toRowMapper)
                .toList();
    }

    public List<Object[]> toInsertableAps() {
        return extractDeviceInfoRaw()
                .stream()
                .map(DeviceInfoRaw::toApInfo)
                .map(ApInfo::toRowMap)
                .toList();
    }

    public List<DeviceInfoRaw> extractDeviceInfoRaw() {
        var list = JsonUtil.fromJsonToArray(rawData, DeviceInfoRaw.class);
        for (var o : list)
            o.setPollTime(pollTime);
        return list;
    }
}
