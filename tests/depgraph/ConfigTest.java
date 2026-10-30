package depgraph;


/**
 * Тесты разбора и проверки параметров (без внешних библиотек).
 */
public class ConfigTest {

    private static final String URL = "https://api.nuget.org/v3/index.json";
    private static final String FILE = "tests/data/test_repo.txt";
    private static final int DEFAULT_DEPTH = 5;
    private static final int CUSTOM_DEPTH = 3;
    private static int failed;

    /**
     * Запускает тесты; при провале завершает процесс с кодом 1.
     *
     * @param args не используются
     */
    public static void main(String[] args) {
        Config c = Config.parse(new String[] {"--package", "Serilog", "--repo", URL});
        check("значения по умолчанию", c.version.equals("latest") && c.maxDepth == DEFAULT_DEPTH && !c.asciiTree);
        c = Config.parse(new String[] {"--package", "A", "--repo", FILE, "--repo-mode", "test",
            "--ascii-tree", "--max-depth", "3", "--filter", "X", "--version", "1.0.0"});
        check("все параметры", c.asciiTree && c.maxDepth == CUSTOM_DEPTH && c.filter.equals("X"));
        fails("неизвестная опция", "--package", "A", "--repo", URL, "--color", "red");
        fails("нет значения", "--package", "--repo", URL);
        fails("нет --repo", "--package", "Serilog");
        fails("плохое имя", "--package", "my pkg!", "--repo", URL);
        fails("строчные в тесте", "--package", "abc", "--repo", FILE, "--repo-mode", "test");
        fails("плохой URL", "--package", "Serilog", "--repo", "api.nuget.org");
        fails("нет файла", "--package", "A", "--repo", "nope.txt", "--repo-mode", "test");
        fails("плохой режим", "--package", "A", "--repo", URL, "--repo-mode", "local");
        fails("плохая версия", "--package", "A", "--repo", URL, "--version", "v1.x");
        fails("глубина не число", "--package", "A", "--repo", URL, "--max-depth", "abc");
        fails("глубина 0", "--package", "A", "--repo", URL, "--max-depth", "0");
        System.out.println(failed == 0 ? "Все тесты пройдены" : "Провалено: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void fails(String name, String... args) {
        try {
            Config.parse(args);
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (!ok) {
            failed++;
        }
    }
}
