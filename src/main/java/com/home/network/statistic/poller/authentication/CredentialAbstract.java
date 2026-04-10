package com.home.network.statistic.poller.authentication;

import com.home.network.statistic.common.util.JsonUtil;

public interface CredentialAbstract {

    String serializeToJson();

    static  CredentialAbstract fromJson(String json, Class<? extends CredentialAbstract> tClass) {
        return JsonUtil.fromJson(json, tClass);
    }
}
