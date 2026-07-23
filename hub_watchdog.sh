#!/usr/bin/env bash
# Compatibility wrapper for installed systemd watchdog unit
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/scripts/deploy/hub_watchdog.sh" "$@"
