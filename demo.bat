@echo off
rem Демонстрация этапа 1: корректные запуски и обработка ошибок для всех параметров
chcp 65001 >nul
set "RUN=%~dp0run.bat"
set "URL=https://api.nuget.org/v3/index.json"

echo === 1. Все параметры заданы ===
call "%RUN%" --package Newtonsoft.Json --repo %URL% --version 13.0.3 --ascii-tree --max-depth 3 --filter Test
echo === 2. Тестовый режим ===
call "%RUN%" --package A --repo "%~dp0tests\data\test_repo.txt" --repo-mode test
echo === 3. Ошибка: неизвестная опция ===
call "%RUN%" --package Serilog --repo %URL% --colour red
echo === 4. Ошибка: нет обязательного --package ===
call "%RUN%" --repo %URL%
echo === 5. Ошибка: недопустимое имя пакета ===
call "%RUN%" --package "my package!" --repo %URL%
echo === 6. Ошибка: URL без http/https ===
call "%RUN%" --package Serilog --repo api.nuget.org/v3/index.json
echo === 7. Ошибка: файл тестового репозитория не найден ===
call "%RUN%" --package A --repo no_such_file.txt --repo-mode test
echo === 8. Ошибка: неизвестный режим репозитория ===
call "%RUN%" --package A --repo %URL% --repo-mode local
echo === 9. Ошибка: неверная версия ===
call "%RUN%" --package Serilog --repo %URL% --version v1.x
echo === 10. Ошибка: глубина не число / вне диапазона ===
call "%RUN%" --package Serilog --repo %URL% --max-depth abc
call "%RUN%" --package Serilog --repo %URL% --max-depth 0
echo === 11. Ошибка: у опции нет значения ===
call "%RUN%" --package --repo %URL%
