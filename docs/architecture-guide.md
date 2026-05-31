# Architecture Guide

## ETL Pipeline

The system processes network device data through a three-layer ETL pipeline. Each supported device family has its own implementation of these layers.

### 1. Ingestion Layer (`in/`)

Responsible for polling raw data from physical network devices. Each device type uses its appropriate protocol:

| Protocol | Used By | Description |
|---|---|---|
| **SNMP** | Aruba Instant AP, RFC1213 | Polls MIB tables via SNMP GET/table-walk operations |
| **HTTP REST API** | TP-Link Deco, iGate GW240 | Authenticates and fetches telemetry via JSON API calls |

**Key classes** (per device):
- `*Request` — Defines the data request (OIDs for SNMP, JSON body for HTTP)
- `*Response` — Parses the raw response into structured data
- `*Target` — Connection target (IP, credentials, timeout)
- `*Job` — Quartz job that triggers the polling
- `*Service` — Orchestrates the polling workflow (auth, fetch, store)

### 2. Normalization Layer (`etl/`)

Processes raw data into normalized, summarized metrics:

- **Job classes** (`*Job`) — Quartz jobs that trigger ETL processing
- **Service classes** (`*Service`) — Business logic for data transformation
- **Model classes** — Represent normalized data structures:
  - `TrafficHourlyCount` / `ClientTrafficHourlyCount` — Hourly traffic summaries
  - `ClientUptimeRecord` — Latest uptime per client device
  - `ClientWlanConnectEvent` — Connect/disconnect events with timestamps
  - `ClientWlanMetricEvent` — SNR (signal-to-noise ratio) metric samples
  - `ApRebootWeeklyCount` — Weekly reboot counts per access point
  - `ApTrafficHourlyCount` — Hourly AP WLAN traffic summaries

### 3. Persistence Layer (`out/`)

Handles all data storage to MySQL:

- **Entity classes** — JPA `@Entity` mappings to `*_stg_ingest` tables
- **Repository classes** — Spring Data JPA repositories for CRUD operations
- **Raw data stores** — JSON blobs in staging tables that get processed by ETL

## Scheduler Architecture

Two independent Quartz scheduler instances manage the pipeline execution:

```
┌────────────────────────┐     ┌────────────────────────┐
│  QuartzSchedulerPollData│    │  QuartzSchedulerETL    │
│  (Poll-data scheduler) │     │  (ETL scheduler)      │
│                        │     │                        │
│  Polls device data     │     │  Runs normalization    │
│  on schedule           │     │  and summarization     │
│                        │     │                        │
│  Triggers:             │     │  Triggers:             │
│  - FetchTelemetryJob   │     │  - ClientDeviceJob     │
│  - Aruba polling jobs  │     │  - DeviceInfoJob       │
│  - SnmpPollingJob      │     │  - Aruba ETL jobs      │
│  - IngestionJob        │     │  - TrafficSummaryJob   │
└────────────────────────┘     └────────────────────────┘
```

### Thread Pool

The `AppQuartzThreadPool` is a custom Quartz `ThreadPool` implementation wrapped around a Java `Executors.newFixedThreadPool(14)`. It is shared across both schedulers, meaning 14 concurrent job threads are available total.

### Scheduler Configuration

- `quartz-polldata.properties` — Configuration for the polling scheduler (job store, thread pool, database)
- `quartz-etl.properties` — Configuration for the ETL scheduler
- Scheduler data is stored in a separate `quartz` database

Jobs and triggers are managed at runtime via the scheduler admin REST API (see [API Reference](api-reference.md)).

## Cross-Cutting Concerns

### Authentication / Credential Management

Device credentials are stored in the `device_auth_data_web` table with:
- `dataClass` — The credential type (e.g., `WebUiCredentials`, `IngestionCredentials`, `SnmpTarget`, `ArubaSnmpAiTarget`)
- `data` — Serialized JSON of credential fields (username, password, host, SNMP community, etc.)
- `tempData` — Temporary data (session tokens, headers) used during active polling

The `AuthDataService` provides CRUD operations for managing credentials via the admin web UI.

### SQL Query Configuration

ETL SQL queries are externalized in XML files under `src/main/resources/etl_queries/`:
- `ap-info-query.xml` — AP info ETL queries (Aruba)
- `ap-wlan-traffic-query.xml` — WLAN traffic ETL queries (Aruba)
- `client-info-query.xml` — Client info ETL queries (Aruba)
- `rfc1213-query.xml` — RFC1213 interface traffic ETL queries
- `igate240-status-wifi-station.xml` — iGate GW240 WiFi station ETL queries
- `tplink-deco-query.xml` — TP-Link Deco client/WLAN ETL queries
- `tplink-deco-device-query.xml` — TP-Link Deco device info ETL queries

Each XML file contains Properties-style key-value pairs of named SQL queries. They are loaded by `SqlQueryConfig` and injected as `ListSqlQuery` beans into ETL services.

### Data Source Configuration

Two data sources are configured:
- **`quartz-scheduler`** — For Quartz job store (scheduler metadata)
- **`statistic-db`** — Application data (device telemetry, auth data, admin data)

Profiles determine which data source configuration is active:
- Executor/admin profiles use both datasources (via `ExecutorDataSourceConfig`)
- Scheduler profiles only need the Quartz datasource

### Encryption Utilities

- `AESUtil` — AES encryption/decryption (used for TP-Link Deco API communication)
- `RSAUtil` — RSA encryption for credential storage
- `EncryptionUtil` — Wrapper for credential encryption/decryption
- `AESUtil` also handles encryption of deco password
