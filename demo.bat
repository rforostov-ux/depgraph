@echo off
rem Демонстрация этапа 2: прямые зависимости пакетов NuGet и обработка ошибок
chcp 65001 >nul
set "RUN=%~dp0run.bat"
set "URL=https://api.nuget.org/v3/index.json"

echo === 1. Конкретная версия ===
call "%RUN%" --package Serilog.Sinks.Console --repo %URL% --version 6.0.0
echo === 2. Пакет с несколькими зависимостями ===
call "%RUN%" --package Microsoft.Extensions.Logging --repo %URL% --version 8.0.0
echo === 3. Последняя версия (latest) ===
call "%RUN%" --package Newtonsoft.Json --repo %URL%
echo === 4. Тестовый репозиторий ===
call "%RUN%" --package A --repo "%~dp0tests\data\test_repo.txt" --repo-mode test
echo === 5. Ошибка: пакет не найден ===
call "%RUN%" --package No.Such.Package.Xyz --repo %URL%
echo === 6. Ошибка: нет такой версии ===
call "%RUN%" --package Newtonsoft.Json --repo %URL% --version 99.0.0
echo === 7. Ошибка: репозиторий недоступен ===
call "%RUN%" --package Newtonsoft.Json --repo http://localhost:1/v3/index.json
echo === 8. Ошибка: пакета нет в тестовом репозитории ===
call "%RUN%" --package Z --repo "%~dp0tests\data\test_repo.txt" --repo-mode test
