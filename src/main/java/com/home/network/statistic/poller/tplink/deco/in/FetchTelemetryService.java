package com.home.network.statistic.poller.tplink.deco.in;

import com.home.network.statistic.poller.authentication.AuthDataProcessor;

public interface FetchTelemetryService extends AuthDataProcessor<WebUiCredentials> {
    void fetchClientAndWlanInfo();
}
