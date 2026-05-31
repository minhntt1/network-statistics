## 1. Controller Changes

- [x] 1.1 Add `size` parameter to `getClientList()` — accept optional `@RequestParam(required = false) Integer size`, default to 15, cap at 100
- [x] 1.2 Add `size` parameter to `getClientConnInfo()` — same default/cap logic
- [x] 1.3 Inject `Sort.by("deviceKey").ascending().and(Sort.by("eventTimestamp").descending())` into the `PageRequest` used by `getClientConnInfo()`

## 2. Template Updates

- [x] 2.1 Update `clientList.html` pagination links to include `size=${param.size[0]}` where applicable
- [x] 2.2 Update `clientConnInfo.html` pagination links to include `size=${param.size[0]}` where applicable
- [x] 2.3 Update the JavaScript `loadConnections()` function in `clientList.html` to pass the `size` parameter

## 3. Spec Update

- [x] 3.1 Update `openspec/specs/api-reference/spec.md` to document the new `size` parameter and enforced sort order on `connection/getInfo.do`
