## Context

The `ClientInfoController` serves two endpoints:
- `getList.do` — paginated list of all known devices (uses `DeviceDimRepo.findAll()`)
- `connection/getInfo.do` — paginated connection history for a device (uses `DeviceWlanConnectionsFactRepo`)

Both currently hard-code `15` as the page size via `PageRequest.of(page - 1, 15)`. The connection endpoint returns rows in database insertion order (the default when no `Sort` is specified), which makes it hard to find the most recent records first.

The page size is only controllable by editing code and redeploying. The sort order is default (unspecified).

## Goals / Non-Goals

**Goals:**
- Add an optional `size` query parameter to both endpoints, defaulting to 15 with a maximum cap (100)
- Enforce `ORDER BY deviceKey ASC, eventTimestamp DESC` on `connection/getInfo.do`
- Preserve the `size` parameter in pagination URLs in both Thymeleaf templates
- Document the new parameter and enforced sorting in the API reference spec

**Non-Goals:**
- No changes to `getList.do` sorting — client list remains in default order
- No configurable sort order for `connection/getInfo.do` — sorting is fixed
- No changes to the data model or database schema

## Decisions

1. **Use Spring Data `Sort.by()` instead of a custom JPQL query** — The existing repository methods accept `Pageable`, which carries `Sort`. We construct a `PageRequest` with the desired `Sort` object rather than rewriting the repository layer. This keeps changes minimal.

2. **Default size = 15, max = 100** — 15 matches the current hard-coded value for backward compatibility. The cap of 100 prevents accidental or abusive large-page requests from straining the database.

3. **Page size capped server-side, not silently changed** — If the caller provides a `size` exceeding the maximum, it's clamped to the max rather than returning an error. This is more resilient for clients that may not validate beforehand.

4. **Sort applied in the controller, not the repository** — Repositories accept whatever `Sort` is on the `Pageable`. The controller constructs the appropriate `PageRequest` with the fixed sort, keeping sort logic at the presentation layer where it belongs.

## Risks / Trade-offs

- The enforced sort on `connection/getInfo.do` adds overhead if `device_key` and `event_timestamp` are not indexed together. The `device_key` column is likely indexed (FK), but a composite index may improve performance for large datasets. This is a DBA-level consideration, not a code change.
- Pagination URLs in templates need to carry the `size` parameter explicitly — failure to do so would reset page size to default on navigation. The implementation must ensure all pagination links include `size=${param.size[0]}`.