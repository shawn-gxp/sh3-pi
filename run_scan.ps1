# One-click BLE scan for Omron HEM-7143T1
# Usage: .\run_scan.ps1
#        .\run_scan.ps1 -Seconds 90

param(
    [double]$Seconds = 45
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

function Find-Python {
    $candidates = @(
        "python",
        "python3",
        "$env:LocalAppData\Programs\Python\Python312\python.exe",
        "$env:LocalAppData\Programs\Python\Python311\python.exe",
        "C:\Python312\python.exe"
    )
    foreach ($c in $candidates) {
        try {
            if ($c -like "*\*" -and -not (Test-Path $c)) { continue }
            $v = & $c --version 2>$null
            if ($LASTEXITCODE -eq 0 -or $v) { return $c }
        } catch {}
    }
    return $null
}

$py = Find-Python
if (-not $py) {
    Write-Host "Python not found. Install Python 3.12+ and re-run." -ForegroundColor Red
    Write-Host "  winget install Python.Python.3.12"
    exit 1
}

Write-Host "Using: $py" -ForegroundColor Cyan
Write-Host "Installing/updating bleak if needed..."
& $py -m pip install -r requirements.txt -q

Write-Host ""
Write-Host ">>> Put Omron in PAIRING mode now (hold BT button until 'P' flashes) <<<" -ForegroundColor Yellow
Write-Host "Starting scan in 3 seconds..."
Start-Sleep -Seconds 3

& $py ble_scan.py --seconds $Seconds
