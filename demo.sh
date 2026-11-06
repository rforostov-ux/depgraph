#!/bin/sh
# Демонстрация этапа 2: прямые зависимости пакетов NuGet и обработка ошибок
ROOT=$(cd "$(dirname "$0")" && pwd)
RUN="$ROOT/run.sh"
URL="https://api.nuget.org/v3/index.json"

echo "=== 1. Конкретная версия ==="
"$RUN" --package Serilog.Sinks.Console --repo "$URL" --version 6.0.0
echo "=== 2. Пакет с несколькими зависимостями ==="
"$RUN" --package Microsoft.Extensions.Logging --repo "$URL" --version 8.0.0
echo "=== 3. Последняя версия (latest) ==="
"$RUN" --package Newtonsoft.Json --repo "$URL"
echo "=== 4. Тестовый репозиторий ==="
"$RUN" --package A --repo "$ROOT/tests/data/test_repo.txt" --repo-mode test
echo "=== 5. Ошибка: пакет не найден ==="
"$RUN" --package No.Such.Package.Xyz --repo "$URL"
echo "=== 6. Ошибка: нет такой версии ==="
"$RUN" --package Newtonsoft.Json --repo "$URL" --version 99.0.0
echo "=== 7. Ошибка: репозиторий недоступен ==="
"$RUN" --package Newtonsoft.Json --repo http://localhost:1/v3/index.json
echo "=== 8. Ошибка: пакета нет в тестовом репозитории ==="
"$RUN" --package Z --repo "$ROOT/tests/data/test_repo.txt" --repo-mode test
exit 0
