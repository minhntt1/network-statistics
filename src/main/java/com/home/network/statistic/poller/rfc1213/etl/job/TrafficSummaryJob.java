package com.home.network.statistic.poller.rfc1213.etl.job;

import com.home.network.statistic.poller.rfc1213.etl.service.BaseService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class TrafficSummaryJob implements Job {
    @Autowired(required = false)
    @Qualifier("trafficSummaryService")
    private BaseService baseService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        baseService.start(context);
    }
}
