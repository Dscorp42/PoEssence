@echo off
setlocal

set "ProcessName=PoEssence"

for /f "tokens=2 delims=," %%i in ('tasklist /FI "IMAGENAME eq %ProcessName%.exe" /FO CSV /NH') do (
    taskkill /F /IM PoEssence.exe
)

endlocal

exit