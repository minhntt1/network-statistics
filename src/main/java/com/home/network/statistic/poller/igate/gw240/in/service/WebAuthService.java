package com.home.network.statistic.poller.igate.gw240.in.service;

import com.home.network.statistic.poller.igate.gw240.in.WebRequestInfo;
import com.home.network.statistic.poller.igate.gw240.in.IngestionCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * service to authenticate with web ui
 * and return auth info
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class WebAuthService {
    private final RestClient restClient;

    public WebRequestInfo obtainInfo(IngestionCredentials credentials) {
        // implement logic to authenticate with router if it going to be complicated in long term

        // try to authenticate to web to get session id
        var tmpAuth = credentials.obtainAuthInfo();

        int tries = 3;
        while (tries-- > 0) {
            // make 3 request to index get rid of bug in router web server
            ResponseEntity<Void> bodilessEntity = restClient.get().uri(tmpAuth.obtainHostUrlIndex()).headers(tmpAuth::addHeader).retrieve().toBodilessEntity();

            // cookie header
            String cookieHeader = Optional.ofNullable(bodilessEntity.getHeaders().get(WebRequestInfo.SET_COOKIE_HEADER))
                    .filter(list -> !list.isEmpty()).map(List::getFirst).orElse(null);

            // after first req fail, update auth token to use new session in new response until request success
            if (bodilessEntity.getStatusCode().is4xxClientError()) {
                log.error("getting auth header failed, retrying");
                if (cookieHeader != null)
                    tmpAuth.updateCookieSessionId(cookieHeader);
            } else if (bodilessEntity.getStatusCode().is2xxSuccessful()) {
                log.info("getting auth header succeeded");
                break;
            }
        }

        return tmpAuth;
    }
}
