# Client Connection Pagination

## Purpose

Defines the pagination behavior for the client connection history view (`connection/getInfo.do`), including model attributes, page button rendering, and navigation constraints.

## Requirements

### Requirement: Client connection pagination uses raw SQL model attributes
The `connection/getInfo.do` endpoint SHALL expose the following model attributes instead of Spring Data `Page`:
- `currentPage` (int): The current 1-based page number
- `listConnections` (List): The records for the current page
- `pages` (long[]): Array of visible page numbers for the pagination bar

#### Scenario: Pagination metadata is available in model
- **WHEN** a request is made to `connection/getInfo.do` with `page=2` and `size=15`
- **THEN** the model SHALL contain `currentPage=2`, `listConnections` with records for page 2, and `pages` with the visible page number array

### Requirement: Active page is highlighted in pagination
The template SHALL highlight the page button where the page number equals `currentPage`.

#### Scenario: Active page button has active class
- **WHEN** the `connectionsFragment` is rendered with `currentPage=3` and `pages=[1,2,3,4,5]`
- **THEN** the page button for page 3 SHALL have the `active` CSS class
- **AND** other page buttons SHALL NOT have the `active` CSS class

### Requirement: Pagination navigation hides First/Last links
The pagination navigation rendered by the `connectionsFragment` SHALL NOT include First or Last page links. Navigation SHALL be limited to numbered page buttons from `pages`.

#### Scenario: First/Last links are absent
- **WHEN** the `connectionsFragment` is rendered with more than 1 page
- **THEN** the HTML output SHALL contain no link labeled "First" or "Last"