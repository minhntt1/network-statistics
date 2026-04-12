package com.home.network.statistic.poller.tplink.deco.in;

import java.net.http.HttpResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WebResponseExtra {
	private HttpResponse response;
	
	public boolean hasErrorStatus() {
		return response.statusCode() != 200;
	}
	
	public boolean hasErrorStringBody() {
		return ((String)response.body()).isBlank();
	}
	
	public boolean hasErrorStringStatus() {
		return hasErrorStatus() || hasErrorStringBody();
	}
	
	public String getStringBody() {
		return (String)response.body();
	}
}
