# depgraph — визуализатор графа зависимостей пакетов NuGet

Практическая работа №2 по дисциплине «Конфигурационное управление», вариант 26.

## 1. Общее описание

`depgraph` — CLI-инструмент для построения и визуализации графа зависимостей пакетов
менеджера NuGet (.NET). Готовые менеджеры пакетов и библиотеки для получения
зависимостей не используются. Язык реализации — Java 17+, без внешних библиотек.

Текущее состояние — **этап 1: минимальный прототип с конфигурацией**.
Приложение принимает параметры из опций командной строки, проверяет их
и выводит в формате `ключ=значение`. При ошибке в параметрах выводится
сообщение и приложение завершается с кодом `1`.

## 2. Функции и настройки

| Опция | Ключ в выводе | Обязательна | По умолчанию | Описание и проверки |
|---|---|---|---|---|
| `--package <имя>` | `package` | да | — | Имя анализируемого пакета. В режиме `remote` — идентификатор NuGet: латинские буквы, цифры, `_`, а `.` и `-` только между ними. В режиме `test` — только заглавные латинские буквы (`A`, `B`, `AB`) |
| `--repo <url\|путь>` | `repository` | да | — | В режиме `remote` — URL, начинающийся с `http://` или `https://`. В режиме `test` — путь к существующему читаемому файлу описания графа |
| `--repo-mode <режим>` | `repo_mode` | нет | `remote` | Режим работы с репозиторием: `remote` или `test` |
| `--version <версия>` | `version` | нет | `latest` | `latest` или номер из цифр и точек, например `13.0.3` |
| `--ascii-tree` | `ascii_tree` | нет | `false` | Флаг (без значения): выводить зависимости в виде ASCII-дерева |
| `--max-depth <число>` | `max_depth` | нет | `5` | Целое число от 1 до 1000 |
| `--filter <подстрока>` | `filter` | нет | пусто | Пакеты, имя которых содержит подстроку, исключаются из анализа |
| `--help` | — | нет | — | Вывести справку |

Общие ошибки: неизвестная опция, опция без значения, отсутствие обязательной опции.

Структура кода:

- `src/depgraph/Main.java` — точка входа, справка, вывод ошибки;
- `src/depgraph/Config.java` — поля параметров, разбор опций (`switch`), проверки, вывод `ключ=значение`;
- `tests/depgraph/ConfigTest.java` — тесты;
- `tests/data/test_repo.txt` — пример файла тестового репозитория.

## 3. Сборка и запуск тестов

Требуется JDK 17 или новее (`javac` и `java` в `PATH`). Скрипты сами компилируют
исходники в каталог `build/`.

Windows (cmd или PowerShell):

```bat
.\run.bat --package Newtonsoft.Json --repo https://api.nuget.org/v3/index.json
.\test.bat
.\demo.bat
```

Linux / macOS:

```sh
./run.sh --package Newtonsoft.Json --repo https://api.nuget.org/v3/index.json
./test.sh
./demo.sh
```

`demo` последовательно показывает корректные запуски и обработку ошибок по каждому параметру.

## 4. Примеры использования

Все параметры:

```
> run.bat --package Newtonsoft.Json --repo https://api.nuget.org/v3/index.json --version 13.0.3 --ascii-tree --max-depth 3 --filter Test
package=Newtonsoft.Json
repository=https://api.nuget.org/v3/index.json
repo_mode=remote
version=13.0.3
ascii_tree=true
max_depth=3
filter=Test
```

Тестовый репозиторий:

```
> run.bat --package A --repo tests/data/test_repo.txt --repo-mode test
package=A
repository=tests/data/test_repo.txt
repo_mode=test
version=latest
ascii_tree=false
max_depth=5
filter=
```

Ошибки:

```
> run.bat --package Serilog --repo https://api.nuget.org/v3/index.json --max-depth 0
Ошибка: глубина должна быть от 1 до 1000, а не 0

> run.bat --package A --repo no_such_file.txt --repo-mode test
Ошибка: файл тестового репозитория не найден: no_such_file.txt

> run.bat --package Serilog --repo api.nuget.org/v3/index.json
Ошибка: URL должен начинаться с http:// или https://: api.nuget.org/v3/index.json
```
