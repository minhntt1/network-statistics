package com.home.network.statistic.poller.tplink.deco.etl;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class ClientDeviceJob implements Job {
    @Autowired(required = false)
    private ClientDeviceInfoService clientDeviceInfoService;


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        clientDeviceInfoService.start(context);
    }
}
