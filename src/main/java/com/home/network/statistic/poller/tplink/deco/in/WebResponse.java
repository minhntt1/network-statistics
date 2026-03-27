package com.home.network.statistic.poller.tplink.deco.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.poller.tplink.deco.out.ClientInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.WlanInfoRaw;
import lombok.NoArgsConstructor;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class WebResponse {
    @JsonProperty("result")
    private WebResponseResult result;

    @JsonProperty("error_code")
    private Integer errorCode;

    @JsonProperty("success")
    public Boolean success;

    @JsonProperty("msg")
    private String msg;

    public List<ClientInfoRaw> extractClientInfoRaws() {
        return result.toClientInfoRaws();
    }

    public List<WlanInfoRaw> extractWlanInfoRaws() {
        return result.toWlanInfoRaws();
    }

    public List<DeviceInfoRaw> extractDeviceInfoRaws(LocalDateTime pollTime) {
        return result.toDeviceInfoRaws(pollTime);
    }

    public boolean hasClientList() {
        return result.getClientList() != null;
    }

    public List<String> getDeviceMacs() {
        return result.extractDeviceMacs();
    }

    public String getStok() {
        return result.getStok();
    }

    public Integer getSeq() {
        return result.getSeq();
    }

    public List<String> getPassword() {
        return result.getPassword();
    }

    public List<String> getKey() {
        return result.getKey();
    }

    public static WebResponse from(String json) {
        return JsonUtil.fromJson(json, WebResponse.class);
    }

    public boolean hasError() {
        return errorCode.equals(1) || !Optional.ofNullable(success).orElse(true);
    }
}
