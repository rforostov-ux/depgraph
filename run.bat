@echo off
rem Сборка и запуск depgraph. Использование: run.bat [опции]
chcp 65001 >nul
set "ROOT=%~dp0"
if not exist "%ROOT%build\classes" mkdir "%ROOT%build\classes"
javac -encoding UTF-8 -sourcepath "%ROOT%src" -d "%ROOT%build\classes" "%ROOT%src\depgraph\Main.java"
if errorlevel 1 exit /b 1
java -cp "%ROOT%build\classes" depgraph.Main %*
