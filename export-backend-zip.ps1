# export-backend-zip.ps1 - Membuat arsip ZIP siap upload ke PythonAnywhere atau VPS
$ErrorActionPreference = 'Stop'
$projectPath = $PSScriptRoot
$zipPath = Join-Path $projectPath "backend-cloud-ready.zip"
$tempDir = Join-Path $projectPath ".tools\export_temp"

if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
if (Test-Path $tempDir) { Remove-Item $tempDir -Recurse -Force }

New-Item -ItemType Directory -Path (Join-Path $tempDir "backend\data") -Force | Out-Null

Copy-Item (Join-Path $projectPath "backend\server.py") -Destination (Join-Path $tempDir "backend\")
Copy-Item (Join-Path $projectPath "backend\wsgi.py") -Destination (Join-Path $tempDir "backend\")
Copy-Item (Join-Path $projectPath "backend\requirements.txt") -Destination (Join-Path $tempDir "backend\")

# Copy database jika sudah ada data
$localDb = Join-Path $projectPath "backend\data\jadwalstudio.sqlite3"
if (Test-Path $localDb) {
    Copy-Item $localDb -Destination (Join-Path $tempDir "backend\data\")
}

Compress-Archive -Path (Join-Path $tempDir "*") -DestinationPath $zipPath -Force
Remove-Item $tempDir -Recurse -Force

Write-Host "====================================================================" -ForegroundColor Green
Write-Host " Arsip backend berhasil dibuat!" -ForegroundColor Yellow
Write-Host " Lokasi file: $zipPath" -ForegroundColor Cyan
Write-Host " File ini siap kamu upload ke PythonAnywhere / VPS!" -ForegroundColor White
Write-Host "====================================================================" -ForegroundColor Green
