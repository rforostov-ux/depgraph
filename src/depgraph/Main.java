package depgraph;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Инструмент визуализации графа зависимостей пакетов NuGet. Этап 3: построение графа.
 */
public class Main {

    /**
     * Точка входа: читает параметры и выводит граф зависимостей или сообщение об ошибке.
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
            if (isRootExcluded(config)) {
                System.out.println("Граф пуст: исходный пакет исключён фильтром");
                return;
            }
            DependencyGraph graph;
            if (config.repoMode.equals("test")) {
                graph = new DependencyGraph(TestRepository.load(config.repository), config.maxDepth, config.filter);
            } else {
                graph = new DependencyGraph(new NugetRepository(config.repository), config.maxDepth, config.filter);
            }
            graph.build(config.packageName, config.version);
            printGraph(graph);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean isRootExcluded(Config config) {
        return !config.filter.isEmpty()
                && config.packageName.toLowerCase(Locale.ROOT).contains(config.filter.toLowerCase(Locale.ROOT));
    }

    private static void printGraph(DependencyGraph graph) {
        if (graph.getGraph().isEmpty()) {
            System.out.println("Граф пуст: исходный пакет исключён фильтром");
            return;
        }
        System.out.println("Граф зависимостей:");
        for (Map.Entry<String, Set<String>> entry : graph.getGraph().entrySet()) {
            printNode(graph, entry.getKey(), entry.getValue());
        }
    }

    private static void printNode(DependencyGraph graph, String node, Set<String> dependencies) {
        if (dependencies.isEmpty()) {
            String message = graph.isExpanded(node) ? "нет зависимостей в графе" : "предел глубины";
            System.out.println(node + " -> (" + message + ")");
            return;
        }
        for (String dependency : dependencies) {
            String marker = graph.isCycle(node, dependency) ? " (цикл)" : "";
            System.out.println(node + " -> " + dependency + marker);
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
