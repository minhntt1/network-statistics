package com.home.network.statistic.poller.tplink.deco.out.etl;

import com.home.network.statistic.common.util.JsonUtil;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientConnectionEvent {
    private String dateConnect;
    private Integer timeConnect;
    private ClientNormalized client;
    private IpNormalized clientIp;
    private InterfaceNormalized clientIface;
    private Integer connectStatus;

    public boolean checkDiffEvent(ClientConnectionEvent other) {
        // consider client is the same
        // check if ip or ap connect is different
        return !clientIp.equals(other.clientIp) || !clientIface.equals(other.clientIface);
    }

    public void disconnect(LocalDateTime disconnectTime) {
        this.dateConnect = disconnectTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        this.timeConnect = disconnectTime.toLocalTime().toSecondOfDay();
        this.connectStatus = 2; // disconnect flag
    }

    public ClientConnectionEvent(String date, Integer time, ClientNormalized client, IpNormalized ip, InterfaceNormalized clientIface) {
        this.dateConnect = date;
        this.timeConnect = time;
        this.client = client;
        this.clientIp = ip;
        this.clientIface = clientIface;
        this.connectStatus = 1;        // connect
    }

    public static List<Object[]> toObjectForInsert(List<ClientConnectionEvent> events) {
        return events.stream().map(ClientConnectionEvent::toObjectRow).toList();
    }

    public Object[] toObjectRow() {
        return new Object[]{
                dateConnect,
                timeConnect,
                client.getClientName(),
                client.getClientMac(),
                client.getClientType(),
                clientIp.getIpv4(),
                clientIface.getRouterMac(),
                clientIface.getWlanName(),
                connectStatus};
    }

    public static String extStateKeyConnectPrefix() {
        return "clientConnState";
    }

    public static boolean checkIsStateKeyConnect(String key) {
        return key.startsWith(extStateKeyConnectPrefix());
    }

    public String extractStateForBatchJob() {
        return "%s_%s.%d".formatted(
                extStateKeyConnectPrefix(),
                client.getClientName(),
                client.getClientMac()
        );
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public static ClientConnectionEvent from(String json) {
        return JsonUtil.fromJson(json, ClientConnectionEvent.class);
    }
}
