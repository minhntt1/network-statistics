package com.home.network.statistic.poller.rfc1213.in.job;

import com.home.network.statistic.poller.rfc1213.in.service.SnmpPollingService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class SnmpPollingJob implements Job {
    @Autowired(required = false)
    private SnmpPollingService snmpPollingService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        snmpPollingService.pollIfTraffic();
    }
}
