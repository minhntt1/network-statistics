package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.home.network.statistic.common.util.JsonUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tplink_deco_client_device_wlan_stg_ingest")
@NoArgsConstructor
@Getter
@Setter
// REMEMBER CAREFULLY: IF PERSIST OBJECT TO DB, BUT DOESN’T DECLARE SETTER ON OBJECT, HIBERNATE USES REFLECTION TO CREATE OBJECT -> NULL ATTRIBUTE VALUE
@Builder
@AllArgsConstructor
public class ClientDeviceInfoEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime pollTime;
    private String rawData;

    public ClientDeviceInfoRaw toClientDeviceInfoRaw() {
        var obj = JsonUtil.fromJson(rawData, ClientDeviceInfoRaw.class);
        obj.setPollTime(pollTime);
        return obj;
    }


}
