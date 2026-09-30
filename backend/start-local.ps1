param(
    [string]$HostAddress = '127.0.0.1',
    [int]$Port = 8765
)
$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
$bundledPython = Join-Path $projectPath '.tools\python\cpython-3.12.14-windows-x86_64-none\python.exe'
if (Test-Path -LiteralPath $bundledPython) {
    & $bundledPython (Join-Path $PSScriptRoot 'server.py') --host $HostAddress --port $Port
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    python (Join-Path $PSScriptRoot 'server.py') --host $HostAddress --port $Port
} else {
    throw 'Python 3.12 belum tersedia. Pasang Python atau jalankan uv python install 3.12.'
}
