#!/usr/bin/env bash
# Compatibility wrapper
exec "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/scripts/dev/run_toolkit.sh" "$@"
