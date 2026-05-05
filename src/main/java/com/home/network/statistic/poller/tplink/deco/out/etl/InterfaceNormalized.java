package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InterfaceNormalized that = (InterfaceNormalized) o;
        return Objects.equals(routerMac, that.routerMac) && Objects.equals(wlanName, that.wlanName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routerMac, wlanName);
    }
}
