#!/bin/sh
# Демонстрация этапа 4 на тестовых репозиториях без интернета
ROOT=$(cd "$(dirname "$0")" && pwd)
RUN="$ROOT/run.sh"
ACYCLIC="$ROOT/tests/data/acyclic_repo.txt"
CYCLIC="$ROOT/tests/data/test_repo.txt"

echo "=== 1. Порядок загрузки и общая зависимость D ==="
"$RUN" --package A --repo "$ACYCLIC" --repo-mode test --max-depth 5
echo "=== 2. Пакет без зависимостей ==="
"$RUN" --package D --repo "$ACYCLIC" --repo-mode test
echo "=== 3. Ограничение глубины ==="
"$RUN" --package A --repo "$ACYCLIC" --repo-mode test --max-depth 1
echo "=== 4. Фильтр исключает ветвь с циклом ==="
"$RUN" --package A --repo "$CYCLIC" --repo-mode test --filter C
echo "=== 5. Цикл: ожидается сообщение об ошибке ==="
"$RUN" --package A --repo "$CYCLIC" --repo-mode test --max-depth 5
exit 0
