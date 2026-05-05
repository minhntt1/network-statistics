package com.home.network.statistic.poller.tplink.deco.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.common.util.JsonUtil;
import com.home.network.statistic.common.util.NetworkUtil;
import com.home.network.statistic.poller.tplink.deco.out.etl.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientDeviceInfoRaw {
    @JsonIgnore
    private LocalDateTime pollTime;

    @JsonProperty("aa")
    private Map<String, List<ClientInfoRaw>> mapDeviceToClient;

    @JsonProperty("ab")
    private List<WlanInfoRaw> wlanInfoRaw;

    public ClientDeviceInfoRaw() {
        pollTime = LocalDateTime.now();
        mapDeviceToClient = new HashMap<>();
        wlanInfoRaw = new ArrayList<>();
    }

    public ClientDeviceInfoRaw(LocalDateTime pollTime, List<WlanInfoRaw> wlanInfoRaw) {
        this.pollTime = pollTime;
        this.mapDeviceToClient = new HashMap<>();
        this.wlanInfoRaw = wlanInfoRaw;
    }

    // create empty record to insert to db to prevent case when no data in db -> can't trigger state removal code in worker
    public static ClientDeviceInfoRaw initEmptyRecord() {
        return new ClientDeviceInfoRaw();
    }

    public String extractDateISO() {
        return pollTime.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public Integer extractTimeInt() {
        return pollTime.toLocalTime().toSecondOfDay();
    }

    public Integer extractHourInt() {
        return pollTime.toLocalTime().truncatedTo(ChronoUnit.HOURS).toSecondOfDay();// truncate to hours and second of days
    }

    public List<ClientHourlyTraffic> toClientHourlyTraffics() {
        return mapDeviceToClient.values()
                .stream()
                .flatMap(List::stream)
                .map(raw -> raw.toClientHourlyTraffic(this))
                .toList();
    }

    public static Long extractDeviceMac(String device) {
        return NetworkUtil.convertMacStringToLong(device);
    }

    public ClientDeviceInfoEntity toClientDeviceInfoEntity() {
        return ClientDeviceInfoEntity.builder()
                .pollTime(pollTime)
                .rawData(JsonUtil.toJson(this))
                .build();
    }

    public void putToMapDeviceToClient(String mac, List<ClientInfoRaw> infoRaws) {
        mapDeviceToClient.put(mac, infoRaws);
    }

    public List<Object[]> toClientNormalizedRow() {
        return mapDeviceToClient.values()
                .stream().flatMap(Collection::stream)
                .map(ClientInfoRaw::toClientNormalized)
                .map(ClientNormalized::toRowMapper)
                .toList();
    }

    public List<Object[]> toClientIpNormalizedRow() {
        return mapDeviceToClient.values()
                .stream().flatMap(Collection::stream)
                .filter(ClientInfoRaw::checkHasIp)
                .map(ClientInfoRaw::toIpNormalized)
                .map(IpNormalized::toRowMapper)
                .toList();
    }

    public Map<String, ClientConnectionEvent> toClientConnectionEvents() {
        var list = new HashMap<String, ClientConnectionEvent>();

        for (var macClient : mapDeviceToClient.entrySet()) {
            var deviceMac = macClient.getKey();
            
            // ignore corrupted records
            if (deviceMac == null || deviceMac.isBlank())
            	continue;
            
            for (var client : macClient.getValue()) if (client.checkMacIp()) {
                for (var wlan : wlanInfoRaw)
                    if (wlan.hasAttachedToClient(client)) {
                        var clientNorm = client.toClientNormalized();
                        var clientIp = client.toIpNormalized();
                        var iFaceNorm = InterfaceNormalized.builder()
                                .routerMac(extractDeviceMac(deviceMac))
                                .wlanName(wlan.createFullSSID())
                                .build();
                        var conn = new ClientConnectionEvent(
                                extractDateISO(),
                                extractTimeInt(),
                                clientNorm,
                                clientIp,
                                iFaceNorm);
                        list.put(conn.extractStateForBatchJob(), conn);
                    }
            }
        }

        // return a map with client state string and its connect event
        return list;
    }

    // to wlan mac to normalize
    // need mac, wlan name (iface name)
    // iface phys name
    // but have ap mac map to client
    // but there is no map from mac to ssid name
    ///  yes, it is possible
    /// from mac device -> client -> band, interface type -> ap name
    public List<Object[]> toIfaceNormalizedRow() {
        var listInterfaceNorm = new ArrayList<InterfaceNormalized>();

        for (var macClient : mapDeviceToClient.entrySet()) {
            var deviceMac = macClient.getKey();
            
            // ignore corrupted records
            if (deviceMac == null || deviceMac.isBlank())
            	continue;
            
            for (var client : macClient.getValue()) {
                for (var wlan : wlanInfoRaw)
                    if (wlan.hasAttachedToClient(client)) {
                        listInterfaceNorm.add(
                            InterfaceNormalized.builder()
                                    .routerMac(extractDeviceMac(deviceMac))
                                    .wlanName(wlan.createFullSSID())
                                    .build()
                        );
                        break;
                    }
            }
        }

        return listInterfaceNorm.stream()
                .map(InterfaceNormalized::toRowMapper)
                .toList();
    }
}
