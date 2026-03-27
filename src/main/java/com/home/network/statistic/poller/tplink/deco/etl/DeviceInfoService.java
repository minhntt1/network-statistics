package com.home.network.statistic.poller.tplink.deco.etl;

import org.quartz.JobExecutionContext;

public interface DeviceInfoService {
    void start(JobExecutionContext context);
}
