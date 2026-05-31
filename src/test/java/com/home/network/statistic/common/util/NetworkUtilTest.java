package com.home.network.statistic.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class NetworkUtilTest {
    @Test
    void testConvertIpIntToString() {
        log.info("{}", NetworkUtil.convertIpIntToString(-1062706170));
    }
}
