#!/bin/sh
# Сборка и запуск depgraph. Использование: ./run.sh [опции]
set -e
ROOT=$(cd "$(dirname "$0")" && pwd)
mkdir -p "$ROOT/build/classes"
javac -encoding UTF-8 -sourcepath "$ROOT/src" -d "$ROOT/build/classes" "$ROOT/src/depgraph/Main.java"
java -cp "$ROOT/build/classes" depgraph.Main "$@"
