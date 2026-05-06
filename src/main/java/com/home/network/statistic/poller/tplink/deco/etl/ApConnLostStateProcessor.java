package com.home.network.statistic.poller.tplink.deco.etl;

import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoEntity;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApConnLostCnt;
import org.quartz.JobDataMap;

import java.util.*;
import java.util.stream.Stream;


public class ApConnLostStateProcessor {
    private final JobDataMap histStates;
    private final Map<ApConnLostCnt, ApConnLostCnt> currTmpLostCnt;
    private final Set<String> inactiveAps;

    public ApConnLostStateProcessor(JobDataMap histStates) {
        this.histStates = histStates;
        this.currTmpLostCnt = new HashMap<>();
        this.inactiveAps = new HashSet<>(histStates.keySet());
    }

    public void calcApConnLostCnt(Stream<DeviceInfoEntity> currentRaw) {
        for (var it = currentRaw.iterator(); it.hasNext(); ) {
            var dev = it.next();
            var devToMapEmptyConnLostCnt = dev.toApConnLost();
            var devToMapStkToState = dev.toMapStkToState();

            // loop thru current stream
            for (var connLostEntry : devToMapEmptyConnLostCnt.entrySet()) {
                var lostCntKey = connLostEntry.getKey();
                var lostCnt = connLostEntry.getValue();
                var currState = devToMapStkToState.get(lostCntKey);

                // remove from inactive list
                inactiveAps.remove(lostCntKey);

                // add lost cnt record with value 0 to map
                currTmpLostCnt.putIfAbsent(lostCnt, lostCnt);

                // if has prev state
                if (histStates.containsKey(lostCntKey)) {
                    var prevState = DeviceInfoRaw.from(histStates.get(lostCntKey).toString());

                    // if conn lost, add value to map cnt
                    if (currState.checkConnLost(prevState)) {
                        currTmpLostCnt.get(lostCnt).increaseLostCnt();
                    }
                }

                // put to state map
                histStates.put(lostCntKey, currState.toJson());
            }
        }
    }

    public Collection<ApConnLostCnt> toApConnLostCnts() {
        return currTmpLostCnt.values();
    }

    public void cleanOldHistStatesNoData() {
        for (var it = histStates.entrySet().iterator(); it.hasNext(); ) {
            var key = it.next().getKey();

            // if there is state in inactive list, remove it from db state
            // in this case, ap is removed so theres no data
            // because only consider it disconnect when has data status = conn->disconn or online->offline, unlike client connection detection
            if (ApConnLostCnt.hasPrefixKeyRebootState(key) && inactiveAps.contains(key))
                it.remove();
        }
    }
}
