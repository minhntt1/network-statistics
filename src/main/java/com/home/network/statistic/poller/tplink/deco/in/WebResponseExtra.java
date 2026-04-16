package com.home.network.statistic.poller.tplink.deco.in;

import java.net.http.HttpResponse;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WebResponseExtra {
	private HttpResponse response;
	
	public boolean checkErrorStatus() {
		return response.statusCode() != 200;
	}
	
	public boolean checkErrorStringBody() {
		return response.body() == null || ((String)response.body()).isBlank();
	}
	
	public boolean checkErrorStringStatus() {
		return checkErrorStatus() || checkErrorStringBody();
	}

	public String getStringBody() {
		return (String)response.body();
	}

	public WebResponseEncrypted toWebResponseEncrypted() {
		return WebResponseEncrypted.from(getStringBody());
	}

	public boolean hasErrorBody() {
		return checkErrorStringStatus() || toWebResponseEncrypted().checkInvalidData();
	}
}
