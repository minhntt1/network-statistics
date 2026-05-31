## Why

The client info controller currently hard-codes a page size of 15 records and returns connection data in database insertion order, making it difficult to browse large result sets and find the most recent connections for a given client.

## What Changes

- Add an optional `size` request parameter to `getList.do` and `connection/getInfo.do` that controls the number of results per page (defaults to 15 if not provided, capped at a maximum to prevent abuse)
- Enforce fixed sorting on `connection/getInfo.do` by `deviceKey asc, eventTimestamp desc` — this ordering is not configurable by the caller
- Update the affected spec (api-reference) to document the new parameter and the fixed ordering

## Capabilities

### New Capabilities
- (none)

### Modified Capabilities
- `api-reference`: Document the new `size` parameter on client info endpoints and the fixed sort order on `connection/getInfo.do`

## Impact

- `ClientInfoController.java`: Add `@RequestParam(required = false) Integer size` to both endpoints, apply sorting via `Sort.by()` for `connection/getInfo.do`
- `DeviceWlanConnectionsFactRepo.java`: Update `findAll(Pageable)` and `findByKeyDeviceKey()` queries to use the sort passed in via `Pageable`
- `clientConnInfo.html`: Add `size` parameter to pagination URL templates so page size is preserved across navigation
- `clientList.html`: Add `size` parameter to pagination URL templates so page size is preserved across navigation
- `openspec/specs/api-reference/spec.md`: Document new `size` parameter and enforced sort order
- No new dependencies or external system changes