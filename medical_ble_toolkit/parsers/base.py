"""Parser protocol (interface) shared by all device parsers."""

from __future__ import annotations

from typing import Any, Optional, Protocol, runtime_checkable

from ..models import DeviceBrand


@runtime_checkable
class VitalParser(Protocol):
    """
    Pure parse contract — port as Kotlin interface:

        interface VitalParser<T> {
            fun parse(payload: ByteArray): T
            val brand: String
            val name: String
        }
    """

    brand: DeviceBrand
    name: str

    def parse(self, payload: bytes | bytearray) -> Any:
        ...

    def can_parse(self, payload: bytes | bytearray, characteristic_uuid: str = "") -> bool:
        """Heuristic: does this payload look like mine? Used by auto-dispatch RE mode."""
        ...


def parse_dispatch(
    payload: bytes | bytearray,
    characteristic_uuid: str = "",
    parsers: Optional[list] = None,
) -> Any:
    """
    Try each parser until one accepts the payload.
    Useful during protocol analysis when UUID ownership is still unknown.
    """
    if parsers is None:
        parsers = []

    errors = []
    for p in parsers:
        try:
            if p.can_parse(payload, characteristic_uuid):
                return p.parse(payload)
        except Exception as exc:  # noqa: BLE001 — forensic: collect all failures
            errors.append(f"{p.name}: {exc}")
    raise ValueError(
        "No parser accepted payload. Tried: "
        + "; ".join(errors) if errors else "No parser matched heuristics."
    )
