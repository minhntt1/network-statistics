package com.home.network.statistic.common.util;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class PaginationUtilTest {
	@Test
	void testRight() {
		var res = PaginationUtil.getPaginationInfo(101, 20, 5, 4);
		System.out.println(Arrays.toString(res));
	}
	@Test
	void testLeft() {
		var res = PaginationUtil.getPaginationInfo(101, 3, 5, 4);
		System.out.println(Arrays.toString(res));
	}
	@Test
	void testLeftOdd() {
		var res = PaginationUtil.getPaginationInfo(101, 4, 5, 5);
		System.out.println(Arrays.toString(res));
	}
	@Test
	void testRightOdd() {
		var res = PaginationUtil.getPaginationInfo(101, 20, 5, 5);
		System.out.println(Arrays.toString(res));
	}
}
