#!/bin/sh
# Примеры визуализации на тестовых пакетах без подключения к интернету
set -e
ROOT=$(cd "$(dirname "$0")" && pwd)
RUN="$ROOT/run.sh"
REPO="$ROOT/tests/data/test_repo.txt"

echo "=== 1. Пакет A: общие зависимости и цикл ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --max-depth 5 --ascii-tree
echo "=== 2. Пакет B: простая зависимость ==="
"$RUN" --package B --repo "$REPO" --repo-mode test --ascii-tree
echo "=== 3. Пакет D: без зависимостей ==="
"$RUN" --package D --repo "$REPO" --repo-mode test --ascii-tree
echo "=== 4. Ограничение глубины ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --max-depth 1 --ascii-tree
echo "=== 5. Фильтр исключает пакет C ==="
"$RUN" --package A --repo "$REPO" --repo-mode test --filter C --ascii-tree
