#!/usr/bin/env bash
# Compatibility wrapper for installed systemd UI unit
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/scripts/deploy/hub_open_ui.sh" "$@"
