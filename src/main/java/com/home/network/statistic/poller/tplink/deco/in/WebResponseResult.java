package com.home.network.statistic.poller.tplink.deco.in;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.home.network.statistic.poller.tplink.deco.out.ClientInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.WlanInfoRaw;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebResponseResult {
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private List<String> password;

    @JsonProperty("key")
    private List<String> key;

    @JsonProperty("seq")
    private Integer seq;

    @JsonProperty("stok")
    private String stok;

    @JsonProperty("band5_1")
    private Object band51;

    @JsonProperty("iot")
    private Object iot;

    @JsonProperty("band2_4")
    private Object band24;

    @JsonProperty("ipv4")
    private Object ipv4;

    @JsonProperty("ipv6")
    private Object ipv6;

    @JsonProperty("lan_ip")
    private Object lanIp;

    @JsonProperty("mac_addr")
    private String macAddress;

    @JsonProperty("device_list")
    private Object deviceList;

    @JsonProperty("client_list")
    private Object clientList;

    public List<String> extractDeviceMacs() {
        var listDevice = (List<Map<String, String>>) deviceList;
        var macs = new ArrayList<String>();

        for (var dev : listDevice)
            macs.add(dev.get("mac"));

        return macs;
    }

    public static List<WlanInfoRaw> mapWlanTypesToObject(Map<String, Map<String, Object>> mapWlanType, String bandName) {
        var listWlanInfo = new ArrayList<WlanInfoRaw>();

        for (var wlanMode : mapWlanType.entrySet()) {
            var wlanRaw = new WlanInfoRaw();
            wlanRaw.setWlanType(bandName);
            wlanRaw.setWlanMode(wlanMode.getKey());

            for (var propWlan : wlanMode.getValue().entrySet()) {
                if ("channel".equals(propWlan.getKey()))
                    wlanRaw.setChannel(propWlan.getValue().toString());
                else if ("ssid".equals(propWlan.getKey()))
                    wlanRaw.setSsid(propWlan.getValue().toString());
                else if ("password".equals(propWlan.getKey()))
                    wlanRaw.setPassword(propWlan.getValue().toString());
                else if ("enable".equals(propWlan.getKey()))
                    wlanRaw.setEnable((Boolean) propWlan.getValue());
                else if ("enable_2g".equals(propWlan.getKey()))
                    wlanRaw.setEnable2g((Boolean) propWlan.getValue());
                else if ("enable_5g".equals(propWlan.getKey()))
                    wlanRaw.setEnable5g((Boolean) propWlan.getValue());
            }

            listWlanInfo.add(wlanRaw);
        }

        return listWlanInfo;
    }

    public List<WlanInfoRaw> toWlanInfoRaws() {
        var listWlanInfo = new ArrayList<WlanInfoRaw>();

        var mapWlanType = (Map<String, Map<String, Object>>) band51;
        listWlanInfo.addAll(mapWlanTypesToObject(mapWlanType, "band5_1"));

        mapWlanType = (Map<String, Map<String, Object>>) iot;
        listWlanInfo.addAll(mapWlanTypesToObject(mapWlanType, "iot"));

        mapWlanType = (Map<String, Map<String, Object>>) band24;
        listWlanInfo.addAll(mapWlanTypesToObject(mapWlanType, "band2_4"));

        return listWlanInfo;
    }

    public List<ClientInfoRaw> toClientInfoRaws() {
        var listClientInfo = new ArrayList<ClientInfoRaw>();
        var mapClientList = (List<Map<String, Object>>)clientList;

        for (var client : mapClientList) {
            var clientRaw = new ClientInfoRaw();
            for (var clientProp : client.entrySet()) {
                if ("ip".equals(clientProp.getKey()))
                    clientRaw.setIp(clientProp.getValue().toString());
                else if ("name".equals(clientProp.getKey()))
                    clientRaw.setName(clientProp.getValue().toString());
                else if ("interface".equals(clientProp.getKey()))
                    clientRaw.setClientInterface(clientProp.getValue().toString());
                else if ("mac".equals(clientProp.getKey()))
                    clientRaw.setMac(clientProp.getValue().toString());
                else if ("client_type".equals(clientProp.getKey()))
                    clientRaw.setClientType(clientProp.getValue().toString());
                else if ("connection_type".equals(clientProp.getKey()))
                    clientRaw.setConnectionType(clientProp.getValue().toString());
                else if ("up_speed".equals(clientProp.getKey()))
                    clientRaw.setUpSpeed((long)(int)clientProp.getValue());     // jackson by deafault number in json casted to int
                else if ("down_speed".equals(clientProp.getKey()))
                    clientRaw.setDownSpeed((long)(int)clientProp.getValue());
            }
            listClientInfo.add(clientRaw);
        }

        return listClientInfo;
    }

    public List<DeviceInfoRaw> toDeviceInfoRaws(LocalDateTime pollTime) {
        var listDeviceInfo = new ArrayList<DeviceInfoRaw>();
        var mapDeviceList = (List<Map<String, Object>>)deviceList;

        for (var dev : mapDeviceList) {
            var devRaw = new DeviceInfoRaw();
            devRaw.setPollTime(pollTime);

            for (var prop : dev.entrySet()) {
                if ("device_model".equals(prop.getKey()))
                    devRaw.setDeviceModel(prop.getValue().toString());
                else if ("device_ip".equals(prop.getKey()))
                    devRaw.setDeviceIp(prop.getValue().toString());
                else if ("nickname".equals(prop.getKey()))
                    devRaw.setNickName(prop.getValue().toString());
                else if ("mac".equals(prop.getKey()))
                    devRaw.setDeviceMac(prop.getValue().toString());
                else if ("inet_status".equals(prop.getKey()))
                    devRaw.setInetStatus(prop.getValue().toString());
                else if ("group_status".equals(prop.getKey()))
                    devRaw.setGroupStatus(prop.getValue().toString());
            }

            listDeviceInfo.add(devRaw);
        }

        return listDeviceInfo;
    }
}


