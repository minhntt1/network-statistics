# Project Overview

## Description

Network Statistics is a Spring Boot application that ingests, normalizes, and analyzes telemetry data from home/office network devices. It collects data from multiple device families (Aruba Instant AP, TP-Link Deco, iGate GW240, RFC1213-compliant devices) and provides:

- **Client monitoring**: Track connected clients, their MAC/IP addresses, signal strength, and connection status
- **Traffic analysis**: Hourly traffic statistics (bytes in/out) per client, per access point interface
- **Connection events**: Connect/disconnect event logging with timestamps
- **Reboot detection**: AP reboot counting per week
- **Visualization**: Grafana dashboards for data visualization and Thymeleaf-based admin web UI

## Package Structure

The project follows a standardized package layout:

```
com.home.network.statistic
├── Main.java                         # Spring Boot entry point
├── admin/
│   ├── scheduler/                    # Quartz job/trigger REST management
│   └── web/                          # Admin web controllers (client info, modem auth)
├── common/
│   ├── config/
│   │   └── quartz/                   # Quartz scheduler configs
│   ├── model/                        # Shared DTOs (TriggerInfo, ListSqlQuery)
│   └── util/                         # Utilities (AES, RSA, JSON, Pagination, Network)
├── poller/
│   ├── authentication/               # Credential management and device auth
│   ├── snmp/                         # Base SNMP target classes
│   ├── util/                         # Poller utilities (VariableBinding parsing)
│   ├── aruba/iap/                    # Aruba Instant AP integration
│   ├── tplink/deco/                  # TP-Link Deco integration
│   ├── igate/gw240/                  # iGate GW240 integration
│   └── rfc1213/                      # RFC1213 (standard SNMP) integration
└── vendor/                           # Vendor registration and lookup
```

Each device family follows the **in/etl/out** 3-layer pattern:
- `in/` — Ingestion: poll raw data from devices
- `etl/` — Normalization: transform raw data into metrics
- `out/` — Persistence: entities, repositories for DB storage

## Design Decisions

- **JSON for raw data storage**: Raw device telemetry is stored as JSON in staging tables, allowing schema flexibility without DDL changes
- **SNMP4J for SNMP polling**: Used for Aruba Instant AP and RFC1213 devices; supports table-walk operations for bulk data retrieval
- **Custom Quartz thread pool**: `AppQuartzThreadPool` provides a fixed-size thread pool (14 threads) shared across both Quartz scheduler instances
- **Profile-based deployment**: Separate Spring profiles for development vs production and for executor/scheduler/admin roles
- **Encrypted communication**: TP-Link Deco uses AES encryption for API calls; credentials can be encrypted with RSA
