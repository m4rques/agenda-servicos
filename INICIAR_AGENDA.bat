@echo off
setlocal
title Agenda de Servicos
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0agenda.ps1"
set "codigo=%errorlevel%"
echo.
if not "%codigo%"=="0" echo Nao foi possivel iniciar a agenda. Confira a mensagem acima.
pause
exit /b %codigo%
