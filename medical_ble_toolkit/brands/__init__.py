"""
Active brand plugin registrations. Import a brand module here to activate it
— this is the ONE file touched when adding a new brand.

Brand plugins must not import omron_bridge / heavy facades at module top-level;
that creates cycles with parsers → brands package init. Lazy-import inside
plugin methods instead.
"""
from .omron import plugin as _omron_plugin   # noqa: F401 — Phase 1
from .beurer import plugin as _beurer_plugin # noqa: F401 — Phase 2A
from .nipro import plugin as _nipro_plugin   # noqa: F401 — Phase 2B
from .fora import plugin as _fora_plugin     # noqa: F401 — Phase 2C
from .masimo import plugin as _masimo_plugin # noqa: F401 — Phase 4-1
from .and_ import plugin as _and_plugin      # noqa: F401 — Phase 4-2
