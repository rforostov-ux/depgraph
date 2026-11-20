package depgraph;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Инструмент анализа зависимостей NuGet. Этап 4: порядок загрузки пакетов.
 */
public class Main {

    /**
     * Точка входа: читает параметры и выводит порядок загрузки или сообщение об ошибке.
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
            printOrder(graph);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean isRootExcluded(Config config) {
        return !config.filter.isEmpty()
                && config.packageName.toLowerCase(Locale.ROOT).contains(config.filter.toLowerCase(Locale.ROOT));
    }

    private static void printOrder(DependencyGraph graph) {
        List<String> order = DependencyOrder.getOrder(graph.getGraph());
        System.out.println("Порядок загрузки (исходный пакет — последний):");
        int number = 1;
        for (String node : order) {
            System.out.println(number + ". " + node);
            number++;
        }
        if (hasUnexpandedNodes(graph)) {
            System.out.println("Внимание: порядок учитывает только граф в пределах заданной глубины.");
        }
    }

    private static boolean hasUnexpandedNodes(DependencyGraph graph) {
        for (String node : graph.getGraph().keySet()) {
            if (!graph.isExpanded(node)) {
                return true;
            }
        }
        return false;
    }

    private static void printHelp() {
        System.out.println("Использование: depgraph --package <имя> --repo <url|путь> [опции]");
        System.out.println("  --package <имя>       имя пакета (обязательно)");
        System.out.println("  --repo <url|путь>     URL репозитория или путь к тестовому файлу (обязательно)");
        System.out.println("  --repo-mode <режим>   remote (по умолчанию) или test");
        System.out.println("  --version <версия>    версия пакета, например 13.0.3 (по умолчанию latest)");
        System.out.println("  --ascii-tree          опция для этапа 5 (на этапе 4 не влияет на вывод)");
        System.out.println("  --max-depth <число>   максимальная глубина, 1..1000 (по умолчанию 5)");
        System.out.println("  --filter <подстрока>  пропускать пакеты, в имени которых есть подстрока");
    }
}
