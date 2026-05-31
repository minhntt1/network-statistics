## 1. Controller: Add currentPage to model

- [x] 1.1 Add `currentPage` (the 1-based `page` variable) to the model in `getClientConnInfo`

## 2. Template: Update data iteration and pagination

- [x] 2.1 Replace `${connections.content}` iteration with `${listConnections}` in the Thymeleaf template
- [x] 2.2 Fix row numbering: replace `${connections.number * connections.size + iterStat.index + 1}` with `(currentPage - 1) * pageSize + iterStat.index + 1` or use the loop index directly since page size is known
- [x] 2.3 Replace the complex page window logic with a simple `${pages}` iteration — for each page number in `pages`, render a link and add `active` class when `page == currentPage`
- [x] 2.4 Update Previous link to use `currentPage - 1` (show only if first page in `pages` > 1)
- [x] 2.5 Update Next link to use `currentPage + 1` (show only if last page in `pages` < total available pages — may need to pass totalPages or derive from count)
- [x] 2.6 Remove First and Last page links from the pagination navigation

## 3. Verify

- [x] 3.1 Verify template compiles without Spring Data Page references
- [x] 3.2 Verify First/Last links are removed from rendered pagination
