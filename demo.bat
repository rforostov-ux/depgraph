@echo off
rem Демонстрация этапа 3 на тестовом репозитории без подключения к интернету
chcp 65001 >nul
set "RUN=%~dp0run.bat"
set "REPO=%~dp0tests\data\test_repo.txt"

echo === 1. Транзитивные зависимости и цикл E -^> A ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --max-depth 5
echo === 2. Ограничение глубины: только прямые зависимости ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --max-depth 1
echo === 3. Фильтр: исключаем пакет C ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --filter C
echo === 4. Пакет без зависимостей ===
call "%RUN%" --package D --repo "%REPO%" --repo-mode test
echo === 5. Фильтр исключает исходный пакет ===
call "%RUN%" --package A --repo "%REPO%" --repo-mode test --filter A
echo === 6. Ошибка: пакет отсутствует ===
call "%RUN%" --package Z --repo "%REPO%" --repo-mode test
exit /b 0
