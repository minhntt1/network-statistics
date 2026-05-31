package com.home.network.statistic.common.util;

public class NetworkUtil {
    public static String convertIpIntToString(Integer ip) {
        String ipStr = "";

        ipStr += (ip >> 24) & 0xff;
        ipStr += ".";
        ipStr += (ip >> 16) & 0xff;
        ipStr += ".";
        ipStr += (ip >> 8) & 0xff;
        ipStr += ".";
        ipStr += ip & 0xff;

        return ipStr;
    }

    public static String convertMacLongToString(Long mac) {
        if (mac == null) return "";
        return Long.toHexString(mac).formatted("%12s").replace(' ', '0');
    }

    public static Long convertMacStringToLong(String mac) {
        var tmpMac = mac;
        tmpMac = tmpMac.replace(".", "");
        tmpMac = tmpMac.replace("-", "");
        tmpMac = tmpMac.replace(":", "");
        tmpMac = tmpMac.isBlank() ? "0" : tmpMac;
        return Long.parseLong(tmpMac, 16);
    }

    public static int convertIpv4StringToInt(String ip) {
        String[] parts = ip.split("\\.");
        int result = 0;
        for (String part : parts) {
            result = result << 8 | Integer.parseInt(part);
        }
        return result;
    }
}
