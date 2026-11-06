package depgraph;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Инструмент визуализации графа зависимостей пакетов NuGet. Этап 2: вывод прямых зависимостей.
 */
public class Main {

    /**
     * Точка входа: читает параметры и выводит прямые зависимости пакета или сообщение об ошибке.
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help")) {
            printHelp();
            return;
        }
        try {
            Config config = Config.parse(args);
            if (config.repoMode.equals("test")) {
                printTestDependencies(config);
            } else {
                printNugetDependencies(config);
            }
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printNugetDependencies(Config config) throws IOException {
        NugetRepository repository = new NugetRepository(config.repository);
        String version = repository.resolveVersion(config.packageName, config.version);
        List<Dependency> dependencies = repository.getDependencies(config.packageName, version);
        System.out.println("Прямые зависимости " + config.packageName + " " + version + ":");
        if (dependencies.isEmpty()) {
            System.out.println("  нет зависимостей");
        }
        String currentFramework = null;
        for (Dependency dependency : dependencies) {
            if (!dependency.framework.equals(currentFramework)) {
                currentFramework = dependency.framework;
                if (!currentFramework.isEmpty()) {
                    System.out.println("[" + currentFramework + "]");
                }
            }
            System.out.println("  " + dependency.name + " " + dependency.version);
        }
    }

    private static void printTestDependencies(Config config) throws IOException {
        Map<String, List<String>> graph = TestRepository.load(config.repository);
        List<String> dependencies = graph.get(config.packageName);
        if (dependencies == null) {
            throw new IOException("пакет " + config.packageName + " не найден в тестовом репозитории");
        }
        System.out.println("Прямые зависимости " + config.packageName + ":");
        if (dependencies.isEmpty()) {
            System.out.println("  нет зависимостей");
        }
        for (String dependency : dependencies) {
            System.out.println("  " + dependency);
        }
    }

    private static void printHelp() {
        System.out.println("Использование: depgraph --package <имя> --repo <url|путь> [опции]");
        System.out.println("  --package <имя>       имя пакета (обязательно)");
        System.out.println("  --repo <url|путь>     URL репозитория или путь к тестовому файлу (обязательно)");
        System.out.println("  --repo-mode <режим>   remote (по умолчанию) или test");
        System.out.println("  --version <версия>    версия пакета, например 13.0.3 (по умолчанию latest)");
        System.out.println("  --ascii-tree          выводить зависимости ASCII-деревом");
        System.out.println("  --max-depth <число>   максимальная глубина, 1..1000 (по умолчанию 5)");
        System.out.println("  --filter <подстрока>  пропускать пакеты, в имени которых есть подстрока");
    }
}
