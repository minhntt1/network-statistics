package com.home.network.statistic.poller.tplink.deco.etl;

import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoEntity;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientConnectionEvent;
import org.quartz.JobDataMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ClientStateProcessor {
    private JobDataMap historyStates;
    private Set<String> inactiveClients;
    private boolean hasStreamRawData;

    public ClientStateProcessor(JobDataMap historyStates, Set<String> inactiveClients) {
        this.historyStates = historyStates;
        this.inactiveClients = inactiveClients;
    }

    public List<ClientConnectionEvent> calcClientConnectionEvents(Stream<ClientDeviceInfoEntity> currRawData) {
        var events = new ArrayList<ClientConnectionEvent>();

        for (var it = currRawData.iterator(); it.hasNext(); ) {
            hasStreamRawData = true;

            // process ingest
            var currentRaw = it.next();

            // current poll time of record in current batch
            LocalDateTime pollTime = currentRaw.getPollTime();

            // convert raw to object
            var rawObj = currentRaw.toClientDeviceInfoRaw();

            // extract list connect event mapped by keys for batch job
            var currConnectEvents = rawObj.toClientConnectionEvents();

            // loop thru current map from ap
            for (var connectEvent : currConnectEvents.entrySet()) {
                var currStateKey = connectEvent.getKey();
                var currStateVal = connectEvent.getValue();

                // remove client from the list of inactive
                inactiveClients.remove(currStateKey);

                // if db map does not contain current event
                if (!historyStates.containsKey(currStateKey)) {
                    // add connect event
                    events.add(currStateVal);
                    // add state to map
                    historyStates.put(currStateKey, currStateVal.toJson());
                } else {
                    var prevStateEvent = ClientConnectionEvent.from(historyStates.get(currStateKey).toString());

                    // if contains prev state and different from current
                    if (prevStateEvent.checkDiffEvent(currStateVal)) {
                        // disconnect right now
                        prevStateEvent.disconnect(pollTime);

                        // add disconn event
                        events.add(prevStateEvent);

                        // add connect event
                        events.add(currStateVal);

                        // update hashmap state
                        historyStates.put(currStateKey, currStateVal.toJson());
                    }
                }
            }
        }

        return events;
    }

    public List<ClientConnectionEvent> calcInactiveClientEvents() {
        var events = new ArrayList<ClientConnectionEvent>();

        // current date time
        var currentDt = LocalDateTime.now();
        for (var it = historyStates.entrySet().iterator(); it.hasNext(); ) {
            var state = it.next();
            var key = state.getKey();
            var val = ClientConnectionEvent.from(state.getValue().toString());

            // if batch has data and state key in inactive set, add disconnect event
            if (hasStreamRawData && inactiveClients.contains(key)) {
                val.disconnect(currentDt);
                events.add(val);
            }

            // in case there is no data at all in current batch
            // in this case, all clients disconnected or aps are down
            // but it can be case when poller code has issue and no data in db
            // because there is no data at all -> remove all client connection state
            // and don't add disconnect event
            if (ClientConnectionEvent.checkIsStateKeyConnect(key)) {
                it.remove();
            }
        }

        return events;
    }
}
