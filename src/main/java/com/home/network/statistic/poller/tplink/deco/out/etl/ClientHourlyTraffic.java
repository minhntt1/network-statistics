package com.home.network.statistic.poller.tplink.deco.out.etl;

import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientHourlyTraffic {
    private String date;
    private Integer hour;
    private ClientNormalized client;
    private Long totalTransmitBytes;

    public static List<Object[]> reduceRecordsToRows(List<ClientHourlyTraffic> traffic) {
        return traffic.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.reducing(ClientHourlyTraffic::addBytes)))
                .values()
                .stream().filter(Optional::isPresent)
                .map(Optional::get)
                .map(ClientHourlyTraffic::toMapRow).toList();
    }

    // todo : function to map to list row, and reduce records so db don't have to accumulate
    public Object[] toMapRow() {
        return new Object[] {date, hour, client.getClientMac(), client.getClientName(), client.getClientType(), totalTransmitBytes};
    }


    public ClientHourlyTraffic addBytes(ClientHourlyTraffic o) {
        totalTransmitBytes += o.totalTransmitBytes;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClientHourlyTraffic that = (ClientHourlyTraffic) o;
        return Objects.equals(date, that.date) && Objects.equals(hour, that.hour) && Objects.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, hour, client);
    }
}
