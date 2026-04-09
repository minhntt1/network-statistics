package com.home.network.statistic.poller.authentication;

import java.util.List;

public interface AuthDataRepo {
    void upsert(AuthData authData);

    List<AuthData> findByClass(String clazz);
}
