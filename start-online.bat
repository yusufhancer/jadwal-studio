@echo off
title JadwalStudio Online Server
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "%~dp0start-online.ps1"
pause
