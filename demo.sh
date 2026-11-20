#!/bin/sh
# Демонстрация этапа 3 на тестовом репозитории без подключения к интернету
ROOT=$(cd "$(dirname "$0")" && pwd)
RUN="$ROOT/run.sh"
REPO="$ROOT/tests/data/test_repo.txt"

echo "=== 1. Транзитивные зависимости и цикл E -> A ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --max-depth 5
echo "=== 2. Ограничение глубины: только прямые зависимости ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --max-depth 1
echo "=== 3. Фильтр: исключаем пакет C ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --filter C
echo "=== 4. Пакет без зависимостей ==="
"$RUN" --package D --repo "$REPO" --repo-mode test
echo "=== 5. Фильтр исключает исходный пакет ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --filter A
echo "=== 6. Ошибка: пакет отсутствует ==="
"$RUN" --package Z --repo "$REPO" --repo-mode test
exit 0
