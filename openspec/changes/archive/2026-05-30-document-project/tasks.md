## 1. Project Overview & Readme Update

- [x] 1.1 Update `readme.md` with enhanced architecture summary, tech stack table, and quick-start guide
- [x] 1.2 Create links in readme to all new documentation files under `docs/`
- [x] 1.3 Update `docs/package_diagram.puml` to reflect current package structure
- [x] 1.4 Create `docs/project-overview.md` with comprehensive project description

## 2. Architecture Guide

- [x] 2.1 Create `docs/architecture-guide.md` documenting the three-layer ETL pipeline (in → etl → out) per device family
- [x] 2.2 Document the Quartz scheduler architecture (poll-data vs ETL schedulers, job/trigger registration, thread pools)
- [x] 2.3 Document cross-cutting concerns: authentication/credential management, SQL query XML config, data source setup

## 3. Device Vendor Reference

- [x] 3.1 Create `docs/device-vendor-reference.md` with a section for each supported vendor
- [x] 3.2 Document TP-Link Deco: HTTP API protocol, auth flow, telemetry endpoints
- [x] 3.3 Document Aruba Instant AP: SNMP protocol, OIDs for client/AP/WLAN data
- [x] 3.4 Document iGate GW240: HTTP + SNMP protocols, endpoints and OIDs
- [x] 3.5 Document RFC1213: standard MIB-II OIDs for interface traffic and IP addressing
- [x] 3.6 Document collected data types per device (client info, AP info, traffic stats, connection events, reboot events)

## 4. Database Schema Documentation

- [x] 4.1 Create `docs/database-schema.md` documenting staging table pattern (`id`, `poll_time`, `raw_data` JSON)
- [x] 4.2 Document archive table pattern with year-based partitioning strategy
- [x] 4.3 Document ingestion tables per device type with field descriptions
- [x] 4.4 Document ETL result tables (ApTrafficHourlyCount, ClientTrafficHourlyCount, ClientUptimeRecord, ClientWlanConnectEvent, ClientWlanMetricEvent, ApRebootWeeklyCount, DeviceInfo, ClientDeviceInfo, IfTableTraffic)

## 5. Configuration Reference

- [x] 5.1 Create `docs/configuration-reference.md` documenting all six Spring profiles
- [x] 5.2 Document key application properties by category (database, scheduling, SNMP, encryption)
- [x] 5.3 Document Quartz scheduler properties (`quartz-polldata.properties`, `quartz-etl.properties`)

## 6. Deployment Guide

- [x] 6.1 Create `docs/deployment-guide.md` with local development setup instructions
- [x] 6.2 Document container-based deployment (Docker build, env config, networking)
- [x] 6.3 Document production procedures (branch strategy, testing, monitoring, rollback)

## 7. API Reference

- [x] 7.1 Create `docs/api-reference.md` documenting scheduler management endpoints (JobSchedulerController)
- [x] 7.2 Document client info endpoints (ClientInfoController) with filtering and pagination
- [x] 7.3 Document modem auth endpoints (ModemAuthController) with auth flow
