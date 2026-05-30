## Context

The project currently has a `readme.md` that describes basic setup, tech stack, development procedures, and deployment instructions. However, it lacks detailed documentation on the system architecture, data pipeline flows, device vendor integrations, database schemas, configuration profiles, and REST API endpoints. As new device families (Aruba, TP-Link Deco, iGate, RFC1213) are added and the ETL pipeline grows, comprehensive documentation is needed to:

- Onboard new contributors quickly
- Provide operational reference for maintaining and debugging the system
- Document design decisions and conventions for consistency

## Goals / Non-Goals

**Goals:**
- Create a structured documentation hierarchy under `docs/` that is easy to navigate
- Document the ETL pipeline architecture (in → etl → out) per device family
- Document all supported device vendors with protocol details (SNMP OIDs, HTTP endpoints)
- Document database schemas — staging, archive, and ingestion tables for each device type
- Document all Spring configuration profiles and their purposes
- Document deployment procedures for both local and container environments
- Document REST API endpoints exposed by the admin module
- Update `readme.md` to serve as the entry point with links to detailed docs
- Follow Markdown format (no external documentation generators) for simplicity

**Non-Goals:**
- Auto-generating documentation from code (e.g., javadoc, OpenAPI specs)
- Creating a standalone documentation site or wiki
- Documenting every internal class or method (focus on architecture and workflows)
- Changing any application code or configuration

## Decisions

| Decision | Choice | Rationale | Alternatives Considered |
|---|---|---|---|
| Documentation format | Markdown (`.md`) | Version-controlled alongside code, universally readable, no build tools needed | AsciiDoc, Sphinx, Docusaurus — overkill for this scope |
| Documentation location | `docs/` directory in repo root | Co-located with source code, conventional location, simple | Separate wiki, external docs site — harder to keep in sync |
| Architecture diagrams | PlantUML (`.puml`) | Already in use (`docs/package_diagram.puml`), version-controllable, text-based | Draw.io, Mermaid — PlantUML integrates with existing tooling |
| Capability docs vs monolithic docs | One `.md` file per capability in `docs/` | Modular, easy to maintain individually, clear separation of concerns | Single monolithic file — harder to navigate and maintain |
| Spec-tracking per capability | OpenSpec spec files per capability | Formalizes requirements, enables traceability from docs to implementation | Ad-hoc docs — no requirement traceability |

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Documentation drifts from code over time | Keep docs close to code in the same repo; update docs as part of development workflow |
| Architecture diagrams become outdated | Diagrams are in PlantUML (text-based, diffable); update as part of feature changes |
| Maintenance burden of multiple doc files | Each doc is focused and scoped to a capability; cross-linking via readme reduces duplication |
