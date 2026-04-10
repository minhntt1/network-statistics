package com.home.network.statistic.poller.aruba.iap.in.job;

import com.home.network.statistic.poller.aruba.iap.in.service.ArubaSnmpAiPollWlanTrafficService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ArubaSnmpAiPollWlanTrafficJob implements Job {
    @Autowired(required = false)
    private ArubaSnmpAiPollWlanTrafficService service;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        service.pollWlanTraffic();
    }
}
