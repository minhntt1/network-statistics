# Proposal: Add `event_timestamp` column to `device_wlan_connections_fact`

## Summary

Add a denormalized `event_timestamp BIGINT` column to the `device_wlan_connections_fact` table, computed from the existing `date_key` → `date_dim.date` and `time_key` → `time_dim.time` dimensions. This provides a directly sortable/queryable timestamp for each connection event without requiring JOINs.

## Motivation

Currently, to get the timestamp of a connection event, one must:
1. Join `date_dim` on `date_key` to get the date
2. Join `time_dim` on `time_key` to get the time

This is expensive for queries that need to sort, filter by time range, or display timestamps. A materialized `event_timestamp` column simplifies queries and improves performance.

## Format

- **Column name**: `event_timestamp`
- **Data type**: `BIGINT` (maps to Java `Long`)
- **Format**: `yyyyMMddHHmmSS` (no separators, numeric for fast comparison)

### Examples

| date_dim.date | time_dim.time (seconds) | event_timestamp |
|---|---|---|
| 2026-01-01 | 0 (00:00:00) | 20260101000000 |
| 2026-05-30 | 3661 (01:01:01) | 20260530010101 |
| 2026-12-31 | 86399 (23:59:59) | 20261231235959 |

## SQL Formula

```sql
CAST(
  CONCAT(
    DATE_FORMAT(dd.date, '%Y%m%d'),
    DATE_FORMAT(SEC_TO_TIME(td.time), '%H%i%s')
  ) AS UNSIGNED
) AS event_timestamp
```

## Non-goals

- Not replacing the existing dimension keys (star schema remains intact)
- Not changing the primary key (composite key stays)
- Not adding an index in this change (can be done separately if needed)

## Scope of changes

| # | File | Change |
|---|---|---|
| 1 | `DeviceWlanConnectionsFact.java` | Add `Long eventTimestamp` field with `@Column` |
| 2 | `client-info-query.xml` (Aruba) | Add `event_timestamp` column to `insert into` and `select` |
| 3 | `tplink-deco-query.xml` (TP-Link) | Same change as #2 |
| 4 | `igate240-status-wifi-station.xml` (iGate) | Same change as #2 |
| 5 | `docs/database-schema.md` | Document the new column |

## Risks

- **Data consistency**: The column is computed at insert time. For existing rows, a backfill migration would be needed if we want historical timestamps.
  Backfill SQL:
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
  ```
- **Storage**: One extra BIGINT column = 8 bytes per row, negligible impact.