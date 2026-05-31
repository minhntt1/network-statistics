## Why

The `getClientConnInfo` method in `ClientInfoController` was refactored to use raw SQL queries instead of Spring Data JPA `Page` for performance. However, the Thymeleaf template (`clientConnInfo.html`) still references Spring Data `Page` properties (`connections.content`, `connections.number`, `connections.totalPages`, etc.) which are no longer available in the model. The pagination UI needs to be updated to work with the new model attributes (`listConnections` for data, `pages` for page number buttons, `currentPage` to highlight the active page), and the First/Last page links should be removed.

## What Changes

- Update `ClientInfoController.getClientConnInfo` to add `currentPage` (1-based) to the model
- Update `clientConnInfo.html` to iterate `${listConnections}` instead of `${connections.content}`
- Update pagination to render page buttons from `${pages}` array and highlight the button where `page == currentPage`
- Remove the First and Last page links from the pagination navigation

## Capabilities

### New Capabilities
- `client-conn-pagination`: Client connection history pagination using raw SQL, with a `pages` array for page buttons, `currentPage` for highlighting the active page, and `listConnections` for data. No First/Last links.

### Modified Capabilities
- `api-reference`: The `connection/getInfo.do` endpoint pagination model attributes change from Spring Data `Page` fields to `currentPage`, `listConnections`, `pages`.

## Impact

- **ClientInfoController.java**: Add `currentPage` to model
- **clientConnInfo.html**: Rewrite data iteration and pagination section to use new attributes; remove First/Last links
- **api-reference spec**: Document the new pagination model attributes for `connection/getInfo.do`