package com.home.network.statistic.poller.tplink.deco.in;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class FetchTelemetryJob implements Job {
    @Autowired(required = false)
    private FetchTelemetryService fetchTelemetryService;

    @Override
    public void execute(JobExecutionContext context) {
        fetchTelemetryService.fetchClientAndWlanInfo();
    }
}
