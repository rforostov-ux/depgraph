@echo off
setlocal
chcp 65001 >nul
set "RUN=%~dp0run.bat"
set "REPO=%~dp0tests\data\test_repo.txt"

echo === 1. Package A: shared dependency and cycle ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --max-depth 5 --ascii-tree
if errorlevel 1 exit /b 1

echo === 2. Package B: direct dependency ===
call "%RUN%" --package B --repo "%REPO%" --repo-mode test --ascii-tree
if errorlevel 1 exit /b 1

echo === 3. Package D: no dependencies ===
call "%RUN%" --package D --repo "%REPO%" --repo-mode test --ascii-tree
if errorlevel 1 exit /b 1

echo === 4. Depth limit ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --max-depth 1 --ascii-tree
if errorlevel 1 exit /b 1

echo === 5. Filter excludes C ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --filter C --ascii-tree
exit /b %errorlevel%
