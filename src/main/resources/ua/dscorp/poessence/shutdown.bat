@echo off
setlocal

set "ProcessName=PoEssence"

for /f "tokens=2 delims=," %%i in ('tasklist /FI "IMAGENAME eq %ProcessName%.exe" /FO CSV /NH') do (
    taskkill /PID %%i /F
)

endlocal