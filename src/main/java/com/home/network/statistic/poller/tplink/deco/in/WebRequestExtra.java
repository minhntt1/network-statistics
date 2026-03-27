package com.home.network.statistic.poller.tplink.deco.in;

import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

public class WebRequestExtra {
    public static final String PATH_LOGIN = "/cgi-bin/luci/;stok=/login?form=login";
    public static final String PATH_FIND_AUTH_KEYS = "/cgi-bin/luci/;stok=/login?form=keys";
    public static final String PATH_FIND_DATA_KEYS = "/cgi-bin/luci/;stok=/login?form=auth";
    public static final String PATH_FIND_DEVICES = "/cgi-bin/luci/;stok=%s/admin/device?form=device_list";
    public static final String PATH_FIND_CLIENT_LIST = "/cgi-bin/luci/;stok=%s/admin/client?form=client_list";
    public static final String PATH_FIND_WLAN_INFO = "/cgi-bin/luci/;stok=%s/admin/wireless?form=wlan";

    private final List<String> headers = new ArrayList<>();
    @Setter
    @Getter
    private WebEncryptor webEncryptor;
    @Setter
    private String stok;

    public WebRequestExtra() {
        // very important header
        headers.add("Content-Type");
        headers.add("application/json");
    }

    public void putCookieHeader(HttpHeaders httpLoginHeaders) {
        headers.add("Cookie");
        headers.add(httpLoginHeaders.firstValue("Set-Cookie").get().split(";")[0]);
    }

    public HttpRequest.BodyPublisher toRequestBody(WebRequest requestBody, boolean enc, boolean login) {
        return HttpRequest.BodyPublishers.ofString(
                enc ?
                        requestBody.encryptDataAES(webEncryptor, login).getURLEncodeForm() :
                        requestBody.toJson()
        );
    }

    public HttpRequest getRequestFindDevices() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), true, false)
                )
                .headers(headers.toArray(String[]::new))  // does not accept array of size 0
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_DEVICES.formatted(stok)))
                .build();
    }

    public HttpRequest getRequestFindClientList(String deviceMac) {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadClientByDeviceRequest(deviceMac), true, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_CLIENT_LIST.formatted(stok)))
                .build();
    }

    public HttpRequest getRequestFindWlan() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), true, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_WLAN_INFO.formatted(stok)))
                .build();
    }

    public HttpRequest getRequestDataKeys() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), false, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_DATA_KEYS))
                .build();
    }

    public HttpRequest getRequestAuthKeys() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), false, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_AUTH_KEYS))
                .build();
    }

    public HttpRequest getRequestLogin() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createRequestLogin(webEncryptor), true, true)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_LOGIN))
                .build();
    }
}
