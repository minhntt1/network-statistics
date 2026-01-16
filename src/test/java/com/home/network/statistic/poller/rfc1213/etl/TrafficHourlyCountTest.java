package com.home.network.statistic.poller.rfc1213.etl;

import com.home.network.statistic.poller.rfc1213.out.IftableTrafficEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TrafficHourlyCountTest {

    @Test
    void testConstructor() {
        // Arrange
        LocalDateTime pollTime = LocalDateTime.of(2023, 10, 1, 12, 0);
        IftableTrafficEntity entity = IftableTrafficEntity.builder()
                .pollTime(pollTime)
                .ifPhysAddress(12345L)
                .ifDescr("eth0")
                .build();

        // Act
        TrafficHourlyCount count = new TrafficHourlyCount(entity);

        // Assert
        assertEquals("2023-10-01", count.getDate());
        assertEquals(43200, count.getTimeHourSecond()); // 12:00:00 in seconds
        assertEquals(12345L, count.getIfPhysAddress());
        assertEquals("eth0", count.getIfDescr());
        assertEquals(0, count.getInBytes());
        assertEquals(0, count.getOutBytes());
    }

    @Test
    void testAdjustTraffic() {
        // Arrange
        LocalDateTime pollTime = LocalDateTime.of(2023, 10, 1, 12, 0);
        IftableTrafficEntity oldEntity = IftableTrafficEntity.builder()
                .ifInOctets(1000L)
                .ifOutOctets(2000L)
                .build();
        IftableTrafficEntity newEntity = IftableTrafficEntity.builder()
                .ifInOctets(1500L)
                .ifOutOctets(2500L)
                .build();
        TrafficHourlyCount count = new TrafficHourlyCount(
                IftableTrafficEntity.builder().pollTime(pollTime).ifPhysAddress(12345L).ifDescr("eth0").build()
        );

        // Act
        count.adjustTraffic(oldEntity, newEntity);

        // Assert
        assertEquals(500, count.getInBytes()); // 1500 - 1000
        assertEquals(500, count.getOutBytes()); // 2500 - 2000
    }

    @Test
    void testAdjustTrafficWithWrapAround() {
        // Arrange: simulate counter wrap-around (new < old)
        IftableTrafficEntity oldEntity = IftableTrafficEntity.builder()
                .ifInOctets(4000L)
                .ifOutOctets(3000L)
                .build();
        IftableTrafficEntity newEntity = IftableTrafficEntity.builder()
                .ifInOctets(1000L) // less than old, so use new value
                .ifOutOctets(500L)
                .build();
        TrafficHourlyCount count = new TrafficHourlyCount(
                IftableTrafficEntity.builder().pollTime(LocalDateTime.now()).ifPhysAddress(12345L).ifDescr("eth0").build()
        );

        // Act
        count.adjustTraffic(oldEntity, newEntity);

        // Assert
        assertEquals(1000, count.getInBytes());
        assertEquals(500, count.getOutBytes());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        TrafficHourlyCount count1 = new TrafficHourlyCount(
                IftableTrafficEntity.builder()
                        .pollTime(LocalDateTime.of(2023, 10, 1, 12, 0))
                        .ifPhysAddress(12345L)
                        .ifDescr("eth0")
                        .build()
        );
        TrafficHourlyCount count2 = new TrafficHourlyCount(
                IftableTrafficEntity.builder()
                        .pollTime(LocalDateTime.of(2023, 10, 1, 12, 0))
                        .ifPhysAddress(12345L)
                        .ifDescr("eth0")
                        .build()
        );
        TrafficHourlyCount count3 = new TrafficHourlyCount(
                IftableTrafficEntity.builder()
                        .pollTime(LocalDateTime.of(2023, 10, 1, 13, 0))
                        .ifPhysAddress(67890L)
                        .ifDescr("eth1")
                        .build()
        );

        // Act & Assert
        assertEquals(count1, count2);
        assertEquals(count1.hashCode(), count2.hashCode());
        assertNotEquals(count1, count3);
        assertNotEquals(count1.hashCode(), count3.hashCode());
    }
}
