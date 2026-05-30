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
- Filtering and pagination parameters

#### Scenario: Client info endpoints are documented
- **WHEN** a developer reads the API reference
- **THEN** they SHALL find the client information endpoints with available query parameters

### Requirement: API reference documents modem auth endpoints
The API reference SHALL document the modem authentication endpoints exposed by `ModemAuthController`:
- Authentication flow for modem access
- Credential management

#### Scenario: Modem auth endpoints are documented
- **WHEN** a developer reads the API reference
- **THEN** they SHALL find the modem authentication endpoints with request/response formats

