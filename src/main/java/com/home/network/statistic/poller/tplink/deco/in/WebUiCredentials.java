package com.home.network.statistic.poller.tplink.deco.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class WebUiCredentials {
    private String username;
    private String password;
    private String host; // ip

    public static WebUiCredentials createDecoCredentials(String password, String host) {
        return new WebUiCredentials("admin", password, host);
    }

    public String getConcatUserPass() {
        return username + password;
    }
}
