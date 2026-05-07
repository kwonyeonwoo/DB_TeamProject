# Project Guide

## Directory Layout

- `docs/source`: original source documents and working drafts
- `docs/review`: validation reports, conflict lists, and missing decision logs
- `docs/normalized`: normalized specs used as implementation contracts
- `backend`: backend source code location
- `tests`: test code location

# AGENTS.md

## Project Goal

This project is a small-to-medium web service backend.
The agent must follow a specification-first workflow.

The current phase is document validation and backend planning, not full implementation unless explicitly requested.

## Authoritative Documents

The authoritative source documents are located in:

- `docs/source/user-flow.md`
- `docs/source/requirements.md`
- `docs/source/screen-design.md`
- `docs/source/api-spec.md`
- `docs/source/erd.md`
- `docs/source/logical-schema.md`
- `docs/source/physical-schema.md`
- `docs/source/dbml.md`

When documents conflict, do not guess.
Report the conflict clearly and stop before implementation.

## Work Phases

The agent must follow these phases in order:

1. Document inventory
2. Cross-document consistency validation
3. Missing decision detection
4. Normalized specification generation
5. Implementation planning
6. Backend code generation
7. Test generation
8. Verification and review

Do not skip phases.

## Hard Rules

1. Do not implement backend code during document validation.
2. Do not invent requirements, API fields, database columns, roles, permissions, or business rules.
3. If a requirement is ambiguous, write it under "Open Questions" instead of guessing.
4. If API and DB schema conflict, report the conflict.
5. If screen design implies behavior that is missing from requirements, report it.
6. If user flow implies a state transition that is not represented in DB/API, report it.
7. If ERD, logical schema, and physical schema differ, report the exact mismatch.
8. Prefer small, staged implementation plans.
9. Every future backend feature must include tests.
10. Every future API implementation must be traceable to requirements, API spec, DB schema, and acceptance criteria.

## Output Rules

For document validation, output Markdown reports under:

- `docs/review/01-document-validation-report.md`
- `docs/review/02-spec-conflict-list.md`
- `docs/review/03-missing-decisions.md`

For normalized specifications, output Markdown files under:

- `docs/normalized/product-spec.md`
- `docs/normalized/feature-list.md`
- `docs/normalized/domain-model.md`
- `docs/normalized/api-contract.md`
- `docs/normalized/db-schema-contract.md`
- `docs/normalized/auth-policy.md`
- `docs/normalized/acceptance-criteria.md`
- `docs/normalized/implementation-plan.md`
- `docs/normalized/naming-convention.md`

## Validation Checklist

When validating documents, check all of the following:

### Requirements

- Are all functional requirements clear?
- Are CRUD operations explicitly defined?
- Are user roles and permissions clear?
- Are validation rules stated?
- Are error cases stated?
- Are non-functional requirements stated where needed?

### User Flow

- Does every major user flow map to one or more requirements?
- Does every flow have a start state, user action, system response, and end state?
- Are exceptional flows covered?

### Screen Design

- Does every screen action map to an API or backend behavior?
- Are form fields, validation rules, and error messages clear?
- Are hidden backend requirements implied by UI actions?

### API

- Does every endpoint map to a requirement?
- Are request and response fields defined?
- Are HTTP methods appropriate?
- Are status codes defined?
- Are authentication and authorization requirements defined?
- Are error response formats consistent?

### Database

- Does every entity in the ERD appear in the logical schema?
- Does every logical schema element appear in the physical schema?
- Are primary keys, foreign keys, unique constraints, and indexes defined?
- Are nullable and non-nullable fields consistent?
- Are enum/status fields clearly defined?
- Are cascade rules clear?

### Cross-Document Consistency

- Requirement ↔ User Flow
- Requirement ↔ Screen Design
- Requirement ↔ API
- API ↔ DB Schema
- ERD ↔ Logical Schema
- Logical Schema ↔ Physical Schema
- User Flow ↔ API
- Screen Design ↔ API

## Report Format

Use the following severity levels:

- BLOCKER: Must be fixed before implementation.
- MAJOR: Should be fixed before implementation.
- MINOR: Can be fixed during implementation.
- QUESTION: Needs user decision.

Each issue must include:

- ID
- Severity
- Related document
- Problem
- Why it matters
- Suggested fix
- Required user decision, if any

## Coding Rules for Future Phases

When implementation is explicitly requested:

1. Generate an implementation plan before editing code.
2. Implement one feature group at a time.
3. Include tests for every feature.
4. Run available tests.
5. Report changed files.
6. Report remaining risks.
7. Do not modify unrelated files.
8. Do not change deployment, secrets, or production settings unless explicitly requested.