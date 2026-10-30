@echo off
rem Сборка и запуск тестов depgraph
chcp 65001 >nul
cd /d "%~dp0"
if not exist build\tests mkdir build\tests
javac -encoding UTF-8 -sourcepath "src;tests" -d build\tests tests\depgraph\ConfigTest.java
if errorlevel 1 exit /b 1
java -cp build\tests depgraph.ConfigTest
