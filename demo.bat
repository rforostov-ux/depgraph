@echo off
setlocal
chcp 65001 >nul
set "RUN=%~dp0run.bat"
set "ACYCLIC=%~dp0tests\data\acyclic_repo.txt"
set "CYCLIC=%~dp0tests\data\test_repo.txt"

echo === 1. Loading order and shared dependency D ===
call "%RUN%" --package A --repo "%ACYCLIC%" --repo-mode test --max-depth 5
if errorlevel 1 exit /b 1

echo === 2. Package without dependencies ===
call "%RUN%" --package D --repo "%ACYCLIC%" --repo-mode test
if errorlevel 1 exit /b 1

echo === 3. Depth limit ===
call "%RUN%" --package A --repo "%ACYCLIC%" --repo-mode test --max-depth 1
if errorlevel 1 exit /b 1

echo === 4. Filter removes the branch with a cycle ===
call "%RUN%" --package A --repo "%CYCLIC%" --repo-mode test --filter C
if errorlevel 1 exit /b 1

echo === 5. Cycle: an error message is expected ===
call "%RUN%" --package A --repo "%CYCLIC%" --repo-mode test --max-depth 5
if not errorlevel 1 (
    echo ERROR: the cycle was not detected.
    exit /b 1
)
exit /b 0
