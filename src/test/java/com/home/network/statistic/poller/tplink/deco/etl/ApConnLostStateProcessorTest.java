package com.home.network.statistic.poller.tplink.deco.etl;

import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoEntity;
import com.home.network.statistic.poller.tplink.deco.out.DeviceInfoRaw;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApConnLostCnt;
import com.home.network.statistic.poller.tplink.deco.out.etl.ApInfo;
import com.home.network.statistic.poller.tplink.deco.out.etl.IpNormalized;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApConnLostStateProcessorTest {

    @Test
    void calcApConnLostCnt_whenNewStateKey_addsZeroLostCntAndStoresState() {
        // Arrange
        JobDataMap histStates = new JobDataMap();
        ApConnLostStateProcessor processor = new ApConnLostStateProcessor(histStates);

        String stateKey = "apConnLostEvent_1001.ap-1";
        ApConnLostCnt emptyLostCnt = sampleLostCnt("2026-05-08", 1001L, "ap-1", 1111, 0);
        DeviceInfoRaw currState = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 21, 0), "online", "connected");

        DeviceInfoEntity entity = mock(DeviceInfoEntity.class);
        when(entity.toApConnLost()).thenReturn(Map.of(stateKey, emptyLostCnt));
        when(entity.toMapStkToState()).thenReturn(Map.of(stateKey, currState));

        // Act
        processor.calcApConnLostCnt(Stream.of(entity));

        // Assert
        Collection<ApConnLostCnt> result = processor.toApConnLostCnts();
        assertEquals(1, result.size());
        assertEquals(0, result.iterator().next().getLostCnt());
        assertEquals(currState.toJson(), histStates.getString(stateKey));
    }

    @Test
    void calcApConnLostCnt_whenHistoryExistsAndNoConnLost_keepsLostCntZero() {
        // Arrange
        JobDataMap histStates = new JobDataMap();
        ApConnLostStateProcessor processor = new ApConnLostStateProcessor(histStates);

        String stateKey = "apConnLostEvent_1001.ap-1";
        DeviceInfoRaw prevState = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 20, 55), "online", "connected");
        histStates.put(stateKey, prevState.toJson());

        ApConnLostCnt emptyLostCnt = sampleLostCnt("2026-05-08", 1001L, "ap-1", 1111, 0);
        DeviceInfoRaw currState = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 21, 0), "online", "connected");

        DeviceInfoEntity entity = mock(DeviceInfoEntity.class);
        when(entity.toApConnLost()).thenReturn(Map.of(stateKey, emptyLostCnt));
        when(entity.toMapStkToState()).thenReturn(Map.of(stateKey, currState));

        // Act
        processor.calcApConnLostCnt(Stream.of(entity));

        // Assert
        ApConnLostCnt result = processor.toApConnLostCnts().iterator().next();
        assertEquals(0, result.getLostCnt());
        assertEquals(currState.toJson(), histStates.getString(stateKey));
    }

    @Test
    void calcApConnLostCnt_whenHistoryExistsAndConnLost_increasesLostCnt() {
        // Arrange
        JobDataMap histStates = new JobDataMap();
        ApConnLostStateProcessor processor = new ApConnLostStateProcessor(histStates);

        String stateKey = "apConnLostEvent_1001.ap-1";
        DeviceInfoRaw prevState = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 20, 55), "online", "connected");
        histStates.put(stateKey, prevState.toJson());

        ApConnLostCnt emptyLostCnt = sampleLostCnt("2026-05-08", 1001L, "ap-1", 1111, 0);
        DeviceInfoRaw currState = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 21, 0), "offline", "disconnected");

        DeviceInfoEntity entity = mock(DeviceInfoEntity.class);
        when(entity.toApConnLost()).thenReturn(Map.of(stateKey, emptyLostCnt));
        when(entity.toMapStkToState()).thenReturn(Map.of(stateKey, currState));

        // Act
        processor.calcApConnLostCnt(Stream.of(entity));

        // Assert
        ApConnLostCnt result = processor.toApConnLostCnts().iterator().next();
        assertEquals(1, result.getLostCnt());
        assertEquals(currState.toJson(), histStates.getString(stateKey));
    }

    @Test
    void cleanOldHistStatesNoData_whenInactivePrefixedKey_removesOnlyThatKey() {
        // Arrange
        JobDataMap histStates = new JobDataMap();
        String inactivePrefixedKey = "apConnLostEvent_1001.ap-1";
        String activePrefixedKey = "apConnLostEvent_1002.ap-2";
        String nonPrefixedKey = "another_state_key";

        histStates.put(inactivePrefixedKey, sampleDeviceState(LocalDateTime.now(), "online", "connected").toJson());
        histStates.put(activePrefixedKey, sampleDeviceState(LocalDateTime.now(), "online", "connected").toJson());
        histStates.put(nonPrefixedKey, "keep-me");

        ApConnLostStateProcessor processor = new ApConnLostStateProcessor(histStates);

        // stream contains only activePrefixedKey => inactivePrefixedKey remains inactive
        DeviceInfoEntity entity = mock(DeviceInfoEntity.class);
        when(entity.toApConnLost()).thenReturn(Map.of(activePrefixedKey, sampleLostCnt("2026-05-08", 1002L, "ap-2", 2222, 0)));
        when(entity.toMapStkToState()).thenReturn(Map.of(activePrefixedKey, sampleDeviceState(LocalDateTime.now(), "online", "connected")));
        processor.calcApConnLostCnt(Stream.of(entity));

        // Act
        processor.cleanOldHistStatesNoData();

        // Assert
        assertFalse(histStates.containsKey(inactivePrefixedKey));
        assertTrue(histStates.containsKey(activePrefixedKey));
        assertTrue(histStates.containsKey(nonPrefixedKey));
    }

    @Test
    void calcApConnLostCnt_whenMalformedHistoryJson_throwsException() {
        // Arrange
        JobDataMap histStates = new JobDataMap();
        ApConnLostStateProcessor processor = new ApConnLostStateProcessor(histStates);

        String stateKey = "apConnLostEvent_1001.ap-1";
        histStates.put(stateKey, "{invalid-json}");

        DeviceInfoEntity entity = mock(DeviceInfoEntity.class);
        when(entity.toApConnLost()).thenReturn(Map.of(stateKey, sampleLostCnt("2026-05-08", 1001L, "ap-1", 1111, 0)));
        when(entity.toMapStkToState()).thenReturn(Map.of(stateKey, sampleDeviceState(LocalDateTime.now(), "online", "connected")));

        // Act + Assert
        assertThrows(Exception.class, () -> processor.calcApConnLostCnt(Stream.of(entity)));
    }

    @Test
    void calcApConnLostCnt_whenMultipleEntitiesWithSameCounterIdentity_accumulatesInSingleCounter() {
        // Arrange
        JobDataMap histStates = new JobDataMap();
        ApConnLostStateProcessor processor = new ApConnLostStateProcessor(histStates);
        String stateKey = "apConnLostEvent_1001.ap-1";

        DeviceInfoRaw prevState = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 20, 0), "online", "connected");
        histStates.put(stateKey, prevState.toJson());

        ApConnLostCnt lostCnt1 = sampleLostCnt("2026-05-05", 1001L, "ap-1", 1111, 0);
        DeviceInfoRaw curr1 = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 21, 0), "offline", "connected");

        ApConnLostCnt lostCnt2 = sampleLostCnt("2026-05-05", 1001L, "ap-1", 1111, 0);
        DeviceInfoRaw curr2 = sampleDeviceState(LocalDateTime.of(2026, 5, 8, 22, 0), "offline", "connected");

        DeviceInfoEntity entity1 = mock(DeviceInfoEntity.class);
        when(entity1.toApConnLost()).thenReturn(Map.of(stateKey, lostCnt1));
        when(entity1.toMapStkToState()).thenReturn(Map.of(stateKey, curr1));

        DeviceInfoEntity entity2 = mock(DeviceInfoEntity.class);
        when(entity2.toApConnLost()).thenReturn(Map.of(stateKey, lostCnt2));
        when(entity2.toMapStkToState()).thenReturn(Map.of(stateKey, curr2));

        // Act
        processor.calcApConnLostCnt(Stream.of(entity1, entity2));

        // Assert
        assertEquals(1, processor.toApConnLostCnts().size());
        ApConnLostCnt cnt = processor.toApConnLostCnts().iterator().next();
        assertEquals(1, cnt.getLostCnt());
    }

    private static DeviceInfoRaw sampleDeviceState(LocalDateTime pollTime, String inetStatus, String groupStatus) {
        return DeviceInfoRaw.builder()
                .pollTime(pollTime)
                .deviceModel("deco-x")
                .deviceIp("192.168.1.1")
                .nickName("ap-1")
                .deviceMac("AA:BB:CC:DD:EE:FF")
                .inetStatus(inetStatus)
                .groupStatus(groupStatus)
                .build();
    }

    private static ApConnLostCnt sampleLostCnt(String week, long apMac, String apName, int ip, int lostCnt) {
        return ApConnLostCnt.builder()
                .startWeek(week)
                .apInfo(ApInfo.builder().apMac(apMac).apName(apName).build())
                .ipNormalized(IpNormalized.builder().ipv4(ip).build())
                .lostCnt(lostCnt)
                .build();
    }
}