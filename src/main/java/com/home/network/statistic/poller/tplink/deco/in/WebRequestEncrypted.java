package com.home.network.statistic.poller.tplink.deco.in;

import lombok.AllArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
public class WebRequestEncrypted {
    private String sign;
    private String data;

    public String getURLEncodeForm() {
        return "sign=%s&data=%s".formatted(
                URLEncoder.encode(sign, StandardCharsets.UTF_8),
                URLEncoder.encode(data, StandardCharsets.UTF_8)
        );
    }
}
