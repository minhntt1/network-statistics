## Why

The existing `readme.md` provides a high-level project description and development procedures but lacks comprehensive documentation about the full architecture, data pipeline, device vendor integrations, database schemas, configuration profiles, and design decisions. As the project grows with multiple device families (Aruba, TP-Link Deco, iGate, RFC1213) and complex ETL workflows, new contributors and maintainers need thorough documentation to understand, operate, and extend the system efficiently.

## What Changes

- **Enhanced high-level readme** with clearer project overview, architecture diagram descriptions, and quick-start guide
- **New architecture documentation** covering the layered ETL pipeline (in → etl → out) per device family
- **New device vendor reference** documenting all supported devices, their protocols (SNMP/HTTP), data types collected, and OID/endpoint details
- **New database schema documentation** for staging, archive, and ingestion tables across all device types
- **New configuration reference** for all Spring profiles (dev-executor, dev-scheduler, dev-admin, prd-executor, prd-scheduler, prd-admin)
- **New deployment guide** covering both local and container-based deployment scenarios
- **New package structure diagram** updated from the existing UML to reflect current state
- **New API reference** documenting admin endpoints (scheduler management, client info, modem auth)

## Capabilities

### New Capabilities

- `project-overview`: High-level readme with architecture summary, tech stack, and quick-start
- `architecture-guide`: Detailed documentation of the ETL data pipeline structure and flow
- `device-vendor-reference`: Reference documenting all supported device vendors and models
- `database-schema`: Documentation of all database schemas (staging, archive, ingestion tables) per device
- `configuration-reference`: Reference for all Spring profiles, properties, and Quartz scheduler configs
- `deployment-guide`: Step-by-step deployment procedures for dev and production environments
- `api-reference`: Documentation of REST API endpoints for admin/scheduler management

### Modified Capabilities

*(None — no existing specs to modify)*

## Impact

- **New documentation files** under `docs/` and possibly `docs/architecture/`, `docs/deployment/`, `docs/api/` directories
- **Updated `readme.md`** to serve as the entry point linking to detailed docs
- **No code changes** — this is purely a documentation effort
- **No API, dependency, or schema changes**
