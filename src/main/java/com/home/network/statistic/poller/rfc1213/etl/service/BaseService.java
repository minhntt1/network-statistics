package com.home.network.statistic.poller.rfc1213.etl.service;

import org.quartz.JobExecutionContext;

public interface BaseService {
    void start(JobExecutionContext context);
}
