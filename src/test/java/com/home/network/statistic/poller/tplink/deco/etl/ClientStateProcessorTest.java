package com.home.network.statistic.poller.tplink.deco.etl;

import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoEntity;
import com.home.network.statistic.poller.tplink.deco.out.ClientDeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientConnectionEvent;
import com.home.network.statistic.poller.tplink.deco.out.etl.ClientNormalized;
import com.home.network.statistic.poller.tplink.deco.out.etl.InterfaceNormalized;
import com.home.network.statistic.poller.tplink.deco.out.etl.IpNormalized;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientStateProcessorTest {

    @Test
    void calcClientConnectionEvents_whenStateDoesNotExist_addsConnectEventAndPersistsState() {
        // Arrange
        JobDataMap historyStates = new JobDataMap();
        Set<String> inactiveClients = new HashSet<>(Set.of("state-1", "other-state"));
        ClientStateProcessor processor = new ClientStateProcessor(historyStates, inactiveClients);

        LocalDateTime pollTime = LocalDateTime.of(2026, 5, 8, 21, 0, 0);
        ClientConnectionEvent currEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");
        String currKey = "state-1";

        ClientDeviceInfoEntity entity = mock(ClientDeviceInfoEntity.class);
        ClientDeviceInfoRaw raw = mock(ClientDeviceInfoRaw.class);
        when(entity.getPollTime()).thenReturn(pollTime);
        when(entity.toClientDeviceInfoRaw()).thenReturn(raw);
        when(raw.toClientConnectionEvents()).thenReturn(Map.of(currKey, currEvent));

        // Act
        List<ClientConnectionEvent> actual = processor.calcClientConnectionEvents(Stream.of(entity));

        // Assert
        assertEquals(1, actual.size());
        assertSame(currEvent, actual.getFirst());
        assertTrue(historyStates.containsKey(currKey));
        assertEquals(currEvent.toJson(), historyStates.getString(currKey));
        assertFalse(inactiveClients.contains(currKey));
        assertTrue(inactiveClients.contains("other-state"));
    }

    @Test
    void calcClientConnectionEvents_whenStateExistsAndNoDiff_returnsNoEventsAndKeepsState() {
        // Arrange
        JobDataMap historyStates = new JobDataMap();
        Set<String> inactiveClients = new HashSet<>(Set.of("state-1"));
        ClientStateProcessor processor = new ClientStateProcessor(historyStates, inactiveClients);

        String currKey = "state-1";
        ClientConnectionEvent prevEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");
        historyStates.put(currKey, prevEvent.toJson());

        ClientConnectionEvent currEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");

        ClientDeviceInfoEntity entity = mock(ClientDeviceInfoEntity.class);
        ClientDeviceInfoRaw raw = mock(ClientDeviceInfoRaw.class);
        when(entity.getPollTime()).thenReturn(LocalDateTime.of(2026, 5, 8, 21, 5, 0));
        when(entity.toClientDeviceInfoRaw()).thenReturn(raw);
        when(raw.toClientConnectionEvents()).thenReturn(Map.of(currKey, currEvent));

        // Act
        List<ClientConnectionEvent> actual = processor.calcClientConnectionEvents(Stream.of(entity));

        // Assert
        assertTrue(actual.isEmpty());
        assertEquals(prevEvent.toJson(), historyStates.getString(currKey));
        assertFalse(inactiveClients.contains(currKey));
    }

    @Test
    void calcClientConnectionEvents_whenStateExistsAndDiff_addsDisconnectThenConnectAndUpdatesState() {
        // Arrange
        JobDataMap historyStates = new JobDataMap();
        Set<String> inactiveClients = new HashSet<>(Set.of("state-1"));
        ClientStateProcessor processor = new ClientStateProcessor(historyStates, inactiveClients);

        LocalDateTime pollTime = LocalDateTime.of(2026, 5, 8, 21, 10, 11);
        String currKey = "state-1";

        ClientConnectionEvent prevEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");
        historyStates.put(currKey, prevEvent.toJson());

        ClientConnectionEvent currEvent = sampleEvent("alice", 1001L, 3232235787L, "ssid-b");

        ClientDeviceInfoEntity entity = mock(ClientDeviceInfoEntity.class);
        ClientDeviceInfoRaw raw = mock(ClientDeviceInfoRaw.class);
        when(entity.getPollTime()).thenReturn(pollTime);
        when(entity.toClientDeviceInfoRaw()).thenReturn(raw);
        when(raw.toClientConnectionEvents()).thenReturn(Map.of(currKey, currEvent));

        // Act
        List<ClientConnectionEvent> actual = processor.calcClientConnectionEvents(Stream.of(entity));

        // Assert
        assertEquals(2, actual.size());

        ClientConnectionEvent disconnectEvent = actual.get(0);
        ClientConnectionEvent connectEvent = actual.get(1);

        assertEquals(2, disconnectEvent.getConnectStatus());
        assertEquals("2026-05-08", disconnectEvent.getDateConnect());
        assertEquals(21 * 3600 + 10 * 60 + 11, disconnectEvent.getTimeConnect());
        assertSame(currEvent, connectEvent);

        assertEquals(currEvent.toJson(), historyStates.getString(currKey));
        assertFalse(inactiveClients.contains(currKey));
    }

    @Test
    void calcClientConnectionEvents_whenStreamIsEmpty_returnsEmptyAndInactiveCleanupRemovesClientStatesWithoutEvents() {
        // Arrange
        JobDataMap historyStates = new JobDataMap();
        Set<String> inactiveClients = new HashSet<>();
        ClientStateProcessor processor = new ClientStateProcessor(historyStates, inactiveClients);

        ClientConnectionEvent stateEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");
        historyStates.put(stateEvent.extractStateForBatchJob(), stateEvent.toJson());
        historyStates.put("non-client-key", "preserved");

        // Act
        List<ClientConnectionEvent> connectionEvents = processor.calcClientConnectionEvents(Stream.empty());
        List<ClientConnectionEvent> inactiveEvents = processor.calcInactiveClientEvents();

        // Assert
        assertTrue(connectionEvents.isEmpty());
        assertTrue(inactiveEvents.isEmpty());
        assertFalse(historyStates.containsKey(stateEvent.extractStateForBatchJob()));
        assertTrue(historyStates.containsKey("non-client-key"));
    }

    @Test
    void calcInactiveClientEvents_whenStreamHasDataAndClientIsInactive_disconnectsAndRemovesState() {
        // Arrange
        JobDataMap historyStates = new JobDataMap();
        ClientConnectionEvent stateEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");
        String stateKey = stateEvent.extractStateForBatchJob();
        historyStates.put(stateKey, stateEvent.toJson());

        Set<String> inactiveClients = new HashSet<>(Set.of(stateKey));
        ClientStateProcessor processor = new ClientStateProcessor(historyStates, inactiveClients);

        // mark hasStreamRawData=true through a non-empty calc call that doesn't affect this key
        ClientDeviceInfoEntity entity = mock(ClientDeviceInfoEntity.class);
        ClientDeviceInfoRaw raw = mock(ClientDeviceInfoRaw.class);
        when(entity.getPollTime()).thenReturn(LocalDateTime.now());
        when(entity.toClientDeviceInfoRaw()).thenReturn(raw);
        when(raw.toClientConnectionEvents()).thenReturn(new HashMap<>());
        processor.calcClientConnectionEvents(Stream.of(entity));

        // Act
        List<ClientConnectionEvent> actual = processor.calcInactiveClientEvents();

        // Assert
        assertEquals(1, actual.size());
        ClientConnectionEvent disconnected = actual.getFirst();
        assertEquals(2, disconnected.getConnectStatus());
        assertFalse(historyStates.containsKey(stateKey));
    }

    @Test
    void calcClientConnectionEvents_whenStoredStateJsonInvalid_throwsException() {
        // Arrange
        JobDataMap historyStates = new JobDataMap();
        Set<String> inactiveClients = new HashSet<>();
        ClientStateProcessor processor = new ClientStateProcessor(historyStates, inactiveClients);

        String currKey = "state-1";
        historyStates.put(currKey, "{invalid-json}");

        ClientConnectionEvent currEvent = sampleEvent("alice", 1001L, 3232235786L, "ssid-a");

        ClientDeviceInfoEntity entity = mock(ClientDeviceInfoEntity.class);
        ClientDeviceInfoRaw raw = mock(ClientDeviceInfoRaw.class);
        when(entity.getPollTime()).thenReturn(LocalDateTime.of(2026, 5, 8, 22, 0, 0));
        when(entity.toClientDeviceInfoRaw()).thenReturn(raw);
        when(raw.toClientConnectionEvents()).thenReturn(Map.of(currKey, currEvent));

        // Act + Assert
        assertThrows(Exception.class, () -> processor.calcClientConnectionEvents(Stream.of(entity)));
    }

    private static ClientConnectionEvent sampleEvent(String clientName, long clientMac, long routerMac, String wlanName) {
        ClientNormalized client = ClientNormalized.builder()
                .clientName(clientName)
                .clientMac(clientMac)
                .clientType(1)
                .build();

        IpNormalized ip = IpNormalized.builder()
                .ipv4(123456)
                .build();

        InterfaceNormalized iface = InterfaceNormalized.builder()
                .routerMac(routerMac)
                .wlanName(wlanName)
                .build();

        return new ClientConnectionEvent("2026-05-08", 3600, client, ip, iface);
    }
}