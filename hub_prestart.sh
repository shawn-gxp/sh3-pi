#!/usr/bin/env bash
# Compatibility wrapper for installed systemd ExecStartPre
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/scripts/deploy/hub_prestart.sh" "$@"
