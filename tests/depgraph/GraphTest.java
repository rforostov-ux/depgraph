package depgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Проверки обхода графа и выбора версий зависимостей. */
public class GraphTest {

    private static int failed;

    /** Запускает тесты графа и возвращает ненулевой код при ошибке. */
    public static void main(String[] args) throws IOException {
        testCycle();
        testDepth();
        testFilter();
        testSharedNode();
        testSelfCycle();
        testMissingRoot();
        testFileValidation();
        testVersionComparison();
        testVersionRanges();
        testInvalidRange();
        System.out.println(failed == 0 ? "Все тесты пройдены" : "Провалено: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static Map<String, List<String>> example() {
        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("A", List.of("B", "C"));
        data.put("B", List.of("D"));
        data.put("C", List.of("D", "E"));
        data.put("D", List.of());
        data.put("E", List.of("A"));
        return data;
    }

    private static void testCycle() throws IOException {
        DependencyGraph graph = new DependencyGraph(example(), 5, "");
        graph.build("A", "latest");
        check("рекурсивные зависимости", graph.getGraph().get("C").equals(Set.of("D", "E")));
        check("цикл E -> A", graph.isCycle("E", "A") && graph.getGraph().get("E").contains("A"));
        check("общая зависимость не цикл", !graph.isCycle("C", "D"));
    }

    private static void testDepth() throws IOException {
        DependencyGraph graph = new DependencyGraph(example(), 1, "");
        graph.build("A", "latest");
        check("глубина один: только A, B, C", graph.getGraph().keySet().equals(Set.of("A", "B", "C")));
        check("глубина один: B не раскрыт", !graph.isExpanded("B") && graph.getGraph().get("B").isEmpty());
    }

    private static void testFilter() throws IOException {
        DependencyGraph graph = new DependencyGraph(example(), 5, "c");
        graph.build("A", "latest");
        check("фильтр убирает C и его ветвь", !graph.getGraph().containsKey("C")
                && !graph.getGraph().containsKey("E") && graph.getGraph().containsKey("D"));
        graph = new DependencyGraph(example(), 5, "a");
        graph.build("A", "latest");
        check("фильтр исходного пакета", graph.getGraph().isEmpty());
    }

    private static void testSharedNode() throws IOException {
        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("A", List.of("B", "X"));
        data.put("B", List.of("C"));
        data.put("C", List.of("X"));
        data.put("X", List.of("Y"));
        data.put("Y", List.of("Z"));
        DependencyGraph graph = new DependencyGraph(data, 4, "");
        graph.build("A", "latest");
        check("повторный обход по более короткому пути", graph.getGraph().containsKey("Z"));
        check("лист без собственной строки", graph.getGraph().get("Z").isEmpty());
    }

    private static void testSelfCycle() throws IOException {
        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("A", List.of("A"));
        DependencyGraph graph = new DependencyGraph(data, 5, "");
        graph.build("A", "latest");
        check("цикл на себя", graph.isCycle("A", "A") && graph.getGraph().size() == 1);
    }

    private static void testMissingRoot() {
        DependencyGraph graph = new DependencyGraph(example(), 5, "");
        try {
            graph.build("Z", "latest");
            check("неизвестный исходный пакет", false);
        } catch (IOException e) {
            check("неизвестный исходный пакет", true);
        }
    }

    private static void testFileValidation() throws IOException {
        Path file = Files.createTempFile("depgraph-invalid-", ".txt");
        try {
            Files.writeString(file, "A: b\n");
            try {
                TestRepository.load(file.toString());
                check("ошибка: имя не заглавными буквами", false);
            } catch (IOException e) {
                check("ошибка: имя не заглавными буквами", true);
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void testVersionComparison() {
        check("нормализация 1.02", NugetVersion.normalize("1.02").equals("1.2.0"));
        check("нулевая четвёртая часть", NugetVersion.normalize("1.2.3.0").equals("1.2.3"));
        check("метаданные версии", NugetVersion.normalize("1.2.3+abc").equals("1.2.3"));
        check("сравнение чисел версии", NugetVersion.compare("1.10", "1.2") > 0);
        check("сравнение предварительных версий", NugetVersion.compare("1.0-rc.2", "1.0-rc.10") < 0);
        check("стабильная версия выше предварительной", NugetVersion.compare("1.0", "1.0-rc") > 0);
    }

    private static void testVersionRanges() throws IOException {
        List<String> versions = List.of("2.0.0", "1.2.0-rc.10", "1.5.0", "1.0.0", "1.2.0-rc.2", "1.2.0");
        check("нижняя граница", new VersionRange("1.2").select(versions).equals("1.2.0"));
        check("открытая граница", new VersionRange("(1.0, 2.0]").select(versions).equals("1.2.0"));
        check("точная версия", new VersionRange("[1.0]").select(versions).equals("1.0.0"));
        check("шаблон версии", new VersionRange("1.*").select(versions).equals("1.5.0"));
        check("последняя стабильная версия", new VersionRange("latest").select(versions).equals("2.0.0"));
        check("диапазон предварительных версий",
                new VersionRange("[1.2.0-rc.2,1.2.0]").select(versions).equals("1.2.0-rc.2"));
        check("дефис в метаданных не включает предрелизы",
                new VersionRange("1.2.0+build-test").select(versions).equals("1.2.0"));
    }

    private static void testInvalidRange() {
        try {
            new VersionRange("[1.0,1.0)");
            check("некорректный диапазон", false);
        } catch (IllegalArgumentException e) {
            check("некорректный диапазон", true);
        }
        try {
            new VersionRange("[3.0]").select(List.of("1.0.0"));
            check("нет подходящей версии", false);
        } catch (IOException e) {
            check("нет подходящей версии", true);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (!ok) {
            failed++;
        }
    }
}
