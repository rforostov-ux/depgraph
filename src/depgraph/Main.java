package depgraph;

import java.io.IOException;
import java.util.Locale;

/**
 * Инструмент анализа зависимостей NuGet. Этап 5: визуализация графа.
 */
public class Main {

    /**
     * Точка входа: читает параметры и выводит граф или сообщение об ошибке.
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
                System.out.println("# Граф пуст: исходный пакет исключён фильтром");
                if (config.asciiTree) {
                    System.out.println("\nASCII-дерево:\n(граф пуст)");
                }
                return;
            }
            DependencyGraph graph;
            if (config.repoMode.equals("test")) {
                graph = new DependencyGraph(TestRepository.load(config.repository), config.maxDepth, config.filter);
            } else {
                graph = new DependencyGraph(new NugetRepository(config.repository), config.maxDepth, config.filter);
            }
            graph.build(config.packageName, config.version);
            GraphVisualizer visualizer = new GraphVisualizer(graph);
            System.out.print(visualizer.toD2());
            if (config.asciiTree) {
                System.out.println("\nASCII-дерево:");
                System.out.print(visualizer.toAsciiTree());
            }
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }

    private static boolean isRootExcluded(Config config) {
        return !config.filter.isEmpty()
                && config.packageName.toLowerCase(Locale.ROOT).contains(config.filter.toLowerCase(Locale.ROOT));
    }

    private static void printHelp() {
        System.out.println("Использование: depgraph --package <имя> --repo <url|путь> [опции]");
        System.out.println("  --package <имя>       имя пакета (обязательно)");
        System.out.println("  --repo <url|путь>     URL репозитория или путь к тестовому файлу (обязательно)");
        System.out.println("  --repo-mode <режим>   remote (по умолчанию) или test");
        System.out.println("  --version <версия>    версия пакета, например 13.0.3 (по умолчанию latest)");
        System.out.println("  --ascii-tree          дополнительно показать ASCII-дерево");
        System.out.println("  --max-depth <число>   максимальная глубина, 1..1000 (по умолчанию 5)");
        System.out.println("  --filter <подстрока>  пропускать пакеты, в имени которых есть подстрока");
    }
}
