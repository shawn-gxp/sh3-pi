# Migration Phase 1 Bug Report

**Scope:** Phase 0–1c plugin architecture + Omron restructure  
**Range reviewed:** `24d2315` → `HEAD` (`5f2a117`)  
**Date:** 2026-07-21  
**Status:** open (20 findings)

| Severity | Count |
|----------|------:|
| bug | 7 |
| suggestion | 10 |
| nit | 3 |

---

## Summary

Phase 0–1c introduces a clean `DevicePlugin` / registry seam, relocates `omron_bp` → `brands/omron`, and cuts `job_pair()` over to `OmronPlugin` with a late circular-import fix (lazy `omron_bridge` import). The mechanical move and pair cutover look directionally correct, but Phase 1b is incomplete (Omron entries still live in both `profiles.py` and `brands/omron/profiles.py`), registration is tightly coupled to package import side effects, and production tree pollution (one-shot migration scripts, stale `omron_bp` pycache, stale path strings) leaves several footguns.

**Dominant risks:**

1. Dual profile sources of truth
2. Import / lifecycle fragility
3. Intentionally unfinished dual path: `job_sync` / hub still bypass the plugin while `run_session` lacks the field-tuned FE4A retry loop

**Verdict:** Directionally sound, not production-clean yet.

---

## Recommended fix order

1. Delete inline Omron blocks from `profiles.py` (single source of truth).
2. Break the profiles ↔ brand import cycle (shared types module or no reverse import).
3. Make plugin registration explicit (`ensure_plugins_loaded()`), not a side effect of profile imports.
4. Honor `PairResult.ok` in `job_pair`.
5. Fix test `sys.path`; remove one-shot scripts + empty `omron_bp/`.
6. Port FE4A retry into `run_session` *before* any `job_sync` cutover.

---

## Issues

### Issue 1 — Severity: bug

- **File:** `medical_ble_toolkit/profiles.py:344`
- **Description:** Phase 1b was supposed to *remove* the inline `"omron"` / `"hem7143t1"` entries from the `PROFILES` literal and re-inject them only via `OMRON_PROFILES`. Both still exist: the dict still defines them at lines 344–371, and lines 386–387 also `update()` from `brands/omron/profiles.py`. That is a dual source of truth. `PROFILES.update()` overwrites matching keys but never deletes keys removed from `OMRON_PROFILES`, so a future edit that drops a key only from the brand file silently leaves the stale inline entry. Pre/post split snapshots can be identical for the wrong reason (same effective content), so the “verified byte-identical” check did not prove the split happened.
- **Suggestion:** Delete the inline Omron blocks from the `PROFILES` literal entirely; keep a single definition in `brands/omron/profiles.py` plus the `update()` call. Re-snapshot and assert that removing a key from `OMRON_PROFILES` actually removes it from `PROFILES`.
- **Status:** open

### Issue 2 — Severity: bug

- **File:** `medical_ble_toolkit/brands/__init__.py:9`
- **Description:** `brands/__init__.py` eagerly does `from .omron import plugin as _omron_plugin`, and `profiles.py` loads Omron profiles via `from .brands.omron.profiles import OMRON_PROFILES`. Any import of `medical_ble_toolkit.profiles` therefore executes brand plugin registration as a side effect. Failure to import/register OmronPlugin (syntax error, missing dep, double-register, etc.) breaks *all* profile access — including Beurer/Nipro — even when the caller only wanted `get_profile("beurer_bp")`. Pure catalog data is no longer independent of the plugin lifecycle.
- **Suggestion:** Keep `brands/__init__.py` free of registration side effects for data-only imports, or load Omron hub profiles without going through a package `__init__` that registers plugins (e.g. explicit `ensure_plugins_loaded()` called only from `ble_jobs` / hub startup). Profile aggregation should not require a live plugin registry.
- **Status:** open

### Issue 3 — Severity: bug

- **File:** `medical_ble_toolkit/brands/omron/profiles.py:4`
- **Description:** Brand-local profiles import `DeviceProfile` from the central catalog (`medical_ble_toolkit.profiles`), while the central catalog imports brand-local profiles. The cycle is only safe because the mid-module import in `profiles.py` runs *after* the dataclass is defined. Reordering imports, lazy-loading differently, or adding any top-level use of `PROFILES` / `OMRON_PROFILES` before the class exists will reintroduce `ImportError: cannot import name 'DeviceProfile'`. This is the same class of fragility that forced the plugin↔`omron_bridge` lazy-import fix.
- **Suggestion:** Move the shared hub `DeviceProfile` dataclass to a cycle-free module (e.g. `medical_ble_toolkit/profile_types.py`), or keep Omron hub entries defined without importing back into the aggregator’s module. Do not rely on “import halfway down the file” as the cycle breaker.
- **Status:** open

### Issue 4 — Severity: bug

- **File:** `medical_ble_toolkit/brands/omron/tests/test_token_unlock.py:9`
- **Description:** `ROOT = Path(__file__).resolve().parents[2]` was correct-ish when the test lived under `omron_bp/tests/` (landed on `medical_ble_toolkit/`). After the move to `brands/omron/tests/`, `parents[2]` is `medical_ble_toolkit/brands/`, which is not a useful `sys.path` entry for `import medical_ble_toolkit...`. Standalone runs / some pytest path layouts will fail or resolve the wrong tree. Repo-root would be `parents[4]`.
- **Suggestion:** Point ROOT at the repository root (`parents[4]`) or drop the path workaround and rely on package install / pytest `pythonpath`. Re-run the omron unit tests from a clean env to confirm.
- **Status:** open

### Issue 5 — Severity: bug

- **File:** `medical_ble_toolkit/core/pairing.py:17`
- **Description:** `pair_device(brand_id, ...)` calls `get_plugin(brand_id)` but never imports `medical_ble_toolkit.brands` (or otherwise ensures plugins are registered). The registry starts empty. Any caller that uses the public `core.pairing` API without a prior side-effect import gets `KeyError: No plugin registered for brand_id='omron'...` even though Omron is “supported”. `job_pair` works only because `ble_jobs.py` imports `medical_ble_toolkit.brands` at module level — that invariant is not enforced by the pairing API the docs advertise as the stable path.
- **Suggestion:** Have `get_plugin` / `pair_device` call a single `ensure_registered()` that imports `medical_ble_toolkit.brands` lazily, or document and test that registration is mandatory and fail with a clearer “plugins not loaded” error.
- **Status:** open

### Issue 6 — Severity: bug

- **File:** `medical_ble_web/ble_jobs.py:391`
- **Description:** `job_pair` does `await get_plugin("omron").pair(...)` and ignores the returned `PairResult`. It then always `upsert_device(..., paired=True)` if no exception was raised. Today `OmronPlugin.pair` always returns `ok=True` after `pair_omron`, so this is latent — but the whole point of `PairResult.ok` is soft-failure signaling. The first time a plugin returns `ok=False` without raising (reasonable for recoverable bond noise), the hub will mark the device paired and start auto-sync against an unpaired cuff.
- **Suggestion:** Check `result.ok` (and optionally `detail`) before persisting `paired=True`; treat `ok=False` like a failed pair. Prefer `core.pairing.pair_device` once it owns shared post-pair policy.
- **Status:** open

### Issue 7 — Severity: bug

- **File:** `medical_ble_toolkit/omron_bridge.py:46`
- **Description:** ImportError text still says Omron is “bundled under `medical_ble_toolkit/omron_bp/`” (also in `parsers/omron.py:49`). After the move that path has no source package (only leftover `__pycache__`). Operators debugging a failed deploy will look in the wrong place and may conclude the install is corrupt when the real package is `brands/omron`.
- **Suggestion:** Update error strings (and remaining user-facing docs) to `medical_ble_toolkit/brands/omron/`.
- **Status:** open

### Issue 8 — Severity: suggestion

- **File:** `medical_ble_web/ble_jobs.py:573`
- **Description:** Incomplete dual path by design (migration plan defers it): `job_pair` uses the plugin registry, but `job_sync` (and therefore hub `_hub_run_session`) still calls `read_omron` / `flatten_readings` directly and keeps the 2-attempt FE4A / “parent service” retry loop. `OmronPlugin.run_session` is a single-shot path without that recovery. Anyone “finishing” the cutover by swapping `job_sync` to `plugin.run_session()` will silently drop field-tuned reliability unless that logic is ported first.
- **Suggestion:** Either port the FE4A retry into `OmronPlugin.run_session` (or a shared helper both call) before cutting over sync, or assert in code/comments at the plugin that `run_session` is not hub-safe yet. Add a failing test or checklist item so the dual path cannot be “cleaned up” accidentally.
- **Status:** open

### Issue 9 — Severity: suggestion

- **File:** `medical_ble_toolkit/core/pairing.py:1`
- **Description:** Module docstring claims this layer will “persist to DB, handle repair flag” and leave only the brand handshake to the plugin. Implementation only does `get_plugin(...).pair(...)`. DB upsert, session rows, `paired=True`, nipro registry, and auto-sync start all remain in `ble_jobs.job_pair`. The API looks like a complete pairing orchestrator but is not; callers who trust the docstring will skip persistence.
- **Suggestion:** Narrow the docstring to current behavior, or actually move shared post-pair persistence here and have `job_pair` call it for Omron (and later other brands).
- **Status:** open

### Issue 10 — Severity: suggestion

- **File:** `edit_profiles.py:1`
- **Description:** One-shot migration utilities were committed to the repo root: `edit_profiles.py`, `fix_external.py`, `fix_internal_imports.py`, `hash_pre.py`, `hash_post.py`. Re-running `edit_profiles.py` is especially dangerous: its regex only partially matches the Omron block and it *appends* another `from .brands.omron.profiles import OMRON_PROFILES` / `PROFILES.update(...)` before `list_profiles()`, which would duplicate side-effect imports. These scripts are not part of the runtime product.
- **Suggestion:** Delete them from `main` (keep in the migration commit history / a branch note), or quarantine under `scripts/migration/` with a README “do not re-run”. Prefer that verification artifacts live only under `migration_verify/` if retained.
- **Status:** open

### Issue 11 — Severity: suggestion

- **File:** `medical_ble_toolkit/omron_bp/`
- **Description:** After `git mv`, an empty `medical_ble_toolkit/omron_bp/` tree remains with only `__pycache__` bytecode (no `.py` sources). That confuses greps, backup tools, and anyone following old docs. Depending on Python import edge cases / leftover pyc, it can also look like the old package still exists.
- **Suggestion:** Remove `medical_ble_toolkit/omron_bp/` entirely (including pycache) and ensure `.gitignore` covers `__pycache__`. Grep the tree for `omron_bp` path references in README / SUPPORT_MATRIX / CLI help and update entrypoints to `python -m medical_ble_toolkit.brands.omron`.
- **Status:** open

### Issue 12 — Severity: suggestion

- **File:** `medical_ble_toolkit/brands/omron/plugin.py:30`
- **Description:** `OmronPlugin.pair` / `run_session` always return `ok=True` if the awaited call returns. Soft-success paths inside omron pairing (e.g. pair() error but FE4A visible treated as OK in `pairing/service.py`) already swallow some failures at a lower layer; the plugin never surfaces `detail` (model resolved, force_rebind, FE4A visibility, retry counts). The new result types are effectively unused metadata.
- **Suggestion:** Populate `PairResult.detail` / `SessionResult.detail` with useful diagnostics, and only set `ok=True` when the underlying facade indicates real success. Map known soft-success cases explicitly.
- **Status:** open

### Issue 13 — Severity: suggestion

- **File:** `medical_ble_toolkit/core/pairing.py:17`
- **Description:** Name collision: `medical_ble_toolkit.core.pairing.pair_device(brand_id, mac, model, repair=...)` vs `medical_ble_toolkit.brands.omron.pairing.service.pair_device(address, profile, force_rebind=...)`. Different signatures, same function name, both “pair_device”. Easy to import the wrong one during future refactors (especially while `omron_bridge` still wraps the brand-local one).
- **Suggestion:** Rename the core helper to `pair_with_plugin` / `pair_brand`, or the brand function to `pair_omron_device`, and keep call sites explicit.
- **Status:** open

### Issue 14 — Severity: suggestion

- **File:** `medical_ble_toolkit/core/registry.py:18`
- **Description:** `register()` raises `ValueError` if the same `brand_id` is registered twice. Normal imports are fine (modules execute once), but `importlib.reload`, test isolation, or accidental double import of plugin modules under different paths will crash process startup. There is also no `unregister` / clear for tests.
- **Suggestion:** Make registration idempotent when the same plugin instance (or same class) is re-registered; provide `clear_plugins()` for unit tests; or document that reload is unsupported.
- **Status:** open

### Issue 15 — Severity: suggestion

- **File:** `medical_ble_toolkit/brands/omron/plugin.py:36`
- **Description:** `run_session` defaults (`find_timeout=15.0`, `session_retries=2`) diverge from `read_omron` defaults (`find_timeout=60.0`, `session_retries=3`). Current `job_sync` hardcodes 15/2 to match the plugin defaults, but any direct `run_session` / future cutover that omits kwargs silently changes timeouts vs the facade CLI path. Easy to introduce behavior drift.
- **Suggestion:** Single source of defaults (constants on the plugin or in `omron_bridge`) shared by CLI, plugin, and `job_sync`.
- **Status:** open

### Issue 16 — Severity: nit

- **File:** `medical_ble_web/ble_jobs.py:387`
- **Description:** Redundant `import medical_ble_toolkit.brands` inside `job_pair` when line 22 already imports it at module load. Harmless but signals uncertainty about registration lifecycle (which matches Issue 5).
- **Suggestion:** Keep one clear `ensure_plugins()` call site; drop the inner import.
- **Status:** open

### Issue 17 — Severity: nit

- **File:** `medical_ble_toolkit/profiles.py:386`
- **Description:** Mid-module import and `PROFILES.update` sit awkwardly between the catalog literal and `list_profiles()`, with extra blank lines and no comment that this is an intentional aggregation seam. Future editors will “clean up” imports to the top and may re-break the cycle described in Issue 3.
- **Suggestion:** If the aggregator pattern stays, add a loud comment: “DO NOT move this import to module top — cycle with brands.omron.profiles”. Better: eliminate the cycle (Issue 3).
- **Status:** open

### Issue 18 — Severity: nit

- **File:** `medical_ble_toolkit/brands/omron/profiles.py:19`
- **Description:** Stale notes string still says “Backed by medical_ble_toolkit.omron_bp (23 models).” Migration plan intentionally preserved this for byte-identical verification; post-verification it is user-visible via profile notes and is now wrong.
- **Suggestion:** After dual-path cleanup, update notes to `brands.omron` / current CLI module path.
- **Status:** open

### Issue 19 — Severity: suggestion

- **File:** `medical_ble_toolkit/core/device_plugin.py:42`
- **Description:** `DevicePlugin` declares `brand_id` / `device_class` as annotations on the ABC but does not enforce them (`@property` abstract or `__init_subclass__` check). A broken plugin can register with missing attributes and only fail at `get_plugin` use or when formatting errors. `matches_advertisement` is implemented on Omron but has zero call sites — hub discovery still uses other paths — so the new surface area is untested in production flows.
- **Suggestion:** Validate required class attributes in `register()`. Wire `matches_advertisement` into hub scan matching or mark it explicitly experimental until used.
- **Status:** open

### Issue 20 — Severity: suggestion

- **File:** `medical_ble_toolkit/brands/omron/plugin.py:56`
- **Description:** Module-level `_plugin = OmronPlugin(); register(_plugin)` means import = registration. Combined with Issue 2, test suites that import profiles or parsers mutate global registry state with no teardown. Parallel tests or multi-app embeds share one process-global `_PLUGINS` dict.
- **Suggestion:** Explicit app bootstrap registration, or registry scoped to an application context, plus test fixtures that clear/restore `_PLUGINS`.
- **Status:** open

---

## What looks good

- Clean `DevicePlugin` / registry seam direction
- Mechanical `omron_bp` → `brands/omron` move is coherent
- Lazy import fix for the plugin ↔ `omron_bridge` cycle is the right class of fix
- Intentional deferral of `job_sync` cutover is documented (needs a hard guard so nobody “finishes” it accidentally)

---

## Related docs

- `migration_plan.md`
- `EXECUTION_PLAN.md`
- `migration_verify/` (pre/post move hashes and profile snapshots)
