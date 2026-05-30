# API Reference

## Overview

The admin module exposes REST APIs and web pages for managing the system. Endpoints are available when running with `dev-admin`, `prd-admin`, `dev-scheduler`, or `prd-scheduler` profiles.

Base URL: `http://<host>:<port>/`

---

## Scheduler Management API

Base path: `/api/scheduler`

Managed by `JobSchedulerController`. Provides CRUD operations for Quartz jobs and triggers.

### Get Registered Schedulers

```
GET /api/scheduler/list
```

Returns a list of registered Quartz scheduler beans.

**Response**: `List<SchedulerDTO>`

### Get Triggers

```
GET /api/scheduler/trigger?schedulerId={schedulerId}
```

Returns all triggers registered with the specified scheduler.

**Parameters**:
| Name | Type | Description |
|---|---|---|
| schedulerId | String | Scheduler instance identifier |

**Response**: `List<TriggerDTO>`

### Schedule / Reschedule a Job

```
POST /api/scheduler/trigger/schedule?removeOldTrigger={boolean}
```

Schedules or reschedules a job with a trigger.

**Parameters**:
| Name | Type | Description |
|---|---|---|
| removeOldTrigger | Boolean | Whether to remove existing trigger if re-scheduling |

**Request Body**: `TriggerDTO`

**Response**: `Boolean` (success/failure)

### Pause a Trigger

```
POST /api/scheduler/trigger/pause
```

Pauses a trigger by its identity.

**Request Body**: `TriggerDTO` (containing trigger name and group)

**Response**: `Boolean`

### Resume a Trigger

```
POST /api/scheduler/trigger/resume
```

Resumes a paused trigger.

**Request Body**: `TriggerDTO`

**Response**: `Boolean`

### Delete a Trigger

```
DELETE /api/scheduler/trigger
```

Deletes a trigger by its identity.

**Request Body**: `TriggerDTO`

**Response**: `Boolean`

### Get Job Details

```
GET /api/scheduler/job/detail?schedulerId={schedulerId}
```

Returns all registered job details for a scheduler.

**Parameters**:
| Name | Type | Description |
|---|---|---|
| schedulerId | String | Scheduler instance identifier |

**Response**: `List<JobDetailDTO>`

### Create Job Detail

```
POST /api/scheduler/job/detail
```

Creates a new job detail in the scheduler.

**Request Body**: `JobDetailDTO`
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

**Response**: `Boolean`

### Delete Job Detail

```
DELETE /api/scheduler/job/detail
```

Deletes a job detail from the scheduler.

**Request Body**: `JobDetailDTO`

**Response**: `Boolean`

### Manually Trigger a Job

```
POST /api/scheduler/job/trigger
```

Manually triggers a job immediately (outside its schedule).

**Request Body**: `JobDetailDTO`

**Response**: `Boolean`

### Get Job Class List

```
GET /api/scheduler/job
```

Returns all available job classes registered in the system.

**Response**: `List<JobDetailDTO>`

---

## Client Info Web Pages

Base path: `/web/client`

Managed by `ClientInfoController`. Provides Thymeleaf-rendered HTML pages.

### Get Client List

```
GET /web/client/getList.do?page={page}
```

Returns a paginated list of all known client devices.

**Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| page | Integer | 1 | Page number (1-based, 15 items per page) |

**Response**: HTML page (`clientList.html`)

### Get Client Connection Info

```
GET /web/client/connection/getInfo.do?page={page}&clientKey={clientKey}
```

Returns connection history for a specific client.

**Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| page | Integer | 1 | Page number (1-based, 15 items per page) |
| clientKey | Integer | — | Client key (device dim ID) to filter by |

**Response**: HTML fragment (`clientConnInfo.html`) — suitable for HTMX/partial page updates

---

## Modem Auth Web Pages

Base path: `/web/modem/auth`

Managed by `ModemAuthController`. Provides CRUD for device authentication credentials.

### Get Auth Info List

```
GET /web/modem/auth/getListAuthInfo.do?page={page}&updateId={updateId}&result={result}
```

Returns a paginated list of authentication records.

**Parameters**:
| Name | Type | Description |
|---|---|---|
| page | Integer | Page number (1-based, 20 items per page) |
| updateId | Integer | Optional ID to pre-fill edit form |
| result | Boolean | Optional operation result notification |

**Response**: HTML page (`modemAuth.html`)

### Create / Update Auth Info

```
POST /web/modem/auth/postModemAuthInfo.do
```

Creates or updates an authentication credential record.

**Request Body**: `AuthData` (form-encoded)
- `id`: Integer (null for new records)
- `dataClass`: String (credential type class name)
- `data`: String (JSON-serialized credential fields)
- `tempData`: String (optional temporary data/tokens)

**Response**: Redirect to list page with `result=true/false`

### Delete Auth Info

```
POST /web/modem/auth/deleteModemAuthInfo.do
```

Deletes an authentication record.

**Parameters**:
| Name | Type | Description |
|---|---|---|
| id | Integer | Auth record ID to delete |

**Response**: Redirect to list page with `result=true/false`

---

## DTO Reference

### JobDetailDTO

| Field | Type | Description |
|---|---|---|
| schedulerId | String | Target scheduler instance |
| jobClassName | String | Fully-qualified job class name |
| jobNm | String | Job name |
| jobGr | String | Job group |
| isDurable | Boolean | Whether job survives without triggers |
| requestsRecovery | Boolean | Whether job recovers on scheduler restart |

### TriggerDTO

| Field | Type | Description |
|---|---|---|
| schedulerId | String | Target scheduler instance |
| triggerName | String | Trigger name |
| triggerGroup | String | Trigger group |
| jobName | String | Associated job name |
| jobGroup | String | Associated job group |
| cronTriggerCronExpression | String | Cron expression for scheduling |
| cronTriggerTimeZone | String | Timezone for cron (e.g., Asia/Saigon) |
| cronTrigger | Boolean | Whether this is a cron trigger |
| simpleTrigger | Boolean | Whether this is a simple trigger |
| dailyTimeIntervalTrigger | Boolean | Whether this is a daily interval trigger |

### SchedulerDTO

Contains scheduler instance metadata (name, status, etc.).
