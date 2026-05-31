## Context

The `getClientConnInfo` endpoint was previously using Spring Data JPA's `Page` abstraction, which returned a `Page<DeviceWlanConnectionsDTO>` with built-in pagination metadata (`totalPages`, `number`, `hasNext`, `hasPrevious`, etc.). Due to performance issues with JPA + Pageable for large datasets, the controller was refactored to use raw SQL queries via `JdbcTemplate`.

Currently the controller passes:
- `pages` — a `long[]` of visible page numbers (from `PaginationUtil.getDefaultPaginationInfo`)
- `listConnections` — a `List<DeviceWlanConnectionsDTO>` of records for the current page

But the Thymeleaf template still references Spring Data `Page` properties like `${connections.content}`, `${connections.number}`, `${connections.totalPages}`, `${connections.isFirst()}`, etc. These no longer exist in the model, breaking the pagination UI.

The `pages` array already contains the complete set of page numbers to render. The only additional metadata needed is `currentPage` to highlight the active page button. The template does not need `totalPages`, `pageSize`, `hasPrevious`, or `hasNext` because the `pages` array is sufficient for rendering the pagination bar.

## Goals / Non-Goals

**Goals:**
- Add `currentPage` (1-based) to the model so the template can highlight the active page button
- Update the template to iterate `${listConnections}` for data
- Update pagination to render buttons from `${pages}` and highlight when `page == currentPage`
- Remove First/Last page links from the navigation

**Non-Goals:**
- Not passing `totalPages`, `pageSize`, `hasPrevious`, `hasNext` to the model (unnecessary since `pages` carries the display info)
- Not changing the SQL queries or `PaginationUtil`
- Not modifying the `clientList` endpoint

## Decisions

1. **Only add `currentPage`**: The `pages` array from `PaginationUtil` already carries the page buttons to display. The template only needs to know the current page to highlight it. No additional pagination metadata is required.

2. **First/Last removal**: The user explicitly stated First/Last links should not be shown. The `pages` array window provides sufficient navigation context.

3. **Model attribute naming**: Use `currentPage` (int, 1-based) — simple and clear.

## Risks / Trade-offs

- **Limited pagination info**: Without `totalPages` in the model, the template cannot display "showing X of Y" style text. Mitigation: the user confirmed this is not needed.
- **Previous/Next visibility**: The template will need to determine whether to show Previous/Next based on the `pages` array boundaries (e.g., if first page number in `pages` is 1, hide Previous). Mitigation: this is a straightforward check against the first and last values in the `pages` array.