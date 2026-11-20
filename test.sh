#!/bin/sh
# Сборка и запуск тестов depgraph
set -e
cd "$(dirname "$0")"
mkdir -p build/tests
javac -encoding UTF-8 -sourcepath src:tests -d build/tests \
    tests/depgraph/ConfigTest.java tests/depgraph/RepositoryTest.java tests/depgraph/GraphTest.java \
    tests/depgraph/DependencyOrderTest.java
java -cp build/tests depgraph.ConfigTest
java -cp build/tests depgraph.RepositoryTest
java -cp build/tests depgraph.GraphTest
java -cp build/tests depgraph.DependencyOrderTest
