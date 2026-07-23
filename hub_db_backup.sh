#!/usr/bin/env bash
# Compatibility wrapper for installed systemd backup unit
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/scripts/deploy/hub_db_backup.sh" "$@"
