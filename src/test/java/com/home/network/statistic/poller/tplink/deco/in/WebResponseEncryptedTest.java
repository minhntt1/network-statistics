package com.home.network.statistic.poller.tplink.deco.in;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

@Slf4j
public class WebResponseEncryptedTest {
	@Test
	public void testFromJsonNull() {
		try {
			WebResponseEncrypted.from(null);
		} catch (Exception e) {
			// TODO: handle exception
			log.info("", e);
		}

	}
	@Test
	public void testFromJsonEmpty() {
		// empty body also causes a jackson exception
		Assertions.assertThrows(MismatchedInputException.class, () -> WebResponseEncrypted.from(""));
	}
}
