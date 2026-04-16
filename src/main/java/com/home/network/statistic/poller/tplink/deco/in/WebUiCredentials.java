package com.home.network.statistic.poller.tplink.deco.in;

import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.poller.authentication.AuthData;
import com.home.network.statistic.poller.authentication.CredentialAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WebUiCredentials implements CredentialAbstract {
    private String username;
    private String password;
    private String host; // ip

    static {
        // init empty object to init template json further
        AuthData.VALID_SUBCLASSES.add(new WebUiCredentials());
    }

    public static WebUiCredentials createDecoCredentials(String password, String host) {
        return new WebUiCredentials("admin", password, host);
    }

    public String extractConcatUserPass() {
        return username + password;
    }

    @Override
    public String serializeToJson() {
        return JsonUtil.toJson(this);
    }

    public static String extCanonicalName() {
        return WebUiCredentials.class.getCanonicalName();
    }
}
