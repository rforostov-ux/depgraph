package depgraph;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Проверяет представления D2 и ASCII-дерева. */
public class VisualizationTest {

    private static final int DEPTH = 5;
    private static final int ONE_LEVEL = 1;
    private static final int SUCCESS = 0;
    private static int failed;

    /** Запускает проверки визуализации. */
    public static void main(String[] args) throws IOException {
        testD2();
        testTree();
        testCycle();
        testSelfCycle();
        testDepth();
        testFilter();
        testSingleNode();
        testEmptyGraph();
        testEscape();
        System.out.println(failed == SUCCESS ? "Все тесты пройдены" : "Провалено: " + failed);
        System.exit(failed == SUCCESS ? SUCCESS : 1);
    }

    private static Map<String, List<String>> example() {
        Map<String, List<String>> data = new LinkedHashMap<>();
        data.put("A", List.of("B", "C"));
        data.put("B", List.of("D"));
        data.put("C", List.of("D", "E"));
        data.put("D", List.of());
        data.put("E", List.of());
        return data;
    }

    private static DependencyGraph build(Map<String, List<String>> data, String root, int depth, String filter)
            throws IOException {
        DependencyGraph graph = new DependencyGraph(data, depth, filter);
        graph.build(root, "latest");
        return graph;
    }

    private static void testD2() throws IOException {
        GraphVisualizer view = new GraphVisualizer(build(example(), "A", DEPTH, ""));
        String expected = "direction: right\nn1: \"A\"\nn2: \"B\"\nn3: \"D\"\nn4: \"C\"\nn5: \"E\"\n"
                + "n1 -> n2\nn1 -> n4\nn2 -> n3\nn4 -> n3\nn4 -> n5\n";
        check("D2: вершины и все рёбра", view.toD2().equals(expected));
    }

    private static void testTree() throws IOException {
        GraphVisualizer view = new GraphVisualizer(build(example(), "A", DEPTH, ""));
        String expected = "A\n|-- B\n|   `-- D\n`-- C\n    |-- D (уже показан)\n    `-- E\n";
        check("дерево: ветви и общая зависимость", view.toAsciiTree().equals(expected));
    }

    private static void testCycle() throws IOException {
        Map<String, List<String>> data = example();
        data.put("E", List.of("A"));
        GraphVisualizer view = new GraphVisualizer(build(data, "A", DEPTH, ""));
        check("D2: ребро, замыкающее цикл", view.toD2().contains("n5 -> n1\n"));
        check("дерево: цикл обозначен", view.toAsciiTree().contains("`-- A (цикл)\n"));
    }

    private static void testSelfCycle() throws IOException {
        Map<String, List<String>> data = example();
        data.put("A", List.of("A"));
        GraphVisualizer view = new GraphVisualizer(build(data, "A", DEPTH, ""));
        check("D2: цикл на себя", view.toD2().contains("n1 -> n1\n"));
        check("дерево: цикл на себя", view.toAsciiTree().equals("A\n`-- A (цикл)\n"));
    }

    private static void testDepth() throws IOException {
        GraphVisualizer view = new GraphVisualizer(build(example(), "A", ONE_LEVEL, ""));
        String tree = "A\n|-- B (предел глубины)\n`-- C (предел глубины)\n";
        check("дерево: предел глубины", view.toAsciiTree().equals(tree));
        check("D2: предупреждение о глубине", view.toD2().contains("# Dependencies beyond the depth limit"));
    }

    private static void testFilter() throws IOException {
        GraphVisualizer view = new GraphVisualizer(build(example(), "A", DEPTH, "c"));
        check("дерево: фильтр исключает ветвь", view.toAsciiTree().equals("A\n`-- B\n    `-- D\n"));
        check("D2: исключённой вершины нет", !view.toD2().contains(": \"C\""));
    }

    private static void testSingleNode() throws IOException {
        GraphVisualizer view = new GraphVisualizer(build(example(), "D", DEPTH, ""));
        check("D2: пакет без зависимостей", view.toD2().equals("direction: right\nn1: \"D\"\n"));
        check("дерево: пакет без зависимостей", view.toAsciiTree().equals("D\n"));
    }

    private static void testEmptyGraph() throws IOException {
        GraphVisualizer view = new GraphVisualizer(build(example(), "A", DEPTH, "a"));
        check("D2: пустой граф", view.toD2().equals("# Graph is empty\n"));
        check("дерево: пустой граф", view.toAsciiTree().equals("(граф пуст)\n"));
    }

    private static void testEscape() {
        check("D2: экранирование кавычек и обратного слеша",
                GraphVisualizer.escape("x\\y\"z").equals("x\\\\y\\\"z"));
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (!ok) {
            failed++;
        }
    }
}
