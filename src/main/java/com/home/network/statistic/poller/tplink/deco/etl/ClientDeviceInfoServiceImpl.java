package com.home.network.statistic.poller.tplink.deco.etl;

import com.home.network.statistic.common.model.ListSqlQuery;
import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoEntity;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientConnectionEvent;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientHourlyTraffic;
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
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@Profile({"dev-executor","prd-executor"})
public class ClientDeviceInfoServiceImpl implements ClientDeviceInfoService {
    private final JdbcTemplate jdbcTemplate;
    private final ApplicationContext applicationContext;
    private final ListSqlQuery listSqlQuery;

    public ClientDeviceInfoServiceImpl(
            @Qualifier("appJdbcTemplate") JdbcTemplate jdbcTemplate,
            ApplicationContext applicationContext,
            @Qualifier("tpLinkDecoQuery") ListSqlQuery listSqlQuery) {
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

    public void insertIntoTimeDimHourNorm() {jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoTimeDimHourNorm"));}

    // put inside one transaction to stay in one connection
    @Transactional(value = "appTx")
    public void insertIntoDeviceDimGwIfaceDimIpDim() {
        log.info("normalize to device, gwiface and ip dim");

        List<Object[]> devices = new ArrayList<>();

        List<Object[]> gwIface = new ArrayList<>();

        List<Object[]> ips = new ArrayList<>();

        // select from stfg ingest
        try (var stream = jdbcTemplate
                .queryForStream(listSqlQuery.getQueryValue("getAllStaging"), new BeanPropertyRowMapper<>(ClientDeviceInfoEntity.class))) {
            for (var it = stream.iterator(); it.hasNext(); ) {
                // convert json string to object
                var obj = it.next().toClientDeviceInfoRaw();

                // should put output model in raw or etl package??
                // put in out because they are highly-coupled
                // move all models related to normalized tables and dimension tables to a separated pacakge/acutally,
                // however, this is not needed, because some simple tables don't have to map to different models
                devices.addAll(obj.toClientNormalizedRow());

                gwIface.addAll(obj.toIfaceNormalizedRow());

                ips.addAll(obj.toClientIpNormalizedRow());
            }
        }

        // create temp table to hold temp data and clean up it
        if (!devices.isEmpty()) {
            // create temp device dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempTableForDeviceDimNormalize"));

            // insert into temp device dim
            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertIntoTempTableForDeviceDimNormalize"), devices);

            // normalize from temp to actual device dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoDeviceDim"));

            // drop temp device dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempTableForDeviceDimNormalize"));
        }

        if (!gwIface.isEmpty()) {
            // create temp gw iface dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempTableForGwIfaceDimNormalize"));

            // insert into temp gw iface dim
            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertIntoTempTableForGwIfaceDimNormalize"), gwIface);

            // normalize from temp to actual gw_iface_dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoGwIfaceDim"));

            // drop temp gw iface dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempTableForGwIfaceDimNormalize"));
        }

        if (!ips.isEmpty()) {
            // create temp gw iface dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempTableForIpDimNormalize"));

            // insert into temp gw iface dim
            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertIntoTempTableForIpDimNormalize"), ips);

            // normalize from temp to actual gw_iface_dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("insertIntoIpDim"));

            // drop temp gw iface dim
            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempTableForIpDimNormalize"));
        }

        log.info("end normalize to device, gwiface and ip dim");
    }

    private void cleanUpBatch() {
        // copy all staging data to archive
        jdbcTemplate.execute(listSqlQuery.getQueryValue("copyStgToArchive"));

        // drop current staging (batch processing table)
        jdbcTemplate.execute(listSqlQuery.getQueryValue("dropCurrentStg"));

        // create new batch process table
        jdbcTemplate.execute(listSqlQuery.getQueryValue("createNewStg"));

        // flip new batch process table with ingest table for further processing
        jdbcTemplate.execute(listSqlQuery.getQueryValue("moveStgToIngest"));
    }

    @Transactional(value = "appTx")
    public void summarizeData(JobExecutionContext context) {
        log.info("start summarizing data");

        // current job map
        var stateMap = context.getJobDetail().getJobDataMap();

        // list of connect/disconnect events
        var events = new ArrayList<ClientConnectionEvent>();

        // define list containing clients with no activity
        var inactive = new HashSet<>(stateMap.keySet());

        // define obj to hold client states
        var clientStates = new ClientStateProcessor(stateMap, inactive);

        try (var stream = jdbcTemplate.queryForStream(listSqlQuery.getQueryValue("getAllStaging"), new BeanPropertyRowMapper<>(ClientDeviceInfoEntity.class))) {
            events.addAll(clientStates.calcClientConnectionEvents(stream));
        }

        events.addAll(clientStates.calcInactiveClientEvents());

        var insertableEvents = ClientConnectionEvent.toObjectForInsert(events);

        if (!insertableEvents.isEmpty()) {
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempForUpdateFactTableDeviceWlanConnections"));

            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertTmpFactToTempTableUpdateFactTableDeviceWlanConnections"), insertableEvents);

            jdbcTemplate.execute(listSqlQuery.getQueryValue("updateFactTableDeviceWlanConnections"));

            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempForUpdateFactTableDeviceWlanConnections"));
        }

        log.info("end summarizing data");
    }

    @Transactional(value = "appTx")
    public void summarizeHourlyTraffic(JobExecutionContext context) {
        log.info("start summarizing hourly traffic");

        // from client raw data, client normalized
        List<ClientHourlyTraffic> clientTraffic = new ArrayList<>();

        // fetch raw row by row
        try (var stream = jdbcTemplate.queryForStream(listSqlQuery.getQueryValue("getAllStaging"), new BeanPropertyRowMapper<>(ClientDeviceInfoEntity.class))) {
            for (var it = stream.iterator(); it.hasNext(); ) {
                var obj = it.next().toClientDeviceInfoRaw();
                clientTraffic.addAll(obj.toClientHourlyTraffics());
            }
        }

        var insertableRows = ClientHourlyTraffic.reduceRecordsToRows(clientTraffic);

        // have records, not upsert into db
        // define list sql query
        if (!insertableRows.isEmpty()) {
            jdbcTemplate.execute(listSqlQuery.getQueryValue("createTempForUpdateFactTableDeviceTrafficByHour"));

            jdbcTemplate.batchUpdate(listSqlQuery.getQueryValue("insertTmpFactToTempTableUpdateFactTableDeviceTrafficByHour"), insertableRows);

            jdbcTemplate.execute(listSqlQuery.getQueryValue("updateFactTableDeviceTrafficByHour"));

            jdbcTemplate.execute(listSqlQuery.getQueryValue("dropTempForUpdateFactTableDeviceTrafficByHour"));
        }

        log.info("end summarizing hourly traffic");
    }

    @Override
    @Timed(value = "tplink.deco.etl.client_device_info")
    public void start(JobExecutionContext context) {
        log.info("start");

        // normalize
        addUndefinedIpDim();
        insertIntoDateDim();
        insertIntoTimeDim();
        insertIntoTimeDimHourNorm();
        applicationContext.getBean(ClientDeviceInfoServiceImpl.class).insertIntoDeviceDimGwIfaceDimIpDim();

        // summarize
        // if multiple jobs share a same table, put all jobs in one class
        applicationContext.getBean(ClientDeviceInfoServiceImpl.class).summarizeData(context);
        applicationContext.getBean(ClientDeviceInfoServiceImpl.class).summarizeHourlyTraffic(context);

        // clean up batch
        cleanUpBatch();

        log.info("end");
    }
}
