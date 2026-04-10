package com.home.network.statistic.poller.authentication;


import java.util.List;

public interface AuthDataService {
    void upsertAuthData(AuthData authData);

    AuthData findById(Integer id);

    long countResultAll();

    List<AuthData> findAll(Integer page, Integer limit);

    boolean deleteById(Integer id);
}
