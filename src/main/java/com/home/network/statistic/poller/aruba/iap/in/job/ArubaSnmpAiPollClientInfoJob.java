package com.home.network.statistic.poller.aruba.iap.in.job;

import com.home.network.statistic.poller.aruba.iap.in.service.ArubaSnmpAiPollClientService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ArubaSnmpAiPollClientInfoJob implements Job {
    @Autowired(required = false)
    private ArubaSnmpAiPollClientService service;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        service.pollClientInfo();
    }
}
