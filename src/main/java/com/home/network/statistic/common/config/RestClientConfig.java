package com.home.network.statistic.common.config;

import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
public class RestClientConfig {
    @Bean
    @SneakyThrows
    HttpClient httpClient() {
        X509TrustManager trustAll = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{ trustAll }, new SecureRandom());

        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");

        return HttpClient.newBuilder()
                .sslContext(ctx)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    RestClient insecureRestClient(HttpClient client) {
        var factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.of(10, ChronoUnit.SECONDS));
        return RestClient.builder()
                .requestFactory(factory)
                .defaultStatusHandler(HttpStatusCode::isError, (req, resp) -> {})
                .build();
    }
}
