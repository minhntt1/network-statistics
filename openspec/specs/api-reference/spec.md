# API Reference

## Purpose

This document serves as the API reference for the Network Statistics platform, documenting all REST API endpoints exposed by the application's controllers.

## Requirements

### Requirement: API reference documents scheduler management endpoints
The API reference SHALL document the scheduler management endpoints exposed by `JobSchedulerController`:
- GET/POST/PUT/DELETE operations for job details and triggers
- Request/response DTOs (JobDetailDTO, TriggerDTO, SchedulerDTO)

#### Scenario: Scheduler endpoints are documented
- **WHEN** a developer reads the API reference
- **THEN** they SHALL find the available scheduler management endpoints with request/response formats

### Requirement: API reference documents client info endpoints
The API reference SHALL document the client information endpoints exposed by `ClientInfoController`:
- Client listing and detail queries
- Connection history and status
- Filtering and pagination parameters, including configurable page `size` (default 15, max 100)
- The `connection/getInfo.do` endpoint SHALL return results sorted by `deviceKey ASC, eventTimestamp DESC`
- The `connection/getInfo.do` endpoint SHALL expose pagination via the following model attributes: `currentPage` (current 1-based page number), `listConnections` (records for current page), `pages` (visible page numbers array for pagination bar)
- The pagination navigation SHALL NOT include First or Last page links

#### Scenario: Client info endpoints are documented
- **WHEN** a developer reads the API reference
- **THEN** they SHALL find the client information endpoints with available query parameters including `page` and `size`
- **THEN** they SHALL find that `connection/getInfo.do` results are sorted by `deviceKey ASC, eventTimestamp DESC`
- **THEN** they SHALL find that `connection/getInfo.do` pagination uses `currentPage`, `listConnections`, `pages` model attributes
- **THEN** they SHALL find that First/Last page links are not included in the pagination navigation

### Requirement: API reference documents modem auth endpoints
The API reference SHALL document the modem authentication endpoints exposed by `ModemAuthController`:
- Authentication flow for modem access
- Credential management

#### Scenario: Modem auth endpoints are documented
- **WHEN** a developer reads the API reference
- **THEN** they SHALL find the modem authentication endpoints with request/response formats

