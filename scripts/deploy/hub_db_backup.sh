#!/usr/bin/env bash
# Rolling SQLite database backup for medical BLE hub
set -euo pipefail

_here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$_here/../.." && pwd)"
DB_PATH="$ROOT/medical_ble_web/data/poc.db"
BAK1="$DB_PATH.bak1"
BAK2="$DB_PATH.bak2"

if [[ ! -f "$DB_PATH" ]]; then
  echo "No database found at $DB_PATH"
  exit 0
fi

# Rotate backups
if [[ -f "$BAK1" ]]; then
  mv "$BAK1" "$BAK2"
fi

# Online backup using SQLite native backup API (safe for WAL mode)
sqlite3 "$DB_PATH" ".backup '$BAK1'"

echo "Backup completed successfully to $BAK1"
