# Database Schema

## Purpose

This document describes the database schema used by the Network Statistics platform, including staging tables, archive tables, ingestion tables, and ETL result tables.

## Requirements

### Requirement: Database schema documents staging tables
The database schema documentation SHALL describe the staging table structure:
- Common schema pattern: `id`, `poll_time`, `raw_data` (JSON)
- Partitioning strategy based on `poll_time`
- Naming convention: `<data>_stg`

#### Scenario: Staging table pattern is documented
- **WHEN** a developer reads the database schema docs
- **THEN** they SHALL understand the staging table schema and its purpose in the ETL pipeline

### Requirement: Database schema documents archive tables
The archive table schema SHALL be documented:
- Same structure as staging tables
- Year-based partitioning for long-term storage
- Naming convention: `<data>_archive`

#### Scenario: Archive table pattern is documented
- **WHEN** a developer reads the database schema docs
- **THEN** they SHALL understand the archive table schema and its partitioning scheme

### Requirement: Database schema documents ingestion tables
The ingestion tables SHALL be documented per device type, covering:
- Device-specific entity fields and their SQL types
- Repository/DAO patterns for accessing them
- Naming convention: `<data>_stg_ingest`

#### Scenario: Ingestion tables are listed per device
- **WHEN** a developer reads the database schema docs
- **THEN** they SHALL find the ingestion table schemas for each device family with field descriptions

### Requirement: Database schema documents ETL result tables
The ETL result tables (summarized/normalized data) SHALL be documented:
- ApTrafficHourlyCount, ClientTrafficHourlyCount
- ClientUptimeRecord, ClientWlanConnectEvent
- ClientWlanMetricEvent, ApRebootWeeklyCount
- DeviceInfo, ClientDeviceInfo, IfTableTraffic

#### Scenario: ETL result tables are documented
- **WHEN** a developer reads the database schema docs
- **THEN** they SHALL find the schema for each ETL result table with field descriptions

