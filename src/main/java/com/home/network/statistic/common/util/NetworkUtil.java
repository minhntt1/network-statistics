package com.home.network.statistic.common.util;

public class NetworkUtil {
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
