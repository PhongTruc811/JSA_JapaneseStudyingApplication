# Japanese Alphabet Quiz

## Project context

- This is a Java 24 desktop application built with Maven and Swing for learning Japanese kana, vocabulary, kanji, and grammar through lessons and quizzes.
- Production code is under `src/main/java/com/mescode/japanese/`; tests are under `src/test/java/com/mescode/japanese/`.
- Keep responsibilities in their existing packages: `app` for startup and navigation, `view` for Swing UI, `controller` for UI coordination, `service` for learning rules, `repository` for data access, and `model` for domain data.
- Bundled learning content and other assets live in `src/main/resources/`. Load them from the classpath so the packaged application works outside the source tree.
- For broader project context, consult `docs/personal/Planning` only when the task needs it.

## Working agreements

- Make focused changes that follow nearby code conventions. Preserve existing Japanese learning content and Vietnamese UI text unless the task calls for changing them.
- Keep Swing UI updates on the Event Dispatch Thread. Avoid blocking it with file I/O, network calls, or long-running work.
- When changing quiz rules, navigation, persistence, or resource loading, check the corresponding tests and update or add a meaningful test if behavior changes.
- Do not add dependencies, external services, or database requirements without a task-specific reason. The current `application.properties` disables database access by default.
- Update planning notes in `docs/personal/Planning` only when a task changes a documented decision or a tracked milestone; do not add routine implementation notes for every small edit.

## Verification

- After changing Java code or learning data, run the relevant local tests automatically without asking for confirmation. Fix failures caused by the change and rerun affected tests.
- Use `mvn -Dtest=TestClassName test` for a focused test or `mvn test` when changes span several components. Do not run the full suite repeatedly without a concrete reason.
- For documentation-only changes, check the changed text and links; running Java tests is unnecessary.
- If a test needs unavailable credentials, an external service, or access beyond the local workspace, report that limitation and the checks that did run.
- In the final response, state what changed, which checks ran, and any unresolved limitation.

<!-- codebase-memory-mcp:start -->
# Codebase Knowledge Graph (codebase-memory-mcp)

This project uses codebase-memory-mcp to maintain a knowledge graph of the codebase.
ALWAYS prefer MCP graph tools over grep/glob/file-search for code discovery.
If the project is not indexed, run `index_repository` before code discovery.

## Priority Order
1. `search_graph` — find functions, classes, routes, variables by pattern
2. `trace_path` — trace who calls a function or what it calls
3. `get_code_snippet` — read specific function/class source code
4. `query_graph` — run Cypher queries for complex patterns
5. `get_architecture` — high-level project summary

## When to fall back to grep/glob
- Searching for string literals, error messages, config values
- Searching non-code files (Dockerfiles, shell scripts, configs)
- When MCP tools are unavailable or return insufficient results

## Examples
- Find a handler: `search_graph(name_pattern=".*OrderHandler.*")`
- Who calls it: `trace_path(function_name="OrderHandler", direction="inbound")`
- Read source: `get_code_snippet(qualified_name="pkg/orders.OrderHandler")`
<!-- codebase-memory-mcp:end -->
