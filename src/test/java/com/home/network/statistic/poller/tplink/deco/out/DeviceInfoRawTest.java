package com.home.network.statistic.poller.tplink.deco.out;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DeviceInfoRawTest {

    @Test
    void extractDateFromEvent_whenPollTimePresent_returnsIsoLocalDate() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder()
                .pollTime(LocalDateTime.of(2026, 5, 8, 20, 30, 0))
                .build();

        // Act
        String actual = raw.extractDateFromEvent();

        // Assert
        assertEquals("2026-05-08", actual);
    }

    @Test
    void extractDateFromEvent_whenPollTimeNull_throwsNullPointerException() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder().build();

        // Act + Assert
        assertThrows(NullPointerException.class, raw::extractDateFromEvent);
    }

    @Test
    void extractNormMac_whenValidColonDelimitedMac_returnsExpectedLong() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder()
                .deviceMac("AA:BB:CC:DD:EE:FF")
                .build();

        // Act
        Long actual = raw.extractNormMac();

        // Assert
        assertEquals(Long.valueOf(0xAABBCCDDEEFFL), actual);
    }

    @Test
    void extractNormMac_whenBlankMac_returnsZero() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder()
                .deviceMac("   ")
                .build();

        // Act
        Long actual = raw.extractNormMac();

        // Assert
        assertEquals(Long.valueOf(0L), actual);
    }

    @Test
    void extractNormMac_whenInvalidMac_throwsNumberFormatException() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder()
                .deviceMac("GG:11:22:33:44:55")
                .build();

        // Act + Assert
        assertThrows(NumberFormatException.class, raw::extractNormMac);
    }

    @Test
    void extractIp_whenValidIpv4_returnsExpectedInt() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder()
                .deviceIp("192.168.1.10")
                .build();

        // Act
        int actual = raw.extractIp();

        // Assert
        int expected = (192 << 24) | (168 << 16) | (1 << 8) | 10;
        assertEquals(expected, actual);
    }

    @Test
    void extractIp_whenInvalidIpv4_throwsNumberFormatException() {
        // Arrange
        DeviceInfoRaw raw = DeviceInfoRaw.builder()
                .deviceIp("192.abc.1.10")
                .build();

        // Act + Assert
        assertThrows(NumberFormatException.class, raw::extractIp);
    }

    @Test
    void checkToOfflineInetStat_whenOnlineToOffline_returnsTrue() {
        // Arrange
        DeviceInfoRaw previous = DeviceInfoRaw.builder().inetStatus("online").build();
        DeviceInfoRaw current = DeviceInfoRaw.builder().inetStatus("offline").build();

        // Act
        boolean actual = current.checkToOfflineInetStat(previous);

        // Assert
        assertTrue(actual);
    }

    @Test
    void checkToOfflineInetStat_whenNotTransitionToOffline_returnsFalse() {
        // Arrange
        DeviceInfoRaw previous = DeviceInfoRaw.builder().inetStatus("offline").build();
        DeviceInfoRaw current = DeviceInfoRaw.builder().inetStatus("online").build();

        // Act
        boolean actual = current.checkToOfflineInetStat(previous);

        // Assert
        assertFalse(actual);
    }

    @Test
    void checkToDisconnectGroupStat_whenConnectedToDisconnected_returnsTrue() {
        // Arrange
        DeviceInfoRaw previous = DeviceInfoRaw.builder().groupStatus("connected").build();
        DeviceInfoRaw current = DeviceInfoRaw.builder().groupStatus("disconnected").build();

        // Act
        boolean actual = current.checkToDisconnectGroupStat(previous);

        // Assert
        assertTrue(actual);
    }

    @Test
    void checkToDisconnectGroupStat_whenNotTransitionToDisconnected_returnsFalse() {
        // Arrange
        DeviceInfoRaw previous = DeviceInfoRaw.builder().groupStatus("disconnected").build();
        DeviceInfoRaw current = DeviceInfoRaw.builder().groupStatus("connected").build();

        // Act
        boolean actual = current.checkToDisconnectGroupStat(previous);

        // Assert
        assertFalse(actual);
    }

    @Test
    void checkConnLost_whenEitherOfflineOrDisconnectTransitionOccurs_returnsTrue() {
        // Arrange
        DeviceInfoRaw previous = DeviceInfoRaw.builder()
                .inetStatus("online")
                .groupStatus("connected")
                .build();
        DeviceInfoRaw current = DeviceInfoRaw.builder()
                .inetStatus("offline")
                .groupStatus("connected")
                .build();

        // Act
        boolean actual = current.checkConnLost(previous);

        // Assert
        assertTrue(actual);
    }

    @Test
    void checkConnLost_whenNoRelevantTransitionOccurs_returnsFalse() {
        // Arrange
        DeviceInfoRaw previous = DeviceInfoRaw.builder()
                .inetStatus("offline")
                .groupStatus("disconnected")
                .build();
        DeviceInfoRaw current = DeviceInfoRaw.builder()
                .inetStatus("offline")
                .groupStatus("disconnected")
                .build();

        // Act
        boolean actual = current.checkConnLost(previous);

        // Assert
        assertFalse(actual);
    }
}