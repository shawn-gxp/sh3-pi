#Requires -Version 5.1
<#
.SYNOPSIS
  Start the Medical BLE Web POC (local FastAPI UI).

.EXAMPLE
  .\run_web.ps1

.EXAMPLE
  .\run_web.ps1 -NoBrowser -Port 8741
#>
param(
    [int]$Port = 8741,
    [switch]$NoBrowser,
    [switch]$SkipInstall
)

$ErrorActionPreference = "Stop"
$WebRoot = $PSScriptRoot
$ExperimentsRoot = Split-Path -Parent $WebRoot

Set-Location -LiteralPath $WebRoot

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  Medical BLE Web POC" -ForegroundColor Cyan
Write-Host "  $WebRoot" -ForegroundColor DarkGray
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

function Find-Python {
    foreach ($name in @("py", "python", "python3")) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if (-not $cmd) { continue }
        if ($name -eq "py") {
            & py -3 -c "import sys; print(sys.executable)" 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { return @{ Exe = "py"; Args = @("-3") } }
        } else {
            & $name -c "import sys; print(sys.executable)" 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) { return @{ Exe = $name; Args = @() } }
        }
    }
    return $null
}

$py = Find-Python
if (-not $py) {
    Write-Host "ERROR: Python 3 not found (tried py -3, python, python3)." -ForegroundColor Red
    exit 1
}

function Invoke-Python {
    param([Parameter(Mandatory)][string[]]$Arguments)
    & $py.Exe @($py.Args + $Arguments)
    return $LASTEXITCODE
}

Write-Host -NoNewline "Python: "
Invoke-Python -Arguments @("-c", "import sys; print(sys.executable)") | Out-Host

if (-not $SkipInstall) {
    Write-Host "Checking dependencies…" -ForegroundColor DarkGray
    $code = Invoke-Python -Arguments @("-c", "import fastapi, uvicorn")
    if ($code -ne 0) {
        Write-Host "Installing medical_ble_web requirements…" -ForegroundColor Yellow
        $req = Join-Path $WebRoot "requirements.txt"
        $code = Invoke-Python -Arguments @("-m", "pip", "install", "-r", $req)
        if ($code -ne 0) {
            Write-Host "pip install failed for web requirements." -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "fastapi/uvicorn OK." -ForegroundColor DarkGray
    }

    $code = Invoke-Python -Arguments @("-c", "import bleak")
    if ($code -ne 0) {
        $parentReq = Join-Path $ExperimentsRoot "requirements.txt"
        if (Test-Path -LiteralPath $parentReq) {
            Write-Host "Installing parent requirements (bleak)…" -ForegroundColor Yellow
            Invoke-Python -Arguments @("-m", "pip", "install", "-r", $parentReq) | Out-Null
        } else {
            Write-Host "Installing bleak…" -ForegroundColor Yellow
            Invoke-Python -Arguments @("-m", "pip", "install", "bleak>=0.22.0") | Out-Null
        }
    }
}

# medical_ble_toolkit lives in parent experiments folder
$env:PYTHONPATH = if ($env:PYTHONPATH) {
    "$ExperimentsRoot;$WebRoot;$($env:PYTHONPATH)"
} else {
    "$ExperimentsRoot;$WebRoot"
}

$url = "http://127.0.0.1:$Port"
Write-Host ""
Write-Host "Starting server on $url" -ForegroundColor Green
Write-Host "  SQLite:   $(Join-Path $WebRoot 'data\poc.db')" -ForegroundColor DarkGray
Write-Host "  Toolkit:  $(Join-Path $ExperimentsRoot 'medical_ble_toolkit')" -ForegroundColor DarkGray
Write-Host "  Live:     WebSocket push + auto-reconnect" -ForegroundColor DarkGray
Write-Host "  Stop:     Ctrl+C" -ForegroundColor DarkGray
Write-Host ""

if (-not $NoBrowser) {
    try { Start-Process $url } catch { Write-Host "Could not open browser: $_" -ForegroundColor Yellow }
}

# app.py lives in this directory; cwd is WebRoot
$boot = @"
import uvicorn
uvicorn.run('app:app', host='127.0.0.1', port=$Port, reload=False, log_level='info')
"@
$code = Invoke-Python -Arguments @("-c", $boot)
exit $code
