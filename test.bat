@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
set "JAVAC=javac"
set "JAVA=java"
if defined JAVA_HOME (
    if not exist "%JAVA_HOME%\bin\javac.exe" (
        echo ERROR: javac.exe not found in JAVA_HOME.
        exit /b 1
    )
    if not exist "%JAVA_HOME%\bin\java.exe" (
        echo ERROR: java.exe not found in JAVA_HOME.
        exit /b 1
    )
    set "JAVAC=%JAVA_HOME%\bin\javac.exe"
    set "JAVA=%JAVA_HOME%\bin\java.exe"
)
if not exist build\tests mkdir build\tests
"%JAVAC%" -encoding UTF-8 -sourcepath "src;tests" -d build\tests ^
    tests\depgraph\ConfigTest.java tests\depgraph\RepositoryTest.java tests\depgraph\GraphTest.java ^
    tests\depgraph\DependencyOrderTest.java
if errorlevel 1 exit /b 1
"%JAVA%" -cp build\tests depgraph.ConfigTest
if errorlevel 1 exit /b 1
"%JAVA%" -cp build\tests depgraph.RepositoryTest
if errorlevel 1 exit /b 1
"%JAVA%" -cp build\tests depgraph.GraphTest
if errorlevel 1 exit /b 1
"%JAVA%" -cp build\tests depgraph.DependencyOrderTest
exit /b %errorlevel%
