package com.home.network.statistic.poller.tplink.deco.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.common.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebRequest {
    @JsonProperty("operation")
    private String operation;

    @JsonProperty("params")
    private Map<String, String> params = new HashMap<>();

    public WebRequest(String op) {
        this.operation = op;
    }

    public static WebRequest createReadClientByDeviceRequest(String deviceMac) {
        var req = createReadRequest();
        req.addDeviceMacParam(deviceMac);
        return req;
    }

    public static WebRequest createReadRequest() {
        return new WebRequest("read");
    }

    public static WebRequest createRequestLogin(WebEncryptor enc) {
        var req = new WebRequest("login");
        req.addEncPassParam(enc);
        return req;
    }

    public void addDeviceMacParam(String mac) {
        params.put("device_mac", mac);
    }

    public void addEncPassParam(WebEncryptor we) {
        params.put("password", we.getEncryptPassForAuth());
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public WebRequestEncrypted encryptDataAES(WebEncryptor e, boolean login) {
        return e.encryptRequest(this, login);// todo
    }
}
