package com.home.network.statistic.poller.tplink.deco.in;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.home.network.statistic.common.util.JsonUtil;
import lombok.Getter;
import lombok.SneakyThrows;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class WebResponseEncrypted {
    private String data;

    public static WebResponseEncrypted from(String json) {
        return JsonUtil.fromJson(json, WebResponseEncrypted.class);
    }

    public boolean checkInvalidData() {
        return data == null || data.isBlank();
    }

    @SneakyThrows
    public WebResponse toJsonDecryptAES(WebEncryptor we) {
        return we.decryptResponse(this);
    }

    @SneakyThrows
    public WebResponse toJsonDecryptAES(WebRequestExtra we) {
        return we.getWebEncryptor().decryptResponse(this);
    }
}
