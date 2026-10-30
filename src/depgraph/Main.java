package depgraph;

/**
 * Инструмент визуализации графа зависимостей пакетов NuGet. Этап 1: вывод параметров.
 */
public class Main {

    /**
     * Точка входа: читает параметры и выводит их или сообщение об ошибке.
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
            config.print();
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Использование: depviz --package <имя> --repo <url|путь> [опции]");
        System.out.println("  --package <имя>       имя пакета (обязательно)");
        System.out.println("  --repo <url|путь>     URL репозитория или путь к тестовому файлу (обязательно)");
        System.out.println("  --repo-mode <режим>   remote (по умолчанию) или test");
        System.out.println("  --version <версия>    версия пакета, например 13.0.3 (по умолчанию latest)");
        System.out.println("  --ascii-tree          выводить зависимости ASCII-деревом");
        System.out.println("  --max-depth <число>   максимальная глубина, 1..1000 (по умолчанию 5)");
        System.out.println("  --filter <подстрока>  пропускать пакеты, в имени которых есть подстрока");
    }
}

