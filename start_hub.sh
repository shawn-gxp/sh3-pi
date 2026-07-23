#!/usr/bin/env bash
# Compatibility wrapper (systemd + old docs still call repo-root scripts)
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/scripts/deploy/start_hub.sh" "$@"
