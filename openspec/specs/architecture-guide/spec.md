# Architecture Guide

## Purpose

This document describes the system architecture of the Network Statistics platform, covering the data processing pipeline, scheduler system, and cross-cutting concerns.

## Requirements

### Requirement: Architecture guide documents the ETL pipeline
The architecture guide SHALL document the three-layer ETL pipeline per device family:
- **in/**: Ingestion layer — how raw data is polled from devices (SNMP/HTTP)
- **etl/**: Normalization layer — how raw data is cleaned, transformed, and summarized
- **out/**: Persistence layer — how data is stored in database entities and repositories

#### Scenario: Architecture describes data flow for a device family
- **WHEN** a developer reads the architecture guide
- **THEN** they SHALL understand the data flow from device polling through normalization to persistence for at least one device family

### Requirement: Architecture guide documents the scheduler system
The architecture guide SHALL document the Quartz scheduler setup, including:
- Polling schedulers vs ETL schedulers
- How job details and triggers are registered
- Thread pool configuration

#### Scenario: Scheduler architecture is described
- **WHEN** a developer reads the architecture guide
- **THEN** they SHALL understand the separation between poll-data schedulers and ETL schedulers

### Requirement: Architecture guide documents cross-cutting concerns
The architecture guide SHALL document cross-cutting components:
- Authentication/credential management for device access
- SQL query configuration via XML files
- Data source configuration for executor vs admin profiles

#### Scenario: Cross-cutting components are documented
- **WHEN** a developer reads the architecture guide
- **THEN** they SHALL find documentation for authentication, query config, and data source setup

