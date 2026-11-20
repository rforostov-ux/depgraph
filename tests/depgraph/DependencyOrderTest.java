package depgraph;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Проверки порядка загрузки для различных графов зависимостей. */
public class DependencyOrderTest {

    private static final int DEPTH = 5;
    private static final int ONE_LEVEL = 1;
    private static final int SUCCESS = 0;
    private static int failed;

    /** Запускает тесты порядка загрузки. */
    public static void main(String[] args) throws IOException {
        testSharedDependency();
        testNoDependencies();
        testDepthLimit();
        testFilter();
        testCycle();
        testSelfCycle();
        testCycleAcrossBranches();
        testEmptyGraph();
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

    private static DependencyGraph build(Map<String, List<String>> data, int depth, String filter)
            throws IOException {
        DependencyGraph graph = new DependencyGraph(data, depth, filter);
        graph.build("A", "latest");
        return graph;
    }

    private static void testSharedDependency() throws IOException {
        DependencyGraph graph = build(example(), DEPTH, "");
        List<String> order = DependencyOrder.getOrder(graph.getGraph());
        check("порядок: общая зависимость выводится один раз", order.equals(List.of("D", "B", "E", "C", "A")));
    }

    private static void testNoDependencies() throws IOException {
        DependencyGraph graph = new DependencyGraph(example(), DEPTH, "");
        graph.build("D", "latest");
        check("порядок: пакет без зависимостей", DependencyOrder.getOrder(graph.getGraph()).equals(List.of("D")));
    }

    private static void testDepthLimit() throws IOException {
        DependencyGraph graph = build(example(), ONE_LEVEL, "");
        check("порядок: только в пределах глубины",
                DependencyOrder.getOrder(graph.getGraph()).equals(List.of("B", "C", "A")));
    }

    private static void testFilter() throws IOException {
        Map<String, List<String>> data = example();
        data.put("E", List.of("A"));
        DependencyGraph graph = build(data, DEPTH, "c");
        check("порядок: фильтр исключает ветвь с циклом",
                DependencyOrder.getOrder(graph.getGraph()).equals(List.of("D", "B", "A")));
    }

    private static void testCycle() throws IOException {
        Map<String, List<String>> data = example();
        data.put("E", List.of("A"));
        expectCycle("порядок: цикл из нескольких пакетов", build(data, DEPTH, ""), "A -> C -> E -> A");
    }

    private static void testSelfCycle() throws IOException {
        Map<String, List<String>> data = example();
        data.put("A", List.of("A"));
        expectCycle("порядок: цикл на себя", build(data, DEPTH, ""), "A -> A");
    }

    private static void testCycleAcrossBranches() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put("A", Set.of("B"));
        graph.put("B", Set.of("C"));
        graph.put("C", Set.of("B"));
        try {
            DependencyOrder.getOrder(graph);
            check("порядок: цикл вне исходного пакета", false);
        } catch (IllegalArgumentException e) {
            check("порядок: цикл вне исходного пакета", e.getMessage().contains("B -> C -> B"));
        }
    }

    private static void testEmptyGraph() {
        check("порядок: пустой граф", DependencyOrder.getOrder(Map.of()).isEmpty());
    }

    private static void expectCycle(String name, DependencyGraph graph, String cycle) {
        try {
            DependencyOrder.getOrder(graph.getGraph());
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage().contains(cycle));
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (!ok) {
            failed++;
        }
    }
}
