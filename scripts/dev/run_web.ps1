#Requires -Version 5.1
# Convenience launcher from experiments root → medical_ble_web\run_web.ps1
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
& (Join-Path $here "medical_ble_web\run_web.ps1") @args
