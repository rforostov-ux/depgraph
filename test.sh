#!/bin/sh
# Сборка и запуск тестов depgraph
set -e
cd "$(dirname "$0")"
mkdir -p build/tests
javac -encoding UTF-8 -sourcepath src:tests -d build/tests tests/depgraph/ConfigTest.java
java -cp build/tests depgraph.ConfigTest
