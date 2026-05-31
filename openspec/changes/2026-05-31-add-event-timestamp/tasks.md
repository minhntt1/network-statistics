# Tasks: Add `event_timestamp` to `device_wlan_connections_fact`

## Overview

5 implementation tasks across Java entity, 3 ETL SQL files, and database docs.

---

### Task 1: Add `eventTimestamp` field to `DeviceWlanConnectionsFact.java`

**Files**: `src/main/java/com/home/network/statistic/admin/web/DeviceWlanConnectionsFact.java`

**Changes**:
1. Add `Long eventTimestamp` field with `@Column(name = "event_timestamp")` after the `connectionStatusKey` block, before the helper methods
2. Add getter and setter via Lombok `@Getter`/`@Setter` (already on class)

**Verification**: `./gradlew build` compiles without errors

---

### Task 2: Add `event_timestamp` to Aruba IAP ETL insert

**Files**: `src/main/resources/etl_queries/client-info-query.xml`

**Changes**:
1. In key `updateFactTableDeviceWlanConnections`, add `event_timestamp` to the `insert ignore into device_wlan_connections_fact (` column list (after `cnt_status_key`)
2. Append `CAST(CONCAT(DATE_FORMAT(dd.date, '%Y%m%d'), DATE_FORMAT(SEC_TO_TIME(td.time), '%H%i%s')) AS UNSIGNED) as event_timestamp` to the SELECT list

---

### Task 3: Add `event_timestamp` to TP-Link Deco ETL insert

**Files**: `src/main/resources/etl_queries/tplink-deco-query.xml`

**Changes**: Same as Task 2 in key `updateFactTableDeviceWlanConnections`

---

### Task 4: Add `event_timestamp` to iGate GW240 ETL insert

**Files**: `src/main/resources/etl_queries/igate240-status-wifi-station.xml`

**Changes**: Same as Task 2 in key `updateFactTableDeviceWlanConnections`

---

### Task 5: Run backfill migration for existing rows

**SQL**:
```sql
UPDATE device_wlan_connections_fact f
  JOIN date_dim d ON f.date_key = d.date_key
  JOIN time_dim t ON f.time_key = t.time_key
  SET f.event_timestamp = CAST(
    CONCAT(
      DATE_FORMAT(d.date, '%Y%m%d'),
      DATE_FORMAT(SEC_TO_TIME(t.time), '%H%i%s')
    ) AS UNSIGNED
  )
  WHERE f.event_timestamp IS NULL;
```

**Prerequisite**: The `ALTER TABLE` to add the column must be run first (done via DB migration tool or manually).

---

### Task 6: Update database schema docs

**Files**: `docs/database-schema.md`

**Changes**:
1. Add `event_timestamp BIGINT` column to the `device_wlan_connections_fact` table definition
2. Add note: "Denormalized timestamp formatted as YYYYMMDDHHmmSS, computed from date_dim.date and time_dim.time"