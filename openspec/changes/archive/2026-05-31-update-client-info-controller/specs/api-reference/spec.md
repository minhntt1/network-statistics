## MODIFIED Requirements

### Requirement: API reference documents client info endpoints
The API reference SHALL document the client information endpoints exposed by `ClientInfoController`:
- Client listing and detail queries
- Connection history and status
- Filtering and pagination parameters, including configurable page `size` (default 15, max 100)
- The `connection/getInfo.do` endpoint SHALL return results sorted by `deviceKey ASC, eventTimestamp DESC`

#### Scenario: Client info endpoints are documented
- **WHEN** a developer reads the API reference
- **THEN** they SHALL find the client information endpoints with available query parameters including `page` and `size`
- **THEN** they SHALL find that `connection/getInfo.do` results are sorted by `deviceKey ASC, eventTimestamp DESC`