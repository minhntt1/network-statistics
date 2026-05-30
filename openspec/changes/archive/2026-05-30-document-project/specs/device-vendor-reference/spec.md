## ADDED Requirements

### Requirement: Device vendor reference documents all supported vendors
The device vendor reference SHALL document each supported vendor and model family:
- TP-Link Deco: protocol (HTTP API), authentication flow, telemetry endpoints
- Aruba Instant AP: protocol (SNMP), OIDs used for client/AP/WLAN data
- iGate GW240: protocol (HTTP + SNMP), endpoints and OIDs
- RFC1213: protocol (SNMP), standard MIB-II OIDs for interface traffic and IP addressing

#### Scenario: Each vendor has a dedicated section
- **WHEN** a developer reads the device vendor reference
- **THEN** they SHALL find a section for each supported vendor with protocol details and data collected

### Requirement: Device reference lists collected data types
The reference SHALL list what data types are collected per device:
- Client information (MAC, IP, signal strength, connection status)
- Access point information (model, firmware, status, connected clients)
- Traffic statistics (bytes in/out, packets, interface counters)
- Connection events (connect/disconnect with timestamps)
- Reboot events

#### Scenario: Data type mapping is documented
- **WHEN** a developer reads the device vendor reference
- **THEN** they SHALL understand what data is collected from each device type and how it flows through the pipeline

### Requirement: SNMP OIDs are documented where applicable
For SNMP-based devices, the reference SHALL document the key OIDs used for polling.

#### Scenario: OIDs are listed
- **WHEN** a developer reads the SNMP device sections
- **THEN** they SHALL find the relevant OIDs for interface tables, IP address tables, and vendor-specific MIBs
