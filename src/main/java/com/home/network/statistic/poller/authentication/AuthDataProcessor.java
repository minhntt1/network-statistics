package com.home.network.statistic.poller.authentication;

import org.slf4j.Logger;

import java.util.List;


public interface AuthDataProcessor<T extends CredentialAbstract> {
    default void fetchBulkAndProcess(Logger log, boolean updateBulk, Class<T> clazz) {
        log.info("start");

        // select for update, and
        AuthDataRepo authDataRepo = extractAuthDataRepo();
        List<AuthData> listAuthData;

        if (updateBulk) listAuthData = authDataRepo.findByClassForUpdate(clazz.getCanonicalName());
        else listAuthData = authDataRepo.findByClass(clazz.getCanonicalName());

        log.info("total records {}", listAuthData.size());

        for (var data : listAuthData) {
            try {
                log.info("handling data {}", data.getData());
                process(data);
            } catch (Exception e) {
                // catch exception to handle remaining data
                log.error("unexpected error during processing record", e);
            }
        }

        log.info("end");
    }

    void process(AuthData authData);

    AuthDataRepo extractAuthDataRepo();
}
