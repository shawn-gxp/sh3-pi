"""
Temporary BlueZ Agent1 for pairing on Linux.

Without an agent (desktop bluetooth-applet / blueman / bluetoothctl agent),
BlueZ returns AuthenticationFailed / AuthenticationCanceled on Device.Pair.

Bleak's pair() does not register an agent — we do, for the duration of pair().

Modes:
  - Just Works (NoInputNoOutput): Omron and most medical meters — auto-accept
  - Passkey (KeyboardDisplay): Beurer BM54 etc. — return 6-digit code from cuff LCD
    via PasskeyBroker (pre-set from UI or provided mid-pair through POST /pair/passkey)
"""

from __future__ import annotations

import logging
import sys
import threading
from contextlib import asynccontextmanager
from typing import AsyncIterator, Callable, Optional

logger = logging.getLogger("omron_bp.ble.bluez_agent")

_IS_LINUX = sys.platform.startswith("linux")

AGENT_PATH = "/medical_ble/agent"
CAPABILITY_JUST_WORKS = "NoInputNoOutput"
CAPABILITY_PASSKEY = "KeyboardDisplay"


class PasskeyBroker:
    """
    Thread-safe passkey handoff between the web UI and the sync BlueZ agent.

    Agent RequestPasskey() blocks here until provide() is called (or timeout).
    """

    def __init__(self) -> None:
        self._lock = threading.Lock()
        self._event = threading.Event()
        self._passkey: Optional[int] = None
        self._waiting = False
        self._cancelled = False
        self._device: str = ""
        self._on_need: Optional[Callable[[], None]] = None

    def reset(self, preset: Optional[int] = None) -> None:
        with self._lock:
            self._passkey = preset
            self._waiting = False
            self._cancelled = False
            self._device = ""
            self._event.clear()
            if preset is not None:
                self._event.set()

    def set_on_need(self, cb: Optional[Callable[[], None]]) -> None:
        self._on_need = cb

    def provide(self, passkey: int | str) -> None:
        digits = "".join(c for c in str(passkey) if c.isdigit())
        if not digits:
            raise ValueError("Passkey must contain digits")
        # BlueZ passkey is uint32 0..999999
        value = int(digits[-6:]) if len(digits) > 6 else int(digits)
        with self._lock:
            self._passkey = value
            self._event.set()
        logger.info("PasskeyBroker: passkey provided (%06d)", value)

    def cancel(self) -> None:
        with self._lock:
            self._cancelled = True
            self._event.set()

    @property
    def waiting(self) -> bool:
        with self._lock:
            return self._waiting and self._passkey is None and not self._cancelled

    @property
    def device_path(self) -> str:
        with self._lock:
            return self._device

    def status(self) -> dict:
        with self._lock:
            return {
                "waiting": self._waiting and self._passkey is None and not self._cancelled,
                "has_passkey": self._passkey is not None,
                "device": self._device,
                "cancelled": self._cancelled,
            }

    def wait(self, device: str = "", timeout: float = 90.0) -> Optional[int]:
        with self._lock:
            if self._passkey is not None:
                return self._passkey
            self._waiting = True
            self._device = device or self._device
        if self._on_need:
            try:
                self._on_need()
            except Exception:  # noqa: BLE001
                pass
        logger.info(
            "PasskeyBroker: waiting up to %.0fs for 6-digit passkey (device=%s)",
            timeout,
            device,
        )
        ok = self._event.wait(timeout=timeout)
        with self._lock:
            self._waiting = False
            if self._cancelled:
                return None
            if ok and self._passkey is not None:
                return self._passkey
            return None


# Process-wide broker so the UI can feed a passkey while pair() is in flight
GLOBAL_PASSKEY_BROKER = PasskeyBroker()


def _build_agent_class(active_broker: Optional[PasskeyBroker], wait_timeout_s: float):
    """Build Agent1 class (sync methods — safe when bus runs on its own thread)."""
    from dbus_fast.service import ServiceInterface, method

    class _PairAgent(ServiceInterface):
        def __init__(self) -> None:
            super().__init__("org.bluez.Agent1")

        @method()
        def Release(self) -> "":  # noqa: N802
            logger.debug("BlueZ agent Release")

        @method()
        def RequestPinCode(self, device: "o") -> "s":  # noqa: N802
            if active_broker is not None:
                pk = active_broker.wait(device=str(device), timeout=wait_timeout_s)
                if pk is not None:
                    logger.info("BlueZ agent RequestPinCode → %06d", pk)
                    return f"{pk:06d}"[-6:]
            logger.info("BlueZ agent RequestPinCode for %s → 0000", device)
            return "0000"

        @method()
        def DisplayPinCode(self, device: "o", pincode: "s") -> "":  # noqa: N802
            logger.info("BlueZ agent DisplayPinCode %s %s", device, pincode)

        @method()
        def RequestPasskey(self, device: "o") -> "u":  # noqa: N802
            if active_broker is not None:
                pk = active_broker.wait(device=str(device), timeout=wait_timeout_s)
                if pk is not None:
                    logger.info("BlueZ agent RequestPasskey → %06d", pk)
                    return int(pk) % 1000000
                logger.warning("BlueZ agent RequestPasskey timeout/cancel")
                return 0
            logger.info("BlueZ agent RequestPasskey for %s → 0 (Just Works)", device)
            return 0

        @method()
        def DisplayPasskey(  # noqa: N802
            self, device: "o", passkey: "u", entered: "q"
        ) -> "":
            logger.info(
                "BlueZ agent DisplayPasskey %s passkey=%06d entered=%s",
                device,
                int(passkey),
                entered,
            )
            if active_broker is not None:
                with active_broker._lock:  # noqa: SLF001
                    active_broker._device = str(device)
                    active_broker._waiting = True

        @method()
        def RequestConfirmation(self, device: "o", passkey: "u") -> "":  # noqa: N802
            logger.info(
                "BlueZ agent RequestConfirmation %s passkey=%06d — accept",
                device,
                int(passkey),
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
            if active_broker is not None:
                active_broker.cancel()

    return _PairAgent


@asynccontextmanager
async def bluez_pair_agent(
    passkey: Optional[int] = None,
    *,
    broker: Optional[PasskeyBroker] = None,
    wait_timeout_s: float = 90.0,
) -> AsyncIterator[bool]:
    """
    Register a BlueZ agent while the context is open.

    Passkey mode runs the D-Bus agent on a **dedicated thread + event loop**
    so RequestPasskey can block waiting for the UI without freezing FastAPI
    (mid-pair POST /pair/passkey still works).

    Just-Works mode uses the caller's asyncio loop (Omron path).
    """
    if not _IS_LINUX:
        yield False
        return

    use_passkey_mode = passkey is not None or broker is not None
    active_broker = broker
    if use_passkey_mode:
        active_broker = broker or GLOBAL_PASSKEY_BROKER
        # Preserve a passkey already seeded by hub UI / job_pair; only reset
        # when we have a new preset or the broker is idle empty.
        st = active_broker.status()
        if passkey is not None:
            active_broker.reset(preset=passkey)
        elif not st.get("has_passkey") and not st.get("waiting"):
            active_broker.reset(preset=None)
    capability = CAPABILITY_PASSKEY if use_passkey_mode else CAPABILITY_JUST_WORKS

    if use_passkey_mode:
        # ---- threaded agent (blocks on RequestPasskey without killing UI) ----
        ready = threading.Event()
        stop = threading.Event()
        result: dict = {"ok": False, "error": None}

        def _thread_main() -> None:
            import asyncio as _aio

            async def _run() -> None:
                bus = None
                registered = False
                try:
                    from dbus_fast import BusType
                    from dbus_fast.aio import MessageBus

                    bus = await MessageBus(bus_type=BusType.SYSTEM).connect()
                    AgentCls = _build_agent_class(active_broker, wait_timeout_s)
                    bus.export(AGENT_PATH, AgentCls())
                    introspection = await bus.introspect("org.bluez", "/org/bluez")
                    root = bus.get_proxy_object("org.bluez", "/org/bluez", introspection)
                    manager = root.get_interface("org.bluez.AgentManager1")
                    try:
                        await manager.call_unregister_agent(AGENT_PATH)
                    except Exception:
                        pass
                    await manager.call_register_agent(AGENT_PATH, capability)
                    try:
                        await manager.call_request_default_agent(AGENT_PATH)
                    except Exception as exc:
                        logger.debug("RequestDefaultAgent: %s", exc)
                    registered = True
                    result["ok"] = True
                    logger.info(
                        "BlueZ passkey agent registered at %s (%s) [thread]",
                        AGENT_PATH,
                        capability,
                    )
                    ready.set()
                    while not stop.is_set():
                        await _aio.sleep(0.2)
                except Exception as exc:
                    result["error"] = exc
                    logger.warning(
                        "Passkey agent thread failed: %s: %s",
                        type(exc).__name__,
                        exc,
                    )
                    ready.set()
                finally:
                    if registered and bus is not None:
                        try:
                            introspection = await bus.introspect("org.bluez", "/org/bluez")
                            root = bus.get_proxy_object(
                                "org.bluez", "/org/bluez", introspection
                            )
                            manager = root.get_interface("org.bluez.AgentManager1")
                            await manager.call_unregister_agent(AGENT_PATH)
                            logger.info("BlueZ passkey agent unregistered")
                        except Exception as exc:
                            logger.debug("unregister agent: %s", exc)
                    if bus is not None:
                        try:
                            bus.disconnect()
                        except Exception:
                            pass

            _aio.run(_run())

        t = threading.Thread(target=_thread_main, name="bluez-passkey-agent", daemon=True)
        t.start()
        # Wait until registered (or failed)
        ready.wait(timeout=8.0)
        try:
            if not result["ok"]:
                logger.warning(
                    "Passkey agent not ready: %s",
                    result.get("error") or "timeout",
                )
                yield False
            else:
                yield True
        finally:
            stop.set()
            if active_broker is not None:
                try:
                    active_broker.cancel()
                except Exception:
                    pass
            t.join(timeout=5.0)
        return

    # ---- Just Works on caller's asyncio loop ----
    bus = None
    registered = False
    try:
        from dbus_fast import BusType
        from dbus_fast.aio import MessageBus

        bus = await MessageBus(bus_type=BusType.SYSTEM).connect()
        AgentCls = _build_agent_class(None, wait_timeout_s)
        bus.export(AGENT_PATH, AgentCls())

        introspection = await bus.introspect("org.bluez", "/org/bluez")
        root = bus.get_proxy_object("org.bluez", "/org/bluez", introspection)
        manager = root.get_interface("org.bluez.AgentManager1")

        try:
            await manager.call_unregister_agent(AGENT_PATH)
        except Exception:
            pass

        await manager.call_register_agent(AGENT_PATH, capability)
        try:
            await manager.call_request_default_agent(AGENT_PATH)
        except Exception as exc:
            logger.debug("RequestDefaultAgent: %s (continuing)", exc)

        registered = True
        logger.info(
            "BlueZ Just-Works agent registered at %s (%s)",
            AGENT_PATH,
            capability,
        )
        yield True
    except Exception as exc:
        logger.warning(
            "Could not register BlueZ pair agent (%s: %s). "
            "If pair fails: run  bluetoothctl  →  agent on  →  default-agent",
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


async def ensure_adapter_pairable() -> None:
    """Turn controller Pairable=true (often left off on desktop images)."""
    if not _IS_LINUX:
        return
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
            for obj_path, ifaces in objects.items():
                if "org.bluez.Adapter1" not in ifaces:
                    continue
                await bus.call(
                    Message(
                        destination="org.bluez",
                        path=obj_path,
                        interface="org.freedesktop.DBus.Properties",
                        member="Set",
                        signature="ssv",
                        body=["org.bluez.Adapter1", "Pairable", Variant("b", True)],
                    )
                )
                logger.info("BlueZ Pairable=true on %s", obj_path)
        finally:
            bus.disconnect()
    except Exception as exc:
        logger.debug("ensure_adapter_pairable: %s", exc)
