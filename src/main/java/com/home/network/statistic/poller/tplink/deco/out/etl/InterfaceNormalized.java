package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterfaceNormalized {
    private Long routerMac;
    private String wlanName;

    public Object[] toRowMapper() {
        return new Object[] {routerMac, wlanName};
    }
}
