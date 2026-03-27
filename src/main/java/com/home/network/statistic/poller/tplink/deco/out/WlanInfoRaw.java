package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WlanInfoRaw {
    @JsonProperty("a")
    private String wlanType;    // band5_1, iot, band2_4
    @JsonProperty("b")
    private String wlanMode;    // backhaul, guest, host
    @JsonProperty("c")
    private String channel;
    @JsonProperty("d")
    private String ssid;
    @JsonProperty("e")
    private String password;
    @JsonProperty("f")
    private Boolean enable;
    @JsonProperty("g")
    private Boolean enable2g;
    @JsonProperty("h")
    private Boolean enable5g;

    public boolean hasBand5() {
        return "band5_1".equals(wlanType);
    }

    public boolean hasBand24() {
        return "band2_4".equals(wlanType);
    }

    public boolean hasAttachedToClient(ClientInfoRaw clientInfoRaw) {
        // band5 -> band5_1 (this)
        // band2_4 -> band2_4
        // main -> host
        // guest -> guest
        return
            (hasBand5() && clientInfoRaw.hasBand5() ||
                hasBand24() && clientInfoRaw.hasBand24()) &&
            ("host".equals(wlanMode) && clientInfoRaw.hasConnectToHost() ||
                "guest".equals(wlanMode) && clientInfoRaw.hasConnectToGuest());
    }

    public String decodeSSID() {
        return new String(Base64.getDecoder().decode(ssid), StandardCharsets.UTF_8);
    }

    // create a detailed ssid with band type
    public String createFullSSID() {
        return "%s(%s)".formatted(
                decodeSSID(),
                hasBand5() ? "5Ghz" : hasBand24() ? "2.4Ghz" : ""
        );
    }
}
