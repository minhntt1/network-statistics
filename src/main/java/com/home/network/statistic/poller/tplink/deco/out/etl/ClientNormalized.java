package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientNormalized {
    private String clientName;
    private Long clientMac;
    private Integer clientType;

    public Object[] toRowMapper() {
        return new Object[]{clientMac, clientName, clientType};
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClientNormalized that = (ClientNormalized) o;
        return Objects.equals(clientName, that.clientName) && Objects.equals(clientMac, that.clientMac) && Objects.equals(clientType, that.clientType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientName, clientMac, clientType);
    }
}
