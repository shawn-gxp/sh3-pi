"""
Temporary BlueZ Agent1 for Just-Works pairing on Linux.

Without an agent (desktop bluetooth-applet / blueman / bluetoothctl agent),
BlueZ returns AuthenticationFailed / AuthenticationCanceled on Device.Pair.

Bleak's pair() does not register an agent — we do, for the duration of pair().
"""

from __future__ import annotations

import logging
import sys
from contextlib import asynccontextmanager
from typing import AsyncIterator, Optional

logger = logging.getLogger("omron_bp.ble.bluez_agent")

_IS_LINUX = sys.platform.startswith("linux")

AGENT_PATH = "/medical_ble/agent"
CAPABILITY = "NoInputNoOutput"  # Just Works (Omron, most medical meters)


@asynccontextmanager
async def bluez_pair_agent() -> AsyncIterator[bool]:
    """
    Register a default NoInputNoOutput agent while the context is open.

    Yields True if the agent was registered, False if skipped/unavailable
    (non-Linux or D-Bus failure — caller may still try bleak.pair()).
    """
    if not _IS_LINUX:
        yield False
        return

    bus = None
    registered = False
    try:
        from dbus_fast import BusType
        from dbus_fast.aio import MessageBus
        from dbus_fast.service import ServiceInterface, method

        class _JustWorksAgent(ServiceInterface):
            """org.bluez.Agent1 — auto-accept everything (Just Works)."""

            def __init__(self) -> None:
                super().__init__("org.bluez.Agent1")

            @method()
            def Release(self) -> "":  # noqa: N802
                logger.debug("BlueZ agent Release")

            @method()
            def RequestPinCode(self, device: "o") -> "s":  # noqa: N802
                logger.info("BlueZ agent RequestPinCode for %s → 0000", device)
                return "0000"

            @method()
            def DisplayPinCode(self, device: "o", pincode: "s") -> "":  # noqa: N802
                logger.info("BlueZ agent DisplayPinCode %s %s", device, pincode)

            @method()
            def RequestPasskey(self, device: "o") -> "u":  # noqa: N802
                logger.info("BlueZ agent RequestPasskey for %s → 0", device)
                return 0

            @method()
            def DisplayPasskey(  # noqa: N802
                self, device: "o", passkey: "u", entered: "q"
            ) -> "":
                logger.info(
                    "BlueZ agent DisplayPasskey %s passkey=%s entered=%s",
                    device,
                    passkey,
                    entered,
                )

            @method()
            def RequestConfirmation(self, device: "o", passkey: "u") -> "":  # noqa: N802
                logger.info(
                    "BlueZ agent RequestConfirmation %s passkey=%s — auto-accept",
                    device,
                    passkey,
                )

            @method()
            def RequestAuthorization(self, device: "o") -> "":  # noqa: N802
                logger.info("BlueZ agent RequestAuthorization %s — auto-accept", device)

            @method()
            def AuthorizeService(self, device: "o", uuid: "s") -> "":  # noqa: N802
                logger.info(
                    "BlueZ agent AuthorizeService %s uuid=%s — auto-accept",
                    device,
                    uuid,
                )

            @method()
            def Cancel(self) -> "":  # noqa: N802
                logger.warning("BlueZ agent Cancel")

        bus = await MessageBus(bus_type=BusType.SYSTEM).connect()
        agent = _JustWorksAgent()
        bus.export(AGENT_PATH, agent)

        introspection = await bus.introspect("org.bluez", "/org/bluez")
        root = bus.get_proxy_object("org.bluez", "/org/bluez", introspection)
        manager = root.get_interface("org.bluez.AgentManager1")

        try:
            await manager.call_unregister_agent(AGENT_PATH)
        except Exception:
            pass

        await manager.call_register_agent(AGENT_PATH, CAPABILITY)
        try:
            await manager.call_request_default_agent(AGENT_PATH)
        except Exception as exc:
            logger.debug("RequestDefaultAgent: %s (continuing)", exc)

        registered = True
        logger.info(
            "BlueZ Just-Works agent registered at %s (%s)",
            AGENT_PATH,
            CAPABILITY,
        )
        yield True
    except Exception as exc:
        logger.warning(
            "Could not register BlueZ pair agent (%s: %s). "
            "If pair fails: run  bluetoothctl  →  agent NoInputNoOutput  →  default-agent",
            type(exc).__name__,
            exc,
        )
        yield False
    finally:
        if registered and bus is not None:
            try:
                introspection = await bus.introspect("org.bluez", "/org/bluez")
                root = bus.get_proxy_object("org.bluez", "/org/bluez", introspection)
                manager = root.get_interface("org.bluez.AgentManager1")
                await manager.call_unregister_agent(AGENT_PATH)
                logger.info("BlueZ pair agent unregistered")
            except Exception as exc:
                logger.debug("unregister agent: %s", exc)
        if bus is not None:
            try:
                bus.disconnect()
            except Exception:
                pass


async def ensure_bluez_trusted(address: str) -> None:
    """Set Trusted=true on the BlueZ device object if we can resolve its path."""
    if not _IS_LINUX:
        return
    mac = address.strip().upper()
    try:
        from dbus_fast import BusType, Variant
        from dbus_fast.aio import MessageBus
        from dbus_fast.constants import MessageType
        from dbus_fast.message import Message

        bus = await MessageBus(bus_type=BusType.SYSTEM).connect()
        try:
            reply = await bus.call(
                Message(
                    destination="org.bluez",
                    path="/",
                    interface="org.freedesktop.DBus.ObjectManager",
                    member="GetManagedObjects",
                )
            )
            if reply is None or reply.message_type != MessageType.METHOD_RETURN:
                return
            objects = reply.body[0] if reply.body else {}
            path: Optional[str] = None
            for obj_path, ifaces in objects.items():
                dev = ifaces.get("org.bluez.Device1") or {}
                addr = dev.get("Address")
                if hasattr(addr, "value"):
                    addr = addr.value
                if isinstance(addr, str) and addr.upper() == mac:
                    path = obj_path
                    break
            if not path:
                logger.debug("ensure_bluez_trusted: no object for %s yet", mac)
                return
            set_reply = await bus.call(
                Message(
                    destination="org.bluez",
                    path=path,
                    interface="org.freedesktop.DBus.Properties",
                    member="Set",
                    signature="ssv",
                    body=[
                        "org.bluez.Device1",
                        "Trusted",
                        Variant("b", True),
                    ],
                )
            )
            if set_reply and set_reply.message_type == MessageType.METHOD_RETURN:
                logger.info("BlueZ Trusted=true for %s (%s)", mac, path)
        finally:
            bus.disconnect()
    except Exception as exc:
        logger.debug("ensure_bluez_trusted failed: %s", exc)
