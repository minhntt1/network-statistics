package com.home.network.statistic.poller.tplink.deco.etl;

import com.home.network.statistic.common.model.ListSqlQuery;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoEntity;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApRebootCnt;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class DeviceInfoServiceImpl implements DeviceInfoService {
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationContext applicationContext;
    private final ListSqlQuery listSqlQuery;

    public DeviceInfoServiceImpl(
            @Qualifier("appJdbcTemplate") JdbcTemplate jdbcTemplate,
            ApplicationContext applicationContext,
            @Qualifier("tpLinkDecoDeviceQuery") ListSqlQuery listSqlQuery) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationContext = applicationContext;
        this.listSqlQuery = listSqlQuery;
    }

    public void addUndefinedIpDim() {
        jdbcTemplate.execute(listSqlQuery.getQueryValue("addUndefinedIpDim"));
    }

    public void insertIntoDateDim() {
        jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoDateDim"));
    }

    public void insertIntoTimeDim() {
        jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoTimeDim"));
    }

    public void insertIntoWeekDateDim() { jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoWeekDateDim"));}

    @Transactional(value = "appTx")
    public void insertIntoApDimIpDim() {
        // first, select from stg table
        // have to add stg query

        var listDevices = new ArrayList<Object[]>();

        var listIps = new ArrayList<Object[]>();

        try (var stream = jdbcTemplate
                .queryForStream(listSqlQuery.getQueryValue("getAllStaging"), new BeanPropertyRowMapper<>(DeviceInfoEntity.class))) {
            for (var it = stream.iterator(); it.hasNext(); ) {
                var device = it.next();

                listDevices.addAll(device.toInsertableAps());

                listIps.addAll(device.toInsertableIps());
            }
        }

        // after list is available, add to db (run queries)
        if (!listDevices.isEmpty()) {
            // create temp ap dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempTableForApDimNormalize"));

            // insert into temp ap dim
            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertIntoTempTableForApDimNormalize"), listDevices);

            // normalize from temp to actual ap dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoApDimNormalize"));

            // drop temp ap dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempTableForApDimNormalize"));
        }

        if (!listIps.isEmpty()) {
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempTableForIpDimNormalize"));

            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertIntoTempTableForIpDimNormalize"), listIps);

            jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoIpDim"));

            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempTableForIpDimNormalize"));
        }

    }

    // normalize device done,
    // now have to count reboot for ap
    @Transactional(value = "appTx")
    public void summarizeData(JobExecutionContext jec) {
        log.info("start summarizing");

        // map to store temp reboot count for each ap to insert into db
        var mapRebootCnt = new HashMap<ApRebootCnt, ApRebootCnt>();
        var mapState = jec.getJobDetail().getJobDataMap();

        boolean noData = true;

        try (var stream = jdbcTemplate
                .queryForStream(listSqlQuery.getQueryValue("getAllStaging"), new BeanPropertyRowMapper<>(DeviceInfoEntity.class))) {
            for (var it = stream.iterator(); it.hasNext(); ) {
                noData = false;

                var dev = it.next();
                var devToMapEmptyRebootCnt = dev.toApRebootCnts();

                // loop thru statedb
                for (var stateIt = mapState.entrySet().iterator(); stateIt.hasNext(); ) {
                    var state = stateIt.next();
                    var stateKey = state.getKey();

                    // if something in current state map not appear in stream, remove from state map
                    if (!devToMapEmptyRebootCnt.containsKey(stateKey)) {
                        stateIt.remove();
                    }
                }

                // loop thru list of current stream
                for (var rebootCntEntry : devToMapEmptyRebootCnt.entrySet()) {
                    var rebootCntKey = rebootCntEntry.getKey();
                    var rebootCnt = rebootCntEntry.getValue();
                    
                    // add reboot cnt record with value 0 to map
                    mapRebootCnt.putIfAbsent(rebootCnt, rebootCnt);
                    
                    if (!mapState.containsKey(rebootCntKey)) {
                        // reboot ~ no data in state db (offline) + the appearance of data in stream (online)
                        // get from map and increase rebootcnt
                    	mapRebootCnt.get(rebootCnt).increaseRebootCnt();
                        // null value because a key is enough to indicate the online status of router
                        mapState.put(rebootCntKey, "");
                    }
                }
            }
        }

        // incase there is no data at all, remove
        if (noData) {
            for (var it = mapState.entrySet().iterator(); it.hasNext(); ) {
                var key = it.next().getKey();
                if (ApRebootCnt.hasPrefixKeyRebootState(key))
                    it.remove();
            }
        }

        var insertableRows = ApRebootCnt.toListInsertable(mapRebootCnt.values());

        if (!insertableRows.isEmpty()) {
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempForApRebootCnt"));

            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertTmpFactToApRebootCnt"), insertableRows);

            jdbcTemplate.execute(listSqlQuery.getQueryValue("updateApRebootCnt"));

            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempForApRebootCnt"));
        }

        log.info("end summarizing");
    }

    public void cleanUpBatch() {
        // copy all staging data to archive
        jdbcTemplate.execute(listSqlQuery.getQueryValue("copyStgToArchive"));

        // drop current staging (batch processing table)
        jdbcTemplate.execute(listSqlQuery.getQueryValue("dropCurrentStg"));

        // create new batch process table
        jdbcTemplate.execute(listSqlQuery.getQueryValue("createNewStg"));

        // flip new batch process table with ingest table for further processing
        jdbcTemplate.execute(listSqlQuery.getQueryValue("moveStgToIngest"));
    }

    @Override
    @Timed(value = "tplink.deco.etl.device_info")
    public void start(JobExecutionContext context) {
        log.info("start");

        // normalize
        addUndefinedIpDim();
        insertIntoDateDim();
        insertIntoTimeDim();
        insertIntoApDimIpDim();
        insertIntoWeekDateDim();

        applicationContext.getBean(DeviceInfoServiceImpl.class).summarizeData(context);

        // clean up batch
        cleanUpBatch();

        log.info("end");
    }
}
