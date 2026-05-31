package com.home.network.statistic.common.config;

import com.home.network.statistic.common.model.ListSqlQuery;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.object.SqlQuery;

import java.util.Properties;

@Configuration
public class SqlQueryConfig {
    @SneakyThrows
    @Bean(name = "apInfoQuery")
    ListSqlQuery getListApInfoQuery() {
        Properties props = new Properties();
        props.loadFromXML(new ClassPathResource("etl_queries/ap-info-query.xml").getInputStream());
        return new ListSqlQuery(props);
    }

    @SneakyThrows
    @Bean(name = "apWlanTrafficQuery")
    ListSqlQuery getListApWlanTrafficQuery() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("etl_queries/ap-wlan-traffic-query.xml").getInputStream());
        return new ListSqlQuery(properties);
    }

    @SneakyThrows
    @Bean(name = "clientInfoQuery")
    ListSqlQuery getListClientInfoQuery() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("etl_queries/client-info-query.xml").getInputStream());
        return new ListSqlQuery(properties);
    }

    @SneakyThrows
    @Bean(name = "rfc1213Query")
    ListSqlQuery getListRfc1213Query() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("etl_queries/rfc1213-query.xml").getInputStream());
        return new ListSqlQuery(properties);
    }

    @SneakyThrows
    @Bean(name = "igate240StatusWifiStation")
    ListSqlQuery getListIgate240StatusWifiStation() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("etl_queries/igate240-status-wifi-station.xml").getInputStream());
        return new ListSqlQuery(properties);
    }

    @SneakyThrows
    @Bean(name = "tpLinkDecoQuery")
    ListSqlQuery getListTpLinkDeco() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("etl_queries/tplink-deco-query.xml").getInputStream());
        return new ListSqlQuery(properties);
    }

    @SneakyThrows
    @Bean(name = "tpLinkDecoDeviceQuery")
    ListSqlQuery getListTpLinkDecoDeviceQuery() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("etl_queries/tplink-deco-device-query.xml").getInputStream());
        return new ListSqlQuery(properties);
    }

    @SneakyThrows
    @Bean(name = "webClientConnectionsQuery")
    ListSqlQuery getListWebClientConnectionsQuery() {
        Properties properties = new Properties();
        properties.loadFromXML(new ClassPathResource("web_queries/client_connections.xml").getInputStream());
        return new ListSqlQuery(properties);
    }
}

