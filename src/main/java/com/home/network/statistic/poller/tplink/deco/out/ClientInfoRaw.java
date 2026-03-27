package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.common.util.NetworkUtil;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientHourlyTraffic;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientNormalized;
import com.home.network.statistic.poller.tplink.deco.out.etl.IpNormalized;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientInfoRaw {
    // to reduce json size, map column name to shorter name
    @JsonProperty("a")
    private String ip;
    @JsonProperty("b")
    private String name;
    @JsonProperty("c")
    private String clientInterface;
    @JsonProperty("d")
    private String mac;
    @JsonProperty("e")
    private String clientType;
    @JsonProperty("f")
    private String connectionType;
    @JsonProperty("g")
    private Long upSpeed;
    @JsonProperty("h")
    private Long downSpeed;

    public boolean checkHasIp() {
        return !Optional.ofNullable(ip).map(String::isBlank).orElse(true);
    }

    public boolean hasBand5() {
        return "band5".equals(connectionType);
    }

    public boolean hasBand24() {
        return "band2_4".equals(connectionType);
    }

    public boolean hasConnectToHost() {
        return "main".equals(clientInterface);
    }

    public boolean hasConnectToGuest() {
        return "guest".equals(clientInterface);
    }

    public Integer convertIpToInt() {
        return NetworkUtil.convertIpv4StringToInt(ip);
    }

    public Integer convertTypeToInt() {
        return "wired".equals(connectionType) ? 0 : 1;
    }

    public String extractFullClientName() {
        return "%s".formatted(extractDecodedName());
    }

    public String extractDecodedName() {
        return new String(
                Base64.getDecoder().decode(name),
                StandardCharsets.UTF_8);
    }

    public Long extractNormMac() {
        return NetworkUtil.convertMacStringToLong(mac);
    }

    public ClientNormalized toClientNormalized() {
        return ClientNormalized.builder()
                .clientMac(extractNormMac())
                .clientName(extractFullClientName())
                .clientType(convertTypeToInt())
                .build();
    }

    public IpNormalized toIpNormalized() {
        return IpNormalized.builder().ipv4(convertIpToInt()).build();
    }

    public long extractTotalBytes() {
        return (upSpeed + downSpeed) * 125; // kbits -> bytes
    }

    public ClientHourlyTraffic toClientHourlyTraffic(ClientDeviceInfoRaw raw) {
        return ClientHourlyTraffic.builder()
                .client(toClientNormalized())
                .date(raw.extractDateISO())
                .hour(raw.extractHourInt())
                .totalTransmitBytes(extractTotalBytes())
                .build();
    }
}
