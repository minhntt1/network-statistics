# Design: Add `event_timestamp` to `device_wlan_connections_fact`

## 1. Column Definition

```sql
ALTER TABLE device_wlan_connections_fact
  ADD COLUMN event_timestamp BIGINT NULL
  AFTER cnt_status_key;
```

| Property | Value |
|---|---|
| Column name | `event_timestamp` |
| Data type | `BIGINT` |
| Nullable | `NULL` (existing rows backfilled separately) |
| Default | `NULL` |
| Java type | `Long` (boxed, nullable) |

## 2. Format Specification

`event_timestamp` = `YYYYMMDDHHmmSS` as a 14-digit unsigned integer.

Computation:
```
event_timestamp = date_part(YYYYMMDD) + time_part(HHmmSS)

Where:
  date_part = DATE_FORMAT(date_dim.date, '%Y%m%d')        -- e.g. 20260101
  time_part = DATE_FORMAT(SEC_TO_TIME(time_dim.time), '%H%i%s')  -- e.g. 000000
```

### Examples

| date_dim.date | time_dim.time (seconds) | time_dim.time (HH:mm:ss) | event_timestamp |
|---|---|---|---|
| 2026-01-01 | 0 | 00:00:00 | 20260101000000 |
| 2026-06-15 | 45000 | 12:30:00 | 20260615123000 |
| 2026-12-31 | 86399 | 23:59:59 | 20261231235959 |

### Validation Constraints

- `event_timestamp` must be UNIQUE when combined with `device_key` (same device cannot have two identical timestamps). This is naturally enforced by the existing composite PK which includes `(date_key, time_key, device_key, ...)` — no two rows share the same date+time combination.
- A CHECK constraint is possible but optional (the source data is always clean).

## 3. File-by-File Changes

### 3.1 `DeviceWlanConnectionsFact.java` — Entity

**Add field:**

```java
@Column(name = "event_timestamp")
private Long eventTimestamp;
```

Placement: After the `connectionStatusKey` relationship block, before the helper methods.

### 3.2 `client-info-query.xml` (Aruba IAP)

**Temp table** — no change needed (temp table only holds staging columns).

**Insert statement** — Add `event_timestamp` column to both the target column list and the SELECT:

```sql
insert ignore into device_wlan_connections_fact (
    date_key, time_key, device_key, device_ip_key, ap_key, iface_key,
    vendor_key, ap_vendor_key, cnt_status_key,
    event_timestamp
)
select
    dd.date_key,
    td.time_key,
    dd1.device_key,
    id.ip_key,
    ad.ap_key,
    gid.iface_key,
    ifnull(vd.vendor_key, -2147483648),
    ifnull(vd1.vendor_key, -2147483648),
    aidi.connect_status,
    CAST(
        CONCAT(
            DATE_FORMAT(dd.date, '%Y%m%d'),
            DATE_FORMAT(SEC_TO_TIME(td.time), '%H%i%s')
        ) AS UNSIGNED
    ) as event_timestamp
from tmp_device_wlan_connections_fact aidi
...  -- rest of the joins unchanged
```

### 3.3 `tplink-deco-query.xml` (TP-Link Deco)

Same pattern as 3.2 — add `event_timestamp` to target column list and SELECT.

### 3.4 `igate240-status-wifi-station.xml` (iGate GW240)

Same pattern as 3.2.

## 4. Data Migration (Backfill)

For existing rows in the table, run a one-time UPDATE:

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

**Estimated execution**: For ~1M rows with indexed JOINs, this should run in under 30 seconds.

## 5. Testing

| Test | What to verify |
|---|---|
| New inserts (Aruba) | Query the staging ingestion, run ETL, verify `event_timestamp` is populated correctly |
| New inserts (TP-Link) | Same for Deco pipeline |
| New inserts (iGate) | Same for GW240 pipeline |
| Backfill | Run UPDATE, spot-check randomly selected rows |
| DTO unchanged | Confirm `DeviceWlanConnectionsDTO` compiles and runs without the new field |
| Sort/filter | `SELECT * FROM device_wlan_connections_fact ORDER BY event_timestamp DESC LIMIT 10` |

## 6. Rollback

If issues are found:

```sql
ALTER TABLE device_wlan_connections_fact DROP COLUMN event_timestamp;
```

Revert the Java entity field and 3 SQL insert statements.