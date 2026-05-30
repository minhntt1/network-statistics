# Device Vendor Reference

## Overview

The system supports four device families, each with its own protocol, data model, and integration approach.

| Vendor | Models | Protocol | Auth Method |
|---|---|---|---|
| **Aruba** | Instant AP (IAP) | SNMP | SNMP community string |
| **TP-Link** | Deco series | HTTP REST API | Encrypted password (AES + custom encryption) |
| **iGate** | GW240 | HTTP + SNMP | HTTP basic auth (Base64) + SNMP community |
| **RFC1213** | Any SNMP-capable device | SNMP | SNMP community string |

---

## Aruba Instant AP (IAP)

### Protocol: SNMP

Aruba Instant APs expose telemetry via Aruba's proprietary AirWave MIB at OID `.1.3.6.1.4.1.14823.2.3.3.1.2`. Three data tables are polled.

### Client Info Table (`ArubaSnmpAiClientRequest`)

Base OID: `1.3.6.1.4.1.14823.2.3.3.1.2.4.1.`

| Column | OID Suffix | Field | Type |
|---|---|---|---|
| aiClientMACAddress | .1 | MAC address | Long (parsed) |
| aiClientWlanMACAddress | .2 | WLAN MAC address | Long (parsed) |
| aiClientIPAddress | .3 | IP address | Integer (parsed) |
| aiClientAPIPAddress | .4 | AP IP address | Integer (parsed) |
| aiClientName | .5 | Device hostname | String |
| aiClientSNR | .7 | Signal-to-noise ratio | Integer (parsed) |
| aiClientTxDataBytes | .9 | TX byte count | Long |
| aiClientRxDataBytes | .13 | RX byte count | Long |
| aiClientUptime | .16 | Uptime (timeticks) | Long → seconds |

### AP Info Table (`ArubaSnmpAiAccessPointRequest`)

Base OID: `1.3.6.1.4.1.14823.2.3.3.1.2.1.1.`

| Column | OID Suffix | Field | Type |
|---|---|---|---|
| aiAPMACAddress | .1 | AP MAC address | Long (parsed) |
| aiAPName | .2 | AP name | String |
| aiAPIPAddress | .3 | AP IP address | Integer (parsed) |
| aiAPModelName | .6 | Model name (e.g., IAP-315) | String |
| aiAPUptime | .9 | Uptime (timeticks) | Long → seconds |

### WLAN Traffic Table (`ArubaSnmpAiWlanRequest`)

Base OID: `1.3.6.1.4.1.14823.2.3.3.1.2.3.1.`

| Column | OID Suffix | Field | Type |
|---|---|---|---|
| aiWlanAPMACAddress | .1 | AP MAC | Long |
| aiWlanESSID | .3 | ESSID name | String |
| aiWlanMACAddress | .4 | WLAN MAC | Long |
| aiWlanTxDataBytes | .7 | TX bytes | Long |
| aiWlanRxDataBytes | .10 | RX bytes | Long |

### Entity Tables

- `aruba_iap_device_info_stg_ingest` — Client device info (ArubaAiClientInfoEntity)
- `aruba_iap_ap_info_stg_ingest` — AP info (ArubaAiApInfoEntity)
- `aruba_iap_wlan_traffic_stg_ingest` — WLAN traffic (ArubaAiWlanTrafficEntity)

### ETL Outputs

- `ClientTrafficHourlyCount` — Hourly TX/RX per client
- `ClientUptimeRecord` — Latest uptime per client
- `ClientWlanConnectEvent` — Connect/disconnect events
- `ClientWlanMetricEvent` — SNR samples
- `ApTrafficHourlyCount` — Hourly AP WLAN traffic
- `ApRebootWeeklyCount` — Weekly AP reboot counts

---

## TP-Link Deco

### Protocol: HTTP REST API

TP-Link Deco devices expose a JSON-based API at the device's IP address. Communication involves:

1. **Authentication**: POST login request with encrypted password
2. **Session management**: Extract `stok` (session token) and `seq` (sequence number) from login response
3. **Data retrieval**: GET/POST requests with encrypted payload using the session token

### Auth Flow

1. Request login page to get RSA public key
2. Encrypt password using custom algorithm (`WebEncryptor`)
3. POST login with encrypted credentials via `WebRequestEncrypted`
4. Receive `stok` (session token) and `seq` in response
5. Include `stok` in subsequent requests as cookie/header

### Credentials

Stored as `WebUiCredentials` in `device_auth_data_web`:
```json
{"username":"admin","password":"<encrypted>","host":"<ip>"}
```

### Data Collected

- **Device Info**: Model, firmware, MAC, IP, uplink status (via `DeviceInfoRaw`)
- **Client Info**: Connected clients with MAC, IP, connection status, signal (via `ClientInfoRaw`)
- **WLAN Info**: SSID, band (2.4GHz/5GHz), mode (host/guest/backhaul), encryption (via `WlanInfoRaw`)

### Entity Tables

- `tplink_deco_device_info_stg_ingest` — Device info (DeviceInfoEntity)
- `tplink_deco_client_device_wlan_stg_ingest` — Client device/WLAN info (ClientDeviceInfoEntity)

### ETL Outputs

- `ApInfo` — Normalized AP information
- `ApConnLostCnt` — AP connection lost count
- `IpNormalized` — Normalized IP entries
- `ClientConnectionEvent` — Client connect/disconnect events
- `ClientHourlyTraffic` — Client hourly traffic
- `ClientNormalized` — Normalized client info
- `InterfaceNormalized` — Normalized interface info

---

## iGate GW240

### Protocol: HTTP + SNMP

iGate GW240 uses a hybrid approach:
- **HTTP API** for authentication and WiFi station list retrieval
- **SNMP** for interface/PHY information

### HTTP Auth

- HTTP Basic auth with Base64-encoded credentials (`uid=%s; psw=%s`)
- Session management via header-based session tokens

### Credentials

Stored as `IngestionCredentials` in `device_auth_data_web`:
```json
{
  "user":"<username>",
  "pass":"<password>",
  "host":"<ip>",
  "snmpCred":{"address":"<ip>","community":"public","timeout":60000,"retries":5}
}
```

### Data Collected

- `StatusWifiStationRaw` — WiFi station status (connected clients)
- `SnmpIfTablePhyInfoResponseRaw` — Interface PHY information via SNMP

### Entity Table

- `igate_gw240_status_wifi_station_stg_ingest` — WiFi station raw data (StatusWifiStationEntity)

### ETL Outputs

- `ClientWlanConnectEvent` — Connect events from WiFi station data

---

## RFC1213 (Standard SNMP MIB-II)

### Protocol: SNMP

Any SNMP-enabled network device supporting standard MIB-II (RFC 1213) can be polled.

### Interface Table (`SnmpIfTableRequest`)

RFC OID: `1.3.6.1.2.1.2.2.1.` (ifTable)

| Column | OID Suffix | Field | Type |
|---|---|---|---|
| ifIndex | .1 | Interface index | Integer |
| ifDescr | .2 | Interface description | String |
| ifPhysAddress | .6 | Physical/MAC address | Long |
| ifAdminStatus | .7 | Admin status (1=up, 2=down) | String |
| ifOperStatus | .8 | Operational status | String |
| ifInOctets | .10 | Input octets | Long |
| ifOutOctets | .16 | Output octets | Long |

### IP Address Table (`SnmpIpAddrTableRequest`)

RFC OID: `1.3.6.1.2.1.4.20.1.` (ipAddrTable)

| Column | OID Suffix | Field | Type |
|---|---|---|---|
| ipAdEntAddr | .1 | IP address | Integer |
| ipAdEntIfIndex | .2 | Associated interface index | Integer |

### Entity Table

- `rfc1213_iftable_traffic_stg_ingest` — Interface traffic data (IftableTrafficEntity)

### ETL Outputs

- `TrafficHourlyCount` — Hourly traffic summaries (in/out bytes) per interface

---

## Common Data Types Collected

| Data Type | Description | Sources |
|---|---|---|
| Client MAC | MAC address of connected client devices | Aruba, Deco, iGate |
| Client IP | IP address assigned to client | Aruba, Deco |
| Client Name | Hostname of client device | Aruba |
| Signal Strength (SNR) | Signal-to-noise ratio in dB | Aruba |
| Traffic (TX/RX) | Bytes transmitted and received | Aruba, RFC1213, Deco |
| Connection Events | Client connect/disconnect with timestamps | Aruba, Deco, iGate |
| Uptime | Device uptime in seconds | Aruba, Deco |
| AP Info | Model, firmware, status | Aruba, Deco |
| Reboot Events | Count of AP reboots (per week) | Aruba |
| Interface Stats | Per-interface traffic counters | RFC1213 |
| SSID/WLAN Info | WiFi network names and bands | Aruba, Deco |
