# Cobblemon Flan Audit Report

Scope: Reviewed all Kotlin/Java sources under `src/main/`:
- `src/main/kotlin/com/eclipse/cobblemon/flan/CobblemonFlan.kt`
- `src/main/kotlin/com/eclipse/cobblemon/flan/listener/CobblemonFlanEventListener.kt`
- `src/main/kotlin/com/eclipse/cobblemon/flan/permission/FlanPermissionChecker.kt`
- `src/main/kotlin/com/eclipse/cobblemon/flan/api/FlanBypass.kt`
- `src/main/kotlin/com/eclipse/cobblemon/flan/config/CobblemonFlanConfig.kt`
- `src/main/java/com/eclipse/cobblemon/flan/mixin/RidingStateHandlerMixin.java`

## Findings

### 1) Permission checks fail open on Flan API errors
- Severity: **CRITICAL**
- File: `src/main/kotlin/com/eclipse/cobblemon/flan/permission/FlanPermissionChecker.kt:29-35`
- Description: `canInteract(...)` catches all exceptions from `ClaimHandler.canInteract(...)` and returns `true`.
- Impact: Any exception in Flan claim resolution (invalid claim data, transient state, API regression, null internals) becomes an implicit allow. This can permit catching/battling/riding/send-out/use in protected claims.
- Suggested fix approach: Fail closed for protection checks (`return false` on exception), and log enough context (player, pos, permission) for diagnosis.

### 2) Event handlers also fail open after exceptions
- Severity: **HIGH**
- File:
  - `src/main/kotlin/com/eclipse/cobblemon/flan/listener/CobblemonFlanEventListener.kt:60-79` (catch)
  - `...:86-107` (battle)
  - `...:114-134` (send out)
  - `...:141-157` (ride)
  - `...:164-186` (display case)
  - `...:37-53` (spawn)
- Description: Each listener wraps logic in `try/catch(Exception)` and only logs warning on error, without canceling the event.
- Impact: Any runtime exception in protection logic turns into an allow path. Combined with Finding #1, this is a broad bypass surface for protected actions, including catching in claims.
- Suggested fix approach: For protection events, default to deny/cancel on internal errors. Restrict catch blocks to expected exception types where possible.

### 3) Bypass set is unbounded and not lifecycle-cleared
- Severity: **HIGH**
- File:
  - `src/main/kotlin/com/eclipse/cobblemon/flan/api/FlanBypass.kt:19-50`
  - `src/main/kotlin/com/eclipse/cobblemon/flan/CobblemonFlan.kt:27-29`
- Description: `FlanBypass` stores UUIDs in a global concurrent set with no TTL, no disconnect cleanup, and `clearAll()` is never called despite comment saying it should run on shutdown.
- Impact: Memory growth over long sessions if external integrations forget to remove entries; stale bypasses can persist for the process lifetime (notably integrated server stop/start), unintentionally granting continued protection bypass.
- Suggested fix approach: Register `SERVER_STOPPED` to call `FlanBypass.clearAll()`, and optionally remove bypass on player disconnect and/or add timed bypass tokens.

### 4) `ownerBypass` config flag is dead/unused
- Severity: **MEDIUM**
- File: `src/main/kotlin/com/eclipse/cobblemon/flan/config/CobblemonFlanConfig.kt:70-71`
- Description: `ownerBypass` exists in config but is never read in permission flow.
- Impact: Operators may assume claim owners bypass checks when enabled, but behavior does not change. This can cause misconfiguration and unexpected denials/perm policy drift.
- Suggested fix approach: Either implement owner bypass in permission checks (consult claim ownership) or remove the setting to avoid false expectations.

### 5) Wild spawn protection only handles player-caused spawn events
- Severity: **MEDIUM**
- File: `src/main/kotlin/com/eclipse/cobblemon/flan/listener/CobblemonFlanEventListener.kt:42-45`
- Description: Spawn protection exits unless `cause.entity` is a `ServerPlayerEntity`.
- Impact: If intent is “prevent wild spawns in claims,” non-player-caused spawns are not checked and remain allowed inside claims.
- Suggested fix approach: Define intended behavior clearly; if full claim spawn protection is required, evaluate claim permission regardless of player cause and use an appropriate permission model.

### 6) Battle player type assumption can silently skip checks
- Severity: **MEDIUM**
- File: `src/main/kotlin/com/eclipse/cobblemon/flan/listener/CobblemonFlanEventListener.kt:92-104`
- Description: Battle participants are cast to `ServerPlayerEntity`; non-matching participant types are ignored with no fallback resolution.
- Impact: If Cobblemon battle participants are wrappers/non-entity abstractions in some battle modes, checks may be skipped and protected battles may start.
- Suggested fix approach: Resolve server player via Cobblemon participant API (UUID/entity extraction) rather than direct cast, and deny when participant identity cannot be validated.

### 7) Mixin suppresses all exceptions without telemetry
- Severity: **LOW**
- File: `src/main/java/com/eclipse/cobblemon/flan/mixin/RidingStateHandlerMixin.java:46-49`
- Description: Catch-all `Exception` is silently ignored.
- Impact: Unexpected decode/network state bugs are hidden, making exploit attempts or regressions hard to detect and diagnose.
- Suggested fix approach: Keep `IndexOutOfBoundsException` suppression if needed, but log unexpected exception classes/rates with throttling.

## Concurrency / Thread-Safety Notes
- `FlanBypass` uses `ConcurrentHashMap.newKeySet()`, so basic add/remove/contains are thread-safe.
- No explicit shared mutable state races were found in this code beyond fail-open error paths.

## Resource/Memory Notes
- No listener unregistration path exists, but Fabric lifecycle typically registers once for process lifetime.
- The concrete leak risk observed is the unbounded `FlanBypass` set (Finding #3).