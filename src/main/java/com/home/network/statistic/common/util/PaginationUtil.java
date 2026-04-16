package com.home.network.statistic.common.util;

public class PaginationUtil {
	public static long[] getDefaultPaginationInfo(long maxResult, int page, int limit) {
		return getPaginationInfo(maxResult, page, limit, 5);
	}
	
	public static long[] getPaginationInfo(long maxResult, int page, int limitResult, int limitPage) {
		long pageCnt = maxResult / limitResult + (maxResult % limitResult > 0 ? 1 : 0);
		long size = Math.min(limitPage, pageCnt);
		var res = new long[(int)size];
		long lpage = size / 2 + size % 2, rpage = pageCnt - size / 2;
		long cp;
		
		if (page < lpage) {
			cp = 1;
			for (int i = 0; i < size; ++i)
				res[i] = cp++;
		} else if (page < rpage) {
			cp = page - lpage;
			for (int i = 0; i < size; ++i)
				res[i] = ++cp;
		} else {
			cp = pageCnt;
			for (int i = (int)size - 1; i >= 0; --i)
				res[i] = cp--;
		}
		
		return res;
	}
}
