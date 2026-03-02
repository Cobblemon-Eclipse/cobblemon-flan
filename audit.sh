#!/bin/bash
# Bug audit of Cobblemon Flan using OpenAI Codex
# Run from: C:\cobblemon\eclipse\AI\RunNBun\cobblemon-flan\

codex exec --full-auto \
  "You are auditing the Cobblemon Flan Minecraft Fabric mod (Kotlin/Java, MC 1.21.1, Cobblemon 1.7.3+).

Cobblemon Flan integrates Cobblemon with the Flan claims mod, controlling Pokemon interactions within claimed regions (catching, battling, riding).

DO NOT modify any code. Only produce a markdown report.

Read ALL .kt and .java source files under src/main/ and analyze for:

1. **CRITICAL bugs** - Permission bypasses allowing catching in protected areas, crashes
2. **Concurrency/Thread Safety** - Race conditions in permission checks
3. **Resource Leaks** - Event listeners never unregistered
4. **Logic Errors** - Wrong permission checks, bypass logic errors
5. **Edge Cases** - Player at claim boundary, overlapping claims, null claim data
6. **Memory Leaks** - Caches growing unbounded

Key subsystems to audit:
- listener/ (CobblemonFlanEventListener)
- permission/ (FlanPermissionChecker)
- api/ (FlanBypass)
- config/ (CobblemonFlanConfig)
- mixin/ (RidingStateHandlerMixin)
- CobblemonFlan.kt (entry point)

For each bug found, report:
- Severity: CRITICAL / HIGH / MEDIUM / LOW
- File and line number
- Description of the bug
- What could go wrong (impact)
- Suggested fix approach (brief)

Write the full report to codex-audit-report.md in the project root."
