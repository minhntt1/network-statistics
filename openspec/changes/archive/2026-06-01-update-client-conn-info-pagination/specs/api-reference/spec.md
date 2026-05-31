## MODIFIED Requirements

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