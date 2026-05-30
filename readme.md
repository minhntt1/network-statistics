# Network Statistics

A project used to **ingest, normalize, analyze, and visualize** telemetry data from home/office network devices — including clients and access point traffic, client connect/disconnect events, and router reboot events.

![screenshot](docs/dashboard.png)

## Tech Stack

| Category | Technology |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.6 |
| **Database** | MySQL (via JDBC Template, JPA, jOOQ) |
| **Device Polling** | SNMP4J (SNMP), Java HTTP Client (REST) |
| **Scheduling** | Quartz Scheduler |
| **API Docs** | Swagger (springdoc-openapi) |
| **Frontend / Visualization** | Thymeleaf (admin UI) + Grafana (dashboards) |
| **Monitoring** | Micrometer + Prometheus + Actuator |
| **Build Tool** | Gradle (Kotlin DSL) |

## Architecture Overview

The system follows a **3-layer ETL pipeline** per device family:

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Ingestion  │ -> │Normalization │ -> │ Persistence  │
│    (in/)     │    │    (etl/)    │    │    (out/)    │
└──────────────┘    └──────────────┘    └──────────────┘
```

- **`in/`** — Polls raw data from physical network devices via SNMP or HTTP APIs
- **`etl/`** — Cleans, transforms, and summarizes raw data into normalized metrics
- **`out/`** — Persists data to MySQL using JPA entities and JDBC templates

The system supports **4 device families**:
- **Aruba Instant AP** (SNMP-based)
- **TP-Link Deco** (HTTP API-based)
- **iGate GW240** (HTTP + SNMP)
- **RFC1213** (standard SNMP MIB-II)

Two **Quartz scheduler instances** orchestrate the pipeline:
- **Poll-data scheduler** — Triggers data ingestion jobs at regular intervals
- **ETL scheduler** — Triggers normalization and summarization jobs

## Documentation

| Document | Description |
|---|---|
| [Architecture Guide](docs/architecture-guide.md) | Detailed ETL pipeline, scheduler, and cross-cutting concerns |
| [Project Overview](docs/project-overview.md) | Comprehensive project description and conventions |
| [Device Vendor Reference](docs/device-vendor-reference.md) | Supported vendors, protocols, OIDs, and data types |
| [Database Schema](docs/database-schema.md) | Staging, archive, ingestion, and ETL result tables |
| [Configuration Reference](docs/configuration-reference.md) | Spring profiles, properties, and Quartz configs |
| [Deployment Guide](docs/deployment-guide.md) | Local, container, and production deployment |
| [API Reference](docs/api-reference.md) | Scheduler, client info, and modem auth endpoints |
| [Package Structure Diagram](docs/package_diagram.puml) | PlantUML diagram of the package structure |

## Project structure

![Package Structure](docs/package-structure.png)

## Quick Start

### Prerequisites
- Java 21
- MySQL 8+
- Gradle (or use the bundled `gradlew` wrapper)

### Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd network-statistics
   ```

2. **Create the database**
   ```sql
   CREATE DATABASE network_statistics;
   CREATE DATABASE quartz;
   ```

3. **Configure database credentials**

   Update `src/main/resources/application-dev-executor.properties` with your MySQL connection details.

4. **Build the project**
   ```bash
   ./gradlew clean build -x test
   ```

5. **Run the application**

   Executor mode:
   ```bash
   java -Dspring.profiles.active=dev-executor -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
   ```

   Scheduler mode:
   ```bash
   java -Dspring.profiles.active=dev-scheduler -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
   ```

   Admin mode:
   ```bash
   java -Dspring.profiles.active=dev-admin -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
   ```

# Profile info
- dev-executor: used to run executor instances outside container environment with direct IP (ex: 192.168.100.1)  
- dev-scheduler: used to run scheduler instances outside container environment with direct IP (ex: 192.168.100.1)
- dev-admin: used to run admin web UI outside container environment with direct IP
- prd-executor: used to run executor instances inside container environment using container hostname (ex: mysql)
- prd-scheduler:  used to run scheduler instances inside container environment using container hostname (ex: mysql)
- prd-admin: used to run admin web UI inside container environment using container hostname

# Create archive, staging, ingestion table
```sql
CREATE TABLE `<data>_stg` (
                           `id` int NOT NULL AUTO_INCREMENT,
                           `poll_time` datetime NOT NULL,
                           `raw_data` json DEFAULT NULL COMMENT 'use json to handle schema change',
                           PRIMARY KEY (`id`,`poll_time`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
create table <data>_archive like <data>_stg;
create table <data>_stg_ingest like <data>_stg;
alter table <data>_archive partition by range (year(poll_time))
(
    partition p2025 values less than(2025) engine = innodb,
    partition p2026 values less than(2026) engine = innodb,
    partition p2027 values less than(2027) engine = innodb,
    partition p2028 values less than(2028) engine = innodb,
    partition p2029 values less than(2029) engine = innodb,
    partition p2030 values less than(2030) engine = innodb,
    partition p9999 values less than(9999) engine = innodb
);
```

# Development procedures for new device models/versions
1. Create a new package related to new models under vendor package (ex: deco under tplink)
2. Put related classes into etl, in, out packages indicating ETL flow, ingestion and persistence logic, respectively
   2.1. Create job classes in etl and in packages to define Quartz jobs
   2.2. Create service classes in etl and in packages to define logic workflows associated with job classes
      2.2.1. Service classes inside "in" responsible for handling data ingestion
      2.2.2. Service classes inside "etl" responsible for handling data normalization and summarization
   2.3. Create model classes in etl, in, and out packages to hold the business logic of workflows
   2.4. Create repository classes in out package to handle the logic of data persistence
3. Create db schemas - archive, staging, ingestion tables related to new device data in the database
4. Create new sql files for the workflows of new models in resources/etc_queries folder
5. Update the size of thread pool to run jobs in src/main/java/com/home/network/statistic/common/config/quartz/AppQuartzThreadPool.java, if necessary
6. Update src/main/java/com/home/network/statistic/common/config/ExecutorDataSourceConfig.java to declare new repository packages to scan
7. Update src/main/java/com/home/network/statistic/common/config/SqlQueryConfig.java to declare new sql query resource locations
8. Update *properties files to define new threadpool size configs for new jobs
9. Declare quartz new quartz triggers, job details in through scheduler interface

Example request for new job details:
```json
{
  "schedulerId": "quartzSchedulerPollData",
  "jobClassName": "com.home.network.statistic.poller.tplink.deco.in.FetchTelemetryJob",
  "jobNm": "fetchTplinkDecoTelemetry",
  "jobGr": "JOB",
  "isDurable": true,
  "requestsRecovery": true
}
```

Example request for new triggers:
```json
{
  "schedulerId": "quartzSchedulerETL",
  "triggerName": "triggerTpLinkDecoClientDeviceInfo",
  "triggerGroup": "TRIGGER",
  "jobName": "etlTpLinkDecoClientDeviceInfo",
  "jobGroup": "JOB",
  "cronTriggerCronExpression": "0 1/10 * * * ?",
  "cronTriggerTimeZone": "Asia/Saigon",
  "cronTrigger": true,
  "simpleTrigger": false,
  "dailyTimeIntervalTrigger": false
}
```

# Deployment procedures
1. Create a new branch from main to implement changes 
2. Test and debug on local using a dev database
3. If local test passes, deploy on a dev environment
4. Monitor dev outputs, if all outputs satistfied, deploy on prd
5. Merge code to main

# Common tasks
Run unit tests
```
gradle test
```

Run spring application from IntellJ
```
gradle bootRun
```

Build and skip test
```
gradle clean build -x test
```

# Run on dev profile - local
Run executor  from java command line
```
java '-Dspring.profiles.active=dev-executor' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run dev scheduler from java cmd
```
java '-Dspring.profiles.active=dev-scheduler' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run dev admin from java cmd
```
java '-Dspring.profiles.active=dev-admin' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

# Run inside container
Run executor  from java command line
```
java '-Dspring.profiles.active=prd-executor' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run scheduler from java cmd
```
java '-Dspring.profiles.active=prd-scheduler' -jar ./build/libs/network-statistic-0.0.1-SNAPSHOT.jar
```

Run executor  from bash shell
```
java -Xmx256m -Dspring.profiles.active=prd-executor -jar network-statistic-0.0.1-SNAPSHOT.jar
```

Run executor  from bash shell
```
java -Xmx256m -Dspring.profiles.active=prd-scheduler -jar network-statistic-0.0.1-SNAPSHOT.jar
```
