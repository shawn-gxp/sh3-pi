import asyncio
import sys
import logging
from bleak import BleakClient, BleakScanner
from bleak.exc import BleakError

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger("beurer_test")

# The standard Blood Pressure Measurement characteristic
BP_MEASUREMENT_UUID = "00002a35-0000-1000-8000-00805f9b34fb"

# CHANGE THIS TO YOUR BM54 MAC ADDRESS
TARGET_MAC = "0C:7F:ED:72:BC:40"  

def is_auth_error(exc: Exception) -> bool:
    msg = str(exc).lower()
    return any(
        s in msg
        for s in (
            "insufficient authentication",
            "att error: 0x05",
            "0x0305",
            "not permitted",
            "authentication required",
            "auth",
            "access denied",
        )
    )

def on_bp_data(sender, data):
    log.info(f"==> RECEIVED DATA from {sender}: {data.hex()}")

async def run_test(mac: str):
    if mac == "XX:XX:XX:XX:XX:XX":
        log.error("Please edit the script to set TARGET_MAC to your cuff's MAC address.")
        return

    log.info(f"Target MAC: {mac}")
    log.info("NOTE: The BM54 only advertises BLE for ~25 seconds!")
    log.info("Make sure the cuff BLE is turned ON now (flashing symbol).")
    
    device = None
    for attempt in range(1, 4):
        log.info(f"[Attempt {attempt}/3] Scanning for BM54 ({mac})...")
        device = await BleakScanner.find_device_by_address(mac, timeout=6.0)
        if device:
            log.info(f"Found {device.name}!")
            break
        log.warning("Device not found on scan attempt. Re-enable BLE on cuff if it turned off...")
        await asyncio.sleep(1.0)
    
    if not device:
        log.error(f"Device {mac} not found after retries. Turn on cuff BLE and try again.")
        return

    log.info(f"Connecting to {mac} via WinRT address lookup...")
    client = BleakClient(mac, timeout=12.0)
    
    try:
        await client.connect()
        log.info(f"Connected! is_connected: {client.is_connected}")
        
        # 1. Attempt CCCD subscription (The GATT Probe)
        log.info("Attempting to subscribe to Blood Pressure Measurement (GATT Probe)...")
        try:
            await client.start_notify(BP_MEASUREMENT_UUID, on_bp_data)
            log.info("Successfully subscribed without authentication!")
        except Exception as exc:
            if is_auth_error(exc):
                log.warning(f"GATT Probe rejected with Auth Error: {exc}")
                log.info("==> THE CUFF IS NOW SHOWING THE 6-DIGIT PASSKEY ON ITS LCD! <==")
                log.info("Triggering OS Pairing...")
                log.info(">>> CHECK YOUR WINDOWS NOTIFICATIONS to enter the PIN! <<<")
                
                try:
                    if sys.platform == "win32":
                        await client.pair(protection_level=2)
                    else:
                        await client.pair()
                    log.info("OS Pairing completed!")
                except Exception as pair_exc:
                    log.error(f"Pairing call exception: {pair_exc}")

                # 3. Retry CCCD subscription
                log.info("Retrying CCCD subscription after pairing step...")
                try:
                    await client.start_notify(BP_MEASUREMENT_UUID, on_bp_data)
                    log.info("Successfully subscribed to Blood Pressure Measurement!")
                except Exception as retry_exc:
                    log.error(f"Failed to subscribe after pairing: {retry_exc}")
            else:
                log.error(f"Unexpected error during GATT probe: {exc}")

        log.info("Listening for 15 seconds for incoming blood pressure readings...")
        await asyncio.sleep(15.0)

    except Exception as exc:
        log.error(f"Connection failed: {exc}")
        log.info("Tip: If connection timed out, the 25s cuff BLE window may have expired right as we connected.")
    finally:
        if client.is_connected:
            try:
                await client.disconnect()
            except Exception:
                pass
            
    log.info("Test finished.")

if __name__ == "__main__":
    try:
        asyncio.run(run_test(TARGET_MAC))
    except KeyboardInterrupt:
        log.info("Aborted by user.")
