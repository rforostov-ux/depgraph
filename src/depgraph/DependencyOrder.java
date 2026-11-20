package depgraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Порядок загрузки: каждая зависимость находится раньше зависящего от неё пакета. */
public class DependencyOrder {

    private final Map<String, Set<String>> graph;
    private final Set<String> visited = new HashSet<>();
    private final Set<String> currentPath = new HashSet<>();
    private final List<String> path = new ArrayList<>();
    private final List<String> order = new ArrayList<>();

    private DependencyOrder(Map<String, Set<String>> graph) {
        this.graph = graph;
    }

    /** Возвращает порядок для построенного графа или сообщает о цикле. */
    public static List<String> getOrder(Map<String, Set<String>> graph) {
        DependencyOrder result = new DependencyOrder(graph);
        for (String node : graph.keySet()) {
            result.visit(node);
        }
        return result.order;
    }

    private void visit(String node) {
        if (currentPath.contains(node)) {
            throw new IllegalArgumentException("невозможно определить порядок загрузки: цикл " + cycle(node));
        }
        if (visited.contains(node)) {
            return;
        }
        currentPath.add(node);
        path.add(node);
        for (String dependency : graph.getOrDefault(node, Set.of())) {
            visit(dependency);
        }
        path.remove(path.size() - 1);
        currentPath.remove(node);
        visited.add(node);
        order.add(node);
    }

    private String cycle(String node) {
        List<String> cycle = new ArrayList<>(path.subList(path.indexOf(node), path.size()));
        cycle.add(node);
        return String.join(" -> ", cycle);
    }
}
