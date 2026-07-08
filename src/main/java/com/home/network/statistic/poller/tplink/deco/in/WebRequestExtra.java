package com.home.network.statistic.poller.tplink.deco.in;

import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.poller.authentication.AuthData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class WebRequestExtra {
    public static final String PATH_LOGIN = "/cgi-bin/luci/;stok=/login?form=login";
    public static final String PATH_FIND_AUTH_KEYS = "/cgi-bin/luci/;stok=/login?form=keys";
    public static final String PATH_FIND_DATA_KEYS = "/cgi-bin/luci/;stok=/login?form=auth";
    public static final String PATH_FIND_DEVICES = "/cgi-bin/luci/;stok=%s/admin/device?form=device_list";
    public static final String PATH_FIND_CLIENT_LIST = "/cgi-bin/luci/;stok=%s/admin/client?form=client_list";
    public static final String PATH_FIND_WLAN_INFO = "/cgi-bin/luci/;stok=%s/admin/wireless?form=wlan";

    private final List<String> headers = new ArrayList<>();
    @Setter
    private WebEncryptor webEncryptor;
    @Setter
    private String stok;

    public WebRequestExtra(WebUiCredentials webUiCredentials) {
        // create a temp web encryptor
        webEncryptor = new WebEncryptor();
        webEncryptor.initCredentials(webUiCredentials);
        // very important header
        headers.add("Content-Type");
        headers.add("application/json");
    }

    public void putCookieHeader(HttpHeaders httpLoginHeaders) {
        headers.add("Cookie");
        headers.add(httpLoginHeaders.firstValue("Set-Cookie").get().split(";")[0]);
    }

    public void initWebEncryptorKeys(WebResponse authResponse, WebResponse dataResponse) {
        webEncryptor.init(authResponse, dataResponse);
    }

    public HttpRequest.BodyPublisher toRequestBody(WebRequest requestBody, boolean enc, boolean login) {
        return HttpRequest.BodyPublishers.ofString(
                enc ?
                        requestBody.encryptDataAES(webEncryptor, login).getURLEncodeForm() :
                        requestBody.toJson()
        );
    }

    public HttpRequest extRequestFindDevices() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), true, false)
                )
                .headers(headers.toArray(String[]::new))  // does not accept array of size 0
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_DEVICES.formatted(stok)))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpRequest extRequestFindClientList(String deviceMac) {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadClientByDeviceRequest(deviceMac), true, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_CLIENT_LIST.formatted(stok)))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpRequest extRequestFindWlan() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), true, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_WLAN_INFO.formatted(stok)))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpRequest extRequestDataKeys() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), false, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_DATA_KEYS))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpRequest extRequestAuthKeys() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createReadRequest(), false, false)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_FIND_AUTH_KEYS))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpRequest extRequestLogin() {
        return HttpRequest.newBuilder()
                .POST(
                        toRequestBody(WebRequest.createRequestLogin(webEncryptor), true, true)
                )
                .headers(headers.toArray(String[]::new))
                .uri(URI.create("http://" + webEncryptor.getWebUiCredentials().getHost() + PATH_LOGIN))
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public void updateAuthEntity(AuthData data) {
        data.updateTempData(toJson());
    }

    public static WebRequestExtra fromJson(String json) {
        return JsonUtil.fromJson(json, WebRequestExtra.class);
    }
}
