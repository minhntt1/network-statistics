# Database Schema

## Overview

The system uses a **3-tier table pattern** per device data type:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────────┐
│  <data>_stg  │ -> │<data>_archive│    │<data>_stg_ingest │
│  (staging)   │    │  (archive)   │    │  (ingestion)     │
└──────────────┘    └──────────────┘    └──────────────────┘
      │                                      │
      └────── ETL moves data ────────────────┘
```

### Staging Tables (`<data>_stg`)

Raw data landing zone. Device data is first written here as-is.

```sql
CREATE TABLE `<data>_stg` (
    `id` int NOT NULL AUTO_INCREMENT,
    `poll_time` datetime NOT NULL,
    `raw_data` json DEFAULT NULL COMMENT 'use json to handle schema change',
    PRIMARY KEY (`id`,`poll_time`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

### Archive Tables (`<data>_archive`)

Long-term storage with year-based partitioning:

```sql
CREATE TABLE `<data>_archive` LIKE `<data>_stg`;

ALTER TABLE `<data>_archive` PARTITION BY RANGE (YEAR(poll_time)) (
    PARTITION p2025 VALUES LESS THAN (2025) ENGINE = InnoDB,
    PARTITION p2026 VALUES LESS THAN (2026) ENGINE = InnoDB,
    PARTITION p2027 VALUES LESS THAN (2027) ENGINE = InnoDB,
    PARTITION p2028 VALUES LESS THAN (2028) ENGINE = InnoDB,
    PARTITION p2029 VALUES LESS THAN (2029) ENGINE = InnoDB,
    PARTITION p2030 VALUES LESS THAN (2030) ENGINE = InnoDB,
    PARTITION p9999 VALUES LESS THAN (9999) ENGINE = InnoDB
);
```

### Ingestion Tables (`<data>_stg_ingest`)

Device-specific entities with strongly-typed columns. Created `LIKE <data>_stg` but populated by ETL processes with parsed/normalized data.

---

## Ingestion Tables (Device-Specific)

### Aruba Instant AP

**`aruba_iap_device_info_stg_ingest`** — Client device info

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| device_mac | BIGINT | Client MAC address (numeric) |
| device_wlan_mac | BIGINT | WLAN MAC address (numeric) |
| device_ip | INT | Client IP address (numeric) |
| device_ap_ip | INT | AP IP address (numeric) |
| device_name | VARCHAR(255) | Client hostname |
| device_rx | BIGINT | RX bytes |
| device_tx | BIGINT | TX bytes |
| device_snr | INT | Signal-to-noise ratio |
| device_uptime_seconds | BIGINT | Uptime in seconds |

**`aruba_iap_ap_info_stg_ingest`** — AP info

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| ap_mac | BIGINT | AP MAC address (numeric) |
| ap_name | VARCHAR(255) | AP name |
| ap_ip | INT | AP IP address (numeric) |
| ap_model | VARCHAR(255) | AP model (e.g., IAP-315) |
| ap_uptime_seconds | BIGINT | Uptime in seconds |

**`aruba_iap_wlan_traffic_stg_ingest`** — WLAN traffic

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| wlan_ap_mac | BIGINT | AP MAC (numeric) |
| wlan_essid | VARCHAR(255) | ESSID name |
| wlan_mac | BIGINT | WLAN MAC (numeric) |
| wlan_rx | BIGINT | RX bytes |
| wlan_tx | BIGINT | TX bytes |

### TP-Link Deco

**`tplink_deco_device_info_stg_ingest`** — Device info

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| raw_data | TEXT/JSON | JSON blob of device info array |

**`tplink_deco_client_device_wlan_stg_ingest`** — Client device/WLAN info

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| raw_data | TEXT/JSON | JSON blob of client/WLAN info |

### iGate GW240

**`igate_gw240_status_wifi_station_stg_ingest`** — WiFi station status

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| raw_data | TEXT/JSON | JSON blob of WiFi station data |

### RFC1213

**`rfc1213_iftable_traffic_stg_ingest`** — Interface traffic

| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK, AUTO) | Auto-increment ID |
| poll_time | DATETIME | Polling timestamp |
| if_index | INT | Interface index |
| if_descr | VARCHAR(255) | Interface description |
| if_phys_address | BIGINT | Physical/MAC address (numeric) |
| if_admin_status | VARCHAR(10) | Admin status (1=up, 2=down) |
| if_oper_status | VARCHAR(10) | Operational status (1=up, 2=down) |
| if_in_octets | BIGINT | Input octets |
| if_out_octets | BIGINT | Output octets |
| ip_ad_ent_addr | INT | Associated IP address (numeric) |

---

## ETL Result Tables

These tables store the output of the normalization/summarization layer. They are populated by ETL services and queried by Grafana dashboards.

### Aruba IAP ETL Results

| Table / Dataset | Key Fields | Source |
|---|---|---|
| ClientTrafficHourlyCount | date, time_second, device_mac, device_name, tx+rx | ArubaAiClientInfoEntity |
| ClientUptimeRecord | device_mac, device_name, device_uptime_seconds, device_ip | ArubaAiClientInfoEntity |
| ClientWlanConnectEvent | device_mac, device_name, device_ip, device_wlan_mac, date_connect, time_second_connect, connect_status (1=connect,2=disconnect) | ArubaAiClientInfoEntity |
| ClientWlanMetricEvent | device_mac, device_name, device_snr, date_metric, time_second_metric | ArubaAiClientInfoEntity |
| ApTrafficHourlyCount | ap_date, ap_hour (seconds), ap_wlan_mac, ap_wlan_essid, ap_wlan_rx_total+tx_total | ArubaAiWlanTrafficEntity |
| ApRebootWeeklyCount | week (Monday date), ap_mac, ap_name, ap_ip, reboot_cnt | ArubaAiApInfoEntity |

### TP-Link Deco ETL Results

| Dataset | Description |
|---|---|
| ApInfo | Normalized AP information (model, firmware, MAC) |
| ApConnLostCnt | AP connection lost counter |
| IpNormalized | Normalized IP entries |
| ClientConnectionEvent | Client connect/disconnect events |
| ClientHourlyTraffic | Client hourly traffic |
| ClientNormalized | Normalized client info |
| InterfaceNormalized | Normalized interface info |

### RFC1213 ETL Results

| Dataset | Key Fields | Source |
|---|---|---|
| TrafficHourlyCount | date, time_hour_second, if_phys_address, if_descr, in_bytes+out_bytes | IftableTrafficEntity |

### iGate GW240 ETL Results

| Dataset | Description | Source |
|---|---|---|
| ClientWlanConnectEvent | Connect events from WiFi station data | StatusWifiStationEntity |

---

## Dimension & Fact Tables

The dimensional model uses a star schema pattern for WLAN connection events.

### `device_wlan_connections_fact`

Connection events (connect/disconnect) from all device sources (Aruba, TP-Link Deco, iGate GW240).

| Column | Type | Description |
|---|---|---|
| date_key | INT (FK) | Reference to `date_dim` |
| time_key | INT (FK) | Reference to `time_dim` |
| device_key | INT (FK) | Reference to `device_dim` |
| device_ip_key | INT (FK) | Reference to `ip_dim` |
| ap_key | INT (FK) | Reference to `ap_dim` (-2147483648 = undefined) |
| iface_key | INT (FK) | Reference to `gw_iface_dim` |
| vendor_key | INT (FK) | Reference to `vendor_dim` (-2147483648 = undefined) |
| ap_vendor_key | INT (FK) | Reference to `vendor_dim` (-2147483648 = undefined) |
| cnt_status_key | INT (FK) | Reference to `connection_status_dim` |
| event_timestamp | BIGINT | Denormalized timestamp formatted as YYYYMMDDHHmmSS, computed from `date_dim.date` + `time_dim.time` |

**Composite Primary Key**: `(date_key, time_key, device_key, device_ip_key, ap_key, iface_key, vendor_key, ap_vendor_key, cnt_status_key)`

### `date_dim`

| Column | Type | Description |
|---|---|---|
| date_key | INT (PK) | Surrogate key |
| date | DATE | Calendar date |

### `time_dim`

| Column | Type | Description |
|---|---|---|
| time_key | INT (PK) | Surrogate key |
| time | INT | Seconds since midnight (0-86399) |
