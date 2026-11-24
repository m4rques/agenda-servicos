@echo off
setlocal
title Testes da Agenda de Servicos
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0agenda.ps1" -Testar
set "codigo=%errorlevel%"
echo.
if "%codigo%"=="0" (
    echo Testes concluidos com sucesso.
) else (
    echo Os testes nao foram concluidos. Confira a mensagem acima.
)
pause
exit /b %codigo%
