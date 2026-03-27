package com.home.network.statistic.poller.tplink.deco.etl;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class DeviceInfoJob implements Job {
    @Autowired(required = false)
    private DeviceInfoService deviceInfoService;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        deviceInfoService.start(context);
    }
}
