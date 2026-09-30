# start-online.ps1 - Menjalankan Backend JadwalStudio + Cloudflare HTTPS Tunnel
$ErrorActionPreference = 'Stop'

$projectPath = $PSScriptRoot
$bundledPython = Join-Path $projectPath '.tools\python\cpython-3.12.14-windows-x86_64-none\python.exe'
$cloudflared = Join-Path $projectPath '.tools\cloudflared.exe'
$serverScript = Join-Path $projectPath 'backend\server.py'
$tunnelLog = Join-Path $projectPath '.tools\tunnel.log'

if (-not (Test-Path $cloudflared)) {
    Write-Host "[!] cloudflared.exe tidak ditemukan di .tools, sedang mendownload..." -ForegroundColor Yellow
    $cfUrl = "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
    Invoke-WebRequest -Uri $cfUrl -OutFile $cloudflared
}

$pythonExe = if (Test-Path -LiteralPath $bundledPython) { $bundledPython } elseif (Get-Command python -ErrorAction SilentlyContinue) { "python" } else { throw "Python tidak ditemukan!" }

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "  MEMULAI SERVER LOKAL & CLOUDFLARE TUNNEL..." -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan

# 1. Jalankan Backend Server
Write-Host "[1/2] Menjalankan Backend Python di 127.0.0.1:8765..." -ForegroundColor Green
$serverProc = Start-Process -FilePath $pythonExe -ArgumentList "`"$serverScript`" --host 127.0.0.1 --port 8765" -PassThru -NoNewWindow

if (Test-Path $tunnelLog) { Remove-Item $tunnelLog -Force }

# 2. Jalankan Cloudflare Tunnel
Write-Host "[2/2] Membuka Cloudflare HTTPS Tunnel ke internet..." -ForegroundColor Green
$tunnelProc = Start-Process -FilePath $cloudflared -ArgumentList "tunnel --url http://127.0.0.1:8765" -RedirectStandardError $tunnelLog -PassThru -NoNewWindow

# 3. Tunggu hingga URL publik terbentuk
$foundUrl = $null
$maxRetries = 25
$retry = 0

Write-Host "Menghubungkan ke Cloudflare Edge... " -NoNewline
while ($retry -lt $maxRetries -and -not $foundUrl) {
    Start-Sleep -Seconds 1
    Write-Host "." -NoNewline
    if (Test-Path $tunnelLog) {
        $lines = Get-Content $tunnelLog -ErrorAction SilentlyContinue
        foreach ($line in $lines) {
            if ($line -match '(https://[a-zA-Z0-9\-]+\.trycloudflare\.com)') {
                $foundUrl = $matches[1]
                break
            }
        }
    }
    $retry++
}
Write-Host ""

if ($foundUrl) {
    # Simpan URL terakhir ke file agar mudah dibaca script lain jika perlu
    Set-Content -Path (Join-Path $projectPath '.tools\last_tunnel_url.txt') -Value $foundUrl

    Write-Host "`n====================================================================" -ForegroundColor Green
    Write-Host "   BACKEND JADWALSTUDIO BERHASIL ONLINE! (HTTPS)" -ForegroundColor Yellow
    Write-Host "====================================================================" -ForegroundColor Green
    Write-Host "`nURL Publik Server Anda:" -ForegroundColor White
    Write-Host ">>>  $foundUrl  <<<`n" -ForegroundColor Cyan
    Write-Host "CARA PAKAI DI HP (TANPA KABEL USB):" -ForegroundColor Yellow
    Write-Host "1. Pastikan HP terhubung ke internet (Paket Data 4G/5G atau Wi-Fi bebas)."
    Write-Host "2. Buka aplikasi JadwalStudio di HP."
    Write-Host "3. Pada layar Login, klik tombol: 'Koneksi server lokal'"
    Write-Host "4. Masukkan URL di atas persis seperti ini:"
    Write-Host "   $foundUrl" -ForegroundColor Cyan
    Write-Host "5. Tekan 'Simpan', lalu login atau daftar akun baru!"
    Write-Host "`n(Biarkan jendela ini tetap terbuka selama aplikasi digunakan)." -ForegroundColor Gray
    Write-Host "Tekan Ctrl + C untuk mematikan server & tunnel.`n" -ForegroundColor Gray
    Write-Host "====================================================================" -ForegroundColor Green

    try {
        # Loop menjaga proses tetap hidup sampai user tekan Ctrl+C
        while ($true) {
            if ($serverProc.HasExited -or $tunnelProc.HasExited) {
                break
            }
            Start-Sleep -Seconds 1
        }
    } finally {
        Write-Host "`nMematikan server dan tunnel..." -ForegroundColor Yellow
        if (-not $serverProc.HasExited) { Stop-Process -Id $serverProc.Id -Force -ErrorAction SilentlyContinue }
        if (-not $tunnelProc.HasExited) { Stop-Process -Id $tunnelProc.Id -Force -ErrorAction SilentlyContinue }
        Write-Host "Selesai." -ForegroundColor Green
    }
} else {
    Write-Host "`n[!] Gagal mendeteksi URL tunnel otomatis. Periksa isi log di: $tunnelLog" -ForegroundColor Red
    if (-not $serverProc.HasExited) { Stop-Process -Id $serverProc.Id -Force -ErrorAction SilentlyContinue }
    if (-not $tunnelProc.HasExited) { Stop-Process -Id $tunnelProc.Id -Force -ErrorAction SilentlyContinue }
}
