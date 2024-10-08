@echo off
setlocal

set "appName=PoEssence.exe"

:checkAppRunning
set "appRunning=false"

for /f "tokens=1,*" %%a in ('tasklist /fi "imagename eq %appName%" /fo csv ^| findstr /i "%appName%"') do (
    set "appRunning=true"
)

if "%appRunning%"=="true" (
    echo The application %appName% is still running.
    timeout /t 5 /nobreak >nul
    goto checkAppRunning
) else (
    echo The application %appName% is closed.
)

endlocal

move /y PoEssence.jar app/PoEssence-1.0.jar

echo Update completed.

start "" /b "PoEssence.exe" & exit