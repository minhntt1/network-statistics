package com.home.network.statistic.poller.authentication;

import java.util.List;
import java.util.Optional;

public interface AuthDataRepo {
    List<AuthData> findByClass(String clazz);

	List<AuthData> findByClassForUpdate(String clazz);
	
    void upsert(AuthData authData);

    Optional<AuthData> findById(Integer id);

    long countResultAll();

    List<AuthData> findAll(Integer page, Integer limit);

    boolean deleteById(Integer id);
}
