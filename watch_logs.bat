@echo off
set "LOG_FILE=%~dp0logs\application.log"

if not exist "%LOG_FILE%" (
    echo Log file not found:
    echo "%LOG_FILE%"
    echo.
    echo Start the application first, or check the file name.
    pause
    exit /b 1
)

start "Application Log" powershell.exe -NoExit -NoProfile -Command "Get-Content -LiteralPath $env:LOG_FILE -Tail 100 -Wait"