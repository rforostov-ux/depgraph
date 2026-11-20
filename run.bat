@echo off
setlocal
chcp 65001 >nul
set "ROOT=%~dp0"
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
if not exist "%ROOT%build\classes" mkdir "%ROOT%build\classes"
"%JAVAC%" -encoding UTF-8 -sourcepath "%ROOT%src" -d "%ROOT%build\classes" "%ROOT%src\depgraph\Main.java"
if errorlevel 1 exit /b 1
"%JAVA%" -cp "%ROOT%build\classes" depgraph.Main %*
exit /b %errorlevel%
