package com.home.network.statistic.poller.igate.gw240.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.authentication.CredentialAbstract;
import com.home.network.statistic.poller.rfc1213.in.SnmpTarget;
import com.home.network.statistic.poller.snmp.BaseTarget;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class IngestionCredentials implements CredentialAbstract {
    private static final String AUTH_PATTERN = "uid =%s; psw=%s";
    private String user;
    private String pass;
    private String host;
    private SnmpTarget snmpCred = new SnmpTarget();

    static {
        AuthData.VALID_SUBCLASSES.add(new IngestionCredentials());
    }

    public String encodeBase64() {
        return Base64.getEncoder().encodeToString(AUTH_PATTERN.formatted(this.user, this.pass).getBytes(StandardCharsets.UTF_8));
    }

    public WebRequestInfo obtainAuthInfo() {
        return new WebRequestInfo(host, encodeBase64());
    }

    public WebRequestInfo obtainAuthInfo(String sessionHeader) {
        return new WebRequestInfo(host, encodeBase64(), sessionHeader);
    }

    @Override
    public String serializeToJson() {
        return JsonUtil.toJson(this);
    }
}
