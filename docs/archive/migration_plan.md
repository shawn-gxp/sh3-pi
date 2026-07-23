# Migration Spec — Phase 0 (Core Plugin Architecture) + Phase 1 (Omron)

**Audience:** Antigravity (executor). This is a mechanical checklist — follow steps in
order, run every verification command, and **stop and report back** if any verification
fails or produces unexpected output. Do not improvise fixes; report the failure instead.

**Repo:** sh3-pi. Working directly on `main` per team decision — this is why every step
below is a separate, individually-revertible commit.

---

## 0. Ground rules (read first)

1. `git status` must be clean before Step 1. If not, stop and report what's dirty.
2. Create a safety tag before touching anything:
   ```
   git tag pre-migration-phase0-1 HEAD
   ```
3. Commit after **every** numbered step below, using the exact commit message given.
   Do not batch multiple steps into one commit — we want one-command rollback
   (`git revert <sha>`) granularity per step.
4. Do not modify, "clean up," reformat, or simplify any file not explicitly named in a
   step. If you notice something that looks like a bug or stale code while working,
   note it in your report — do not fix it inline.
5. Never touch: BLE unlock/protocol byte-level logic, parser decode math, anything
   under `datasheets/`, `phoneblelog/`. These are out of scope for this spec entirely.
6. If a verification step's "expected" output doesn't match reality, **stop immediately,
   do not proceed to the next step**, and report the discrepancy verbatim.

---

## Phase 0 — Core plugin architecture (net-new files only, zero existing files touched)

### Step 0.1 — Create directory + package marker

```bash
mkdir -p medical_ble_toolkit/core
touch medical_ble_toolkit/core/__init__.py
```

### Step 0.2 — Create `medical_ble_toolkit/core/device_plugin.py`

Create this file with exactly this content:

```python
"""
DevicePlugin — the contract every brand adapter implements.

This is the seam between the orchestrator (medical_ble_web/ble_jobs.py) and
brand-specific BLE logic (medical_ble_toolkit/brands/<brand>/). Adding a new
brand means implementing this interface and registering an instance in
brands/__init__.py — no changes to the orchestrator's dispatch logic required.

Do NOT put BLE protocol logic here. Implementations must be thin wrappers
that delegate to the brand's existing, field-proven modules.
"""
from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from enum import Enum
from typing import Any, Optional


class DeviceClass(str, Enum):
    """Mirrors medical_ble_toolkit.hub.policy's STREAM/WINDOWED/ALWAYS semantics."""
    STREAM = "stream"      # e.g. MightySat — live while measuring
    WINDOWED = "windowed"  # e.g. NBP / NT-100B — advertise then short dump
    ALWAYS = "always"      # e.g. Omron — scheduled / opportunistic history


@dataclass
class PairResult:
    ok: bool
    mac: str
    model: str
    detail: dict[str, Any] = field(default_factory=dict)


@dataclass
class SessionResult:
    ok: bool
    readings: list[Any] = field(default_factory=list)
    detail: dict[str, Any] = field(default_factory=dict)


class DevicePlugin(ABC):
    """One instance per brand. See brands/omron/plugin.py for the reference implementation."""

    brand_id: str
    device_class: DeviceClass
    priority_rank: int = 50

    @abstractmethod
    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        """One-time manual bonding/unlock. Called from the web UI pairing flow only."""
        ...

    @abstractmethod
    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        """Connect, read, and return parsed clinical readings for an already-paired device."""
        ...

    async def teardown(self, mac: str) -> None:
        """Optional brand-specific disconnect/power-off step. Default: no-op."""
        return None

    def matches_advertisement(self, name: str = "", mfg_ids: Optional[list[int]] = None) -> bool:
        """Optional: opportunistic advertisement match for hub discovery. Default: False (MAC-strict only)."""
        return False
```

### Step 0.3 — Create `medical_ble_toolkit/core/registry.py`

```python
"""
Plugin registry — brand_id -> DevicePlugin instance.

Registration is explicit: each brands/<brand>/plugin.py module registers
itself when imported, and brands/__init__.py imports every active brand
module once. brands/__init__.py is the ONLY file that changes when a new
brand is added.
"""
from __future__ import annotations

from typing import Dict, List

from .device_plugin import DevicePlugin

_PLUGINS: Dict[str, DevicePlugin] = {}


def register(plugin: DevicePlugin) -> None:
    if plugin.brand_id in _PLUGINS:
        raise ValueError(f"Plugin already registered for brand_id={plugin.brand_id!r}")
    _PLUGINS[plugin.brand_id] = plugin


def get_plugin(brand_id: str) -> DevicePlugin:
    try:
        return _PLUGINS[brand_id]
    except KeyError:
        raise KeyError(
            f"No plugin registered for brand_id={brand_id!r}. "
            f"Known: {sorted(_PLUGINS)}. Is brands/__init__.py importing it?"
        ) from None


def all_plugins() -> List[DevicePlugin]:
    return list(_PLUGINS.values())


def has_plugin(brand_id: str) -> bool:
    return brand_id in _PLUGINS
```

### Step 0.4 — Create `medical_ble_toolkit/core/pairing.py`

```python
"""
Generic pairing workflow — will orchestrate the parts of "pair a device" that
are identical across every brand (persist to DB, handle repair flag), leaving
the brand-specific handshake to DevicePlugin.pair().

NOTE: only Omron is migrated as of Phase 1. This module is intentionally thin
until Nipro/Beurer/FORA are migrated and ble_jobs.py's job_pair() is updated
to call it for every brand instead of just Omron. Do not expand this file's
scope in Phase 1 — it exists now so the import path is stable for later phases.
"""
from __future__ import annotations

from .device_plugin import PairResult
from .registry import get_plugin


async def pair_device(brand_id: str, mac: str, model: str, *, repair: bool = False) -> PairResult:
    plugin = get_plugin(brand_id)
    return await plugin.pair(mac, model, force_rebind=repair)
```

### Step 0.5 — Verify

```bash
python3 -c "from medical_ble_toolkit.core import device_plugin, registry, pairing; print('Phase 0 imports OK')"
```
Expected output: `Phase 0 imports OK`

### Step 0.6 — Commit

```bash
git add medical_ble_toolkit/core
git commit -m "Phase 0: add core plugin architecture (device_plugin, registry, pairing) — net-new, no existing files touched"
```

---

## Phase 1a — Relocate `omron_bp/` → `brands/omron/` (pure move, zero logic changes)

### Step 1a.1 — Baseline test run (BEFORE any change, for later comparison)

```bash
mkdir -p /tmp/migration_verify
python3 -m pytest medical_ble_toolkit/tests/ medical_ble_toolkit/omron_bp/tests/ -v > /tmp/migration_verify/pytest_before.txt 2>&1
tail -20 /tmp/migration_verify/pytest_before.txt
```
Record the pass/fail count. This is the baseline Step 1a.7 must match.

### Step 1a.2 — Checksum snapshot (BEFORE move)

```bash
find medical_ble_toolkit/omron_bp -name "*.py" -exec sha256sum {} \; \
  | sed 's#medical_ble_toolkit/omron_bp/##' | sort > /tmp/migration_verify/omron_pre_move.sha256
wc -l /tmp/migration_verify/omron_pre_move.sha256
```

### Step 1a.3 — The move

```bash
mkdir -p medical_ble_toolkit/brands
touch medical_ble_toolkit/brands/__init__.py   # placeholder, real content in Step 1c
git mv medical_ble_toolkit/omron_bp medical_ble_toolkit/brands/omron
```

### Step 1a.4 — Checksum verification (AFTER move, BEFORE any import fix)

```bash
find medical_ble_toolkit/brands/omron -name "*.py" -exec sha256sum {} \; \
  | sed 's#medical_ble_toolkit/brands/omron/##' | sort > /tmp/migration_verify/omron_post_move.sha256
diff /tmp/migration_verify/omron_pre_move.sha256 /tmp/migration_verify/omron_post_move.sha256
```
**Expected: no output.** If this shows any diff, STOP — it means content changed during
the move, which should be impossible with `git mv`. Report immediately.

### Step 1a.5 — Fix internal import paths (inside the moved folder only)

```bash
grep -rl "medical_ble_toolkit\.omron_bp" medical_ble_toolkit/brands/omron --include="*.py" \
  | xargs sed -i 's/medical_ble_toolkit\.omron_bp/medical_ble_toolkit.brands.omron/g'
```

### Step 1a.6 — Fix the 3 external files with real (non-comment) imports

**File 1: `medical_ble_toolkit/omron_bridge.py`** — has 4 real import occurrences (inside
`_require_omron_bp()`, `unpair_omron()`, `pair_omron()`). Blanket replace is safe here —
every occurrence in this file is a real import, none are comments:
```bash
sed -i 's/medical_ble_toolkit\.omron_bp/medical_ble_toolkit.brands.omron/g' medical_ble_toolkit/omron_bridge.py
```

**File 2: `medical_ble_toolkit/parsers/omron.py`** — same blanket treatment is safe; this
file's other mentions of "omron_bp" are in comments/docstrings and updating them too is
harmless (keeps docs accurate):
```bash
sed -i 's/medical_ble_toolkit\.omron_bp/medical_ble_toolkit.brands.omron/g' medical_ble_toolkit/parsers/omron.py
```

**File 3: `medical_ble_toolkit/ble_client.py`** — CAUTION: this file has 5 mentions of
"omron_bp" but only ONE is a real import (the rest are comments/help text in an argparse
CLI section — leave those alone, do not touch). Make this exact single-line replacement:

Find this exact line:
```python
                from .omron_bp.ble.connection import pair_client as _omron_pair
```
Replace with exactly:
```python
                from .brands.omron.ble.connection import pair_client as _omron_pair
```
Do not modify any other line in this file (specifically leave the comment/help-text
mentions of "omron_bp" near the CLI argparse section untouched — they're cosmetic).

### Step 1a.7 — Repo-wide verification: no real import references to the old path remain

```bash
grep -rn "medical_ble_toolkit\.omron_bp\|from \.omron_bp\|from omron_bp" --include="*.py" . | grep -v "__pycache__"
```
Expected: **zero results**, OR only results that are clearly inside comments/docstrings
(e.g. containing words like "Reuses", "bundled", "Backed by", "catalog"). If you see any
result that looks like an actual `import`/`from ... import` statement, stop and report —
it means a real import was missed.

### Step 1a.8 — Run test suite again, compare to baseline

```bash
python3 -m pytest medical_ble_toolkit/tests/ medical_ble_toolkit/brands/omron/tests/ -v > /tmp/migration_verify/pytest_after.txt 2>&1
tail -20 /tmp/migration_verify/pytest_after.txt
diff <(grep -E "^(PASSED|FAILED|ERROR)" /tmp/migration_verify/pytest_before.txt) <(grep -E "^(PASSED|FAILED|ERROR)" /tmp/migration_verify/pytest_after.txt)
```
Expected: identical pass/fail results to Step 1a.1 (only the file paths in the pytest
output should differ, not the outcomes).

### Step 1a.9 — Smoke-test the full import chain end-to-end

```bash
python3 -c "
from medical_ble_toolkit import omron_bridge
from medical_ble_toolkit.parsers import omron as omron_parser
from medical_ble_toolkit import ble_client
from medical_ble_toolkit.brands.omron.models.registry import list_models
print('Omron models loaded:', len(list_models()))
print('ALL IMPORTS OK')
"
```
Expected: prints a model count > 0, then `ALL IMPORTS OK`.

### Step 1a.10 — Commit

```bash
git add -A
git commit -m "Phase 1a: relocate omron_bp -> brands/omron (git mv, zero logic changes, import paths updated, verified byte-identical)"
```

---

## Phase 1b — Split Omron entries out of `profiles.py` (aggregator pattern, API-preserving)

`get_profile()` / `list_profiles()` in `medical_ble_toolkit/profiles.py` are called from 8
places across the codebase. This split must not change their behavior at all.

### Step 1b.1 — Snapshot the CURRENT merged PROFILES dict (before any edit)

```bash
python3 -c "
from medical_ble_toolkit.profiles import PROFILES
import json, dataclasses
snap = {k: dataclasses.asdict(v) for k, v in PROFILES.items()}
json.dump(snap, open('/tmp/migration_verify/profiles_pre_split.json', 'w'), indent=2, sort_keys=True)
print(len(snap), 'profiles snapshotted')
"
```

### Step 1b.2 — Create `medical_ble_toolkit/brands/omron/profiles.py`

Move the `"omron"` and `"hem7143t1"` entries out of the `PROFILES` dict in
`medical_ble_toolkit/profiles.py` into this new file, unmodified field-for-field, under
the name `OMRON_PROFILES`. Copy the `DeviceProfile` import and dataclass usage exactly
as it appears in the source file — do not change any field value.

```python
"""Omron DeviceProfile entries — split out of the monolithic profiles.py catalog."""
from __future__ import annotations

from medical_ble_toolkit.profiles import DeviceProfile

OMRON_PROFILES: dict[str, DeviceProfile] = {
    "omron": DeviceProfile(
        id="omron",
        brand="omron",
        model="HEM-* (catalog)",
        parser_key="omron",
        name_hints=("Omron", "BLESmart", "HEM-"),
        company_ids=(0x020E,),  # Omron company ID
        service_uuid="0000fe4a-0000-1000-8000-00805f9b34fb",  # modern FE4A
        notify_uuids=("49123040-aee8-11e1-a74d-0002a5d5c51b",),
        write_uuid="db5b55e0-aee7-11e1-965e-0002a5d5c51b",
        notes=(
            "NOT SIG BLP history. Use: python -m medical_ble_toolkit omron pair|read "
            "-d HEM-7143T1 -a <MAC>. Backed by medical_ble_toolkit.omron_bp (23 models)."
        ),
    ),
    "hem7143t1": DeviceProfile(
        id="hem7143t1",
        brand="omron",
        model="HEM-7143T1",
        parser_key="hem-7143t1",
        name_hints=("HEM-7143", "BLESmart", "Omron"),
        company_ids=(0x020E,),
        service_uuid="0000fe4a-0000-1000-8000-00805f9b34fb",
        notify_uuids=("49123040-aee8-11e1-a74d-0002a5d5c51b",),
        write_uuid="db5b55e0-aee7-11e1-965e-0002a5d5c51b",
        notes="Lab cuff. Pair with flashing P; read with short-press BT transfer mode.",
    ),
}
```

Note: the `notes` text is copied verbatim, including its stale mention of `omron_bp` —
do NOT "fix" it to say `brands.omron`. Keeping every field byte-identical to the source
is what makes Step 1b.4's zero-diff verification meaningful. Cosmetic accuracy of that
comment string can be a follow-up, not part of this mechanical split.

### Step 1b.3 — Edit `medical_ble_toolkit/profiles.py`

1. Remove the `"omron"` and `"hem7143t1"` keys and their values from the inline
   `PROFILES` dict literal.
2. After the `PROFILES` dict literal (still before `list_profiles()`/`get_profile()`),
   add:
   ```python
   from .brands.omron.profiles import OMRON_PROFILES
   PROFILES.update(OMRON_PROFILES)
   ```
3. Do not change `list_profiles()`, `get_profile()`, or the `aliases` dict inside
   `get_profile()` — they operate on the merged `PROFILES` dict and need no changes.

### Step 1b.4 — Snapshot AFTER the split and diff

```bash
python3 -c "
from medical_ble_toolkit.profiles import PROFILES
import json, dataclasses
snap = {k: dataclasses.asdict(v) for k, v in PROFILES.items()}
json.dump(snap, open('/tmp/migration_verify/profiles_post_split.json', 'w'), indent=2, sort_keys=True)
print(len(snap), 'profiles snapshotted')
"
diff /tmp/migration_verify/profiles_pre_split.json /tmp/migration_verify/profiles_post_split.json
```
**Expected: no output and identical profile count to Step 1b.1.** If there's any diff,
STOP and report — it means a field was altered during the copy.

### Step 1b.5 — Re-run the callers' smoke test

```bash
python3 -c "
from medical_ble_toolkit.profiles import get_profile, list_profiles
p = get_profile('omron')
p2 = get_profile('hem-7143t1')  # exercises the alias path
print('omron ->', p.id, '| hem-7143t1 alias ->', p2.id)
print('total profiles:', len(list_profiles()))
"
```
Expected: prints `omron -> omron | hem-7143t1 alias -> hem7143t1` and the same total
count as before the split.

### Step 1b.6 — Commit

```bash
git add -A
git commit -m "Phase 1b: split Omron profiles into brands/omron/profiles.py (aggregator pattern, verified byte-identical PROFILES dict)"
```

---

## Phase 1c — Wire up the Omron plugin (net-new file + brands/__init__.py + ONE narrow cutover)

### Step 1c.1 — Create `medical_ble_toolkit/brands/omron/plugin.py`

```python
"""
Omron DevicePlugin — thin adapter over medical_ble_toolkit.omron_bridge.

No BLE/parsing logic lives here. This file only maps the existing,
field-proven omron_bridge functions (pair_omron, read_omron, unpair_omron)
onto the DevicePlugin interface so the orchestrator can call Omron generically.
"""
from __future__ import annotations

from typing import Any

from medical_ble_toolkit.core.device_plugin import (
    DeviceClass,
    DevicePlugin,
    PairResult,
    SessionResult,
)
from medical_ble_toolkit.core.registry import register
from medical_ble_toolkit.omron_bridge import (
    flatten_readings,
    pair_omron,
    read_omron,
)


class OmronPlugin(DevicePlugin):
    brand_id = "omron"
    device_class = DeviceClass.ALWAYS
    priority_rank = 10

    async def pair(self, mac: str, model: str, *, force_rebind: bool = False) -> PairResult:
        await pair_omron(mac, model, force_rebind=force_rebind)
        return PairResult(ok=True, mac=mac, model=model)

    async def run_session(self, mac: str, model: str, **kwargs: Any) -> SessionResult:
        find_timeout = kwargs.get("find_timeout", 15.0)
        session_retries = kwargs.get("session_retries", 2)
        all_users = await read_omron(
            mac, model, find_timeout=find_timeout, session_retries=session_retries
        )
        readings = flatten_readings(all_users)
        return SessionResult(ok=True, readings=readings)

    def matches_advertisement(self, name: str = "", mfg_ids=None) -> bool:
        name_l = (name or "").lower()
        if any(h in name_l for h in ("omron", "blesmart", "hem-")):
            return True
        if mfg_ids and 0x020E in mfg_ids:
            return True
        return False


_plugin = OmronPlugin()
register(_plugin)
```

**Note:** this deliberately does NOT replicate the 2-attempt retry loop / "fe4a" /
"parent service" exception matching that exists in `ble_jobs.py`'s `job_sync()` Omron
branch (lines ~570-639). That logic is field-tuned error handling and is explicitly
OUT OF SCOPE for this phase (see the note at the end of this document) — `run_session()`
here is a single-attempt call. Do not port that retry logic into this file without
separate, explicit instruction.

### Step 1c.2 — Replace the placeholder `medical_ble_toolkit/brands/__init__.py`

```python
"""
Active brand plugin registrations. Import a brand module here to activate it
— this is the ONE file touched when adding a new brand.
"""
from .omron import plugin as _omron_plugin  # noqa: F401

# Future:
# from .nipro import plugin as _nipro_plugin
# from .beurer import plugin as _beurer_plugin
# from .fora import plugin as _fora_plugin
```

### Step 1c.3 — Verify registration

```bash
python3 -c "
import medical_ble_toolkit.brands  # triggers registration
from medical_ble_toolkit.core.registry import get_plugin, all_plugins
p = get_plugin('omron')
print('registered brand_id:', p.brand_id, '| device_class:', p.device_class, '| total plugins:', len(all_plugins()))
"
```
Expected: `registered brand_id: omron | device_class: DeviceClass.ALWAYS | total plugins: 1`

### Step 1c.4 — The ONE narrow cutover: `medical_ble_web/ble_jobs.py`, inside `job_pair()`

This is the only behavior-changing edit in this phase. It is scoped to exactly one
self-contained if-branch (no retry logic, no timing constants — safe to swap).

Find this exact block inside `job_pair()`:
```python
            if brand.get("is_omron"):
                from medical_ble_toolkit.omron_bridge import pair_omron

                await pair_omron(mac_u, model, force_rebind=repair)
            else:
```

Replace with exactly:
```python
            if brand.get("is_omron"):
                from medical_ble_toolkit.core.registry import get_plugin

                await get_plugin("omron").pair(mac_u, model, force_rebind=repair)
            else:
```

Do not touch anything else in `ble_jobs.py` in this phase — specifically leave
`job_sync()`'s Omron branch (~lines 570-639) and `_listen_for_brand()`'s omron duration
line (~line 1408) untouched. See "Explicitly deferred" section below.

### Step 1c.5 — Commit

```bash
git add -A
git commit -m "Phase 1c: add OmronPlugin, register in brands/__init__.py, cut over job_pair() to use plugin registry (job_sync retry logic intentionally untouched)"
```

---

## Explicitly deferred (do NOT attempt in this pass — flagged for a future, separate spec)

1. **`job_sync()`'s Omron branch** (`medical_ble_web/ble_jobs.py`, lines ~570-639) —
   contains a 2-attempt retry loop with string-matching on exception messages
   (`"fe4a" in msg`, `"parent service" in msg`). This is field-tuned error-recovery
   logic. Porting it into `OmronPlugin.run_session()` requires care to preserve
   behavior exactly and needs its own reviewed spec — not a mechanical relocation.
2. **Duration constants** — `12.0 if brand_id == "masimo" else 8.0` (~line 506) and
   `_listen_for_brand()`'s `45.0` cap for Omron (~line 1408) are two *different*
   timing concepts tuned against real hardware behavior. Do not consolidate them into
   a single plugin attribute without separate confirmation — they may not mean the
   same thing.
3. **Nipro, Beurer, FORA migrations** — separate specs, to follow the same Phase
   1a/1b/1c pattern once Omron is confirmed working end-to-end (including live
   hardware test).
4. **Stale/unused file cleanup** (root-level duplicate JSON registries, `poc.db`
   committed to git, `mqtt_config.json` secrets) — deliberately deferred to a final
   cleanup phase after all brand migrations are verified, per team decision.

---

## After Phase 1c: live hardware verification (manual, by the team — not Antigravity)

1. Pair a real Omron cuff via the web UI (`POST /pair`).
2. Confirm the pairing succeeds and `next_steps` in the response looks normal.
3. Trigger a sync/read via the web UI.
4. Confirm readings appear in `poc.db` and match what pre-migration behavior would
   produce (same field values, same record count).
5. Only after this passes: report back for Phase 2 (Nipro) planning.

## Rollback

Since this all lands on `main` directly:
```bash
git reset --hard pre-migration-phase0-1
```
This only works cleanly if no one else has pushed commits on top in the meantime —
confirm with the team before running it.