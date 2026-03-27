package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpNormalized {
    // define ip to put in normalized ip table
    private Integer ipv4;

    public Object[] toRowMapper() {
        return new Object[]{ipv4};
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IpNormalized that = (IpNormalized) o;
        return Objects.equals(ipv4, that.ipv4);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ipv4);
    }
}
