# Configuration Reference

## Purpose

This document provides a reference for all configuration options in the Network Statistics platform, including Spring profiles, application properties, and scheduler configuration.

## Requirements

### Requirement: Configuration reference documents all Spring profiles
The configuration reference SHALL document all Spring profiles:
- **dev-executor**: Local executor instance (direct IP access)
- **dev-scheduler**: Local scheduler instance (direct IP access)
- **dev-admin**: Local admin web UI instance
- **prd-executor**: Container executor instance (container hostnames)
- **prd-scheduler**: Container scheduler instance (container hostnames)
- **prd-admin**: Container admin web UI instance

#### Scenario: Each profile has a documented purpose
- **WHEN** a developer reads the configuration reference
- **THEN** they SHALL understand the purpose and configuration of each profile

### Requirement: Key application properties are documented
The configuration reference SHALL document key properties:
- Database connections (MySQL JDBC URLs)
- Thread pool sizes for various job types
- SNMP community strings and timeouts
- Quartz scheduler properties (thread pool, datasource)
- Encryption/credential settings

#### Scenario: Properties are documented by category
- **WHEN** a developer reads the configuration reference
- **THEN** they SHALL find properties organized by category (database, scheduling, SNMP, etc.)

### Requirement: Quartz scheduler configuration is documented
The Quartz scheduler setup SHALL be documented:
- `quartz-polldata.properties`: Polling scheduler configuration
- `quartz-etl.properties`: ETL scheduler configuration
- How job details and triggers are persisted

#### Scenario: Quartz configs are described
- **WHEN** a developer reads the configuration reference
- **THEN** they SHALL understand the two Quartz scheduler instances and their properties

