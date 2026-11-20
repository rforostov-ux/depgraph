package depgraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Формирует описание D2 и текстовое дерево построенного графа. */
public class GraphVisualizer {

    private final DependencyGraph graph;

    /** Принимает уже построенный граф зависимостей. */
    public GraphVisualizer(DependencyGraph graph) {
        this.graph = graph;
    }

    /** Возвращает граф на языке D2. Стрелка направлена от пакета к зависимости. */
    public String toD2() {
        if (graph.getGraph().isEmpty()) {
            return "# Graph is empty\n";
        }
        StringBuilder result = new StringBuilder("direction: right\n");
        if (hasDepthLimit()) {
            result.append("# Dependencies beyond the depth limit are not shown.\n");
        }
        Map<String, String> ids = nodeIds();
        for (Map.Entry<String, String> entry : ids.entrySet()) {
            result.append(entry.getValue()).append(": \"").append(escape(entry.getKey())).append("\"\n");
        }
        appendEdges(result, ids);
        return result.toString();
    }

    private Map<String, String> nodeIds() {
        Map<String, String> ids = new LinkedHashMap<>();
        int number = 1;
        for (String node : graph.getGraph().keySet()) {
            ids.put(node, "n" + number);
            number++;
        }
        return ids;
    }

    private void appendEdges(StringBuilder result, Map<String, String> ids) {
        for (Map.Entry<String, Set<String>> entry : graph.getGraph().entrySet()) {
            for (String dependency : entry.getValue()) {
                result.append(ids.get(entry.getKey())).append(" -> ").append(ids.get(dependency)).append("\n");
            }
        }
    }

    private boolean hasDepthLimit() {
        for (String node : graph.getGraph().keySet()) {
            if (!graph.isExpanded(node)) {
                return true;
            }
        }
        return false;
    }

    static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    /** Возвращает дерево; циклы и ранее показанные вершины не обходятся повторно. */
    public String toAsciiTree() {
        if (graph.getGraph().isEmpty()) {
            return "(граф пуст)\n";
        }
        String root = graph.getGraph().keySet().iterator().next();
        StringBuilder result = new StringBuilder(root).append("\n");
        Set<String> path = new HashSet<>();
        Set<String> shown = new HashSet<>();
        path.add(root);
        shown.add(root);
        appendChildren(root, "", path, shown, result);
        return result.toString();
    }

    private void appendChildren(String node, String prefix, Set<String> path, Set<String> shown,
                                StringBuilder result) {
        Iterator<String> children = graph.getGraph().get(node).iterator();
        while (children.hasNext()) {
            String child = children.next();
            boolean last = !children.hasNext();
            result.append(prefix).append(last ? "`-- " : "|-- ").append(child);
            if (path.contains(child)) {
                result.append(" (цикл)\n");
            } else if (!shown.add(child)) {
                result.append(" (уже показан)\n");
            } else {
                appendBranch(child, prefix, last, path, shown, result);
            }
        }
    }

    private void appendBranch(String child, String prefix, boolean last, Set<String> path, Set<String> shown,
                              StringBuilder result) {
        if (!graph.isExpanded(child)) {
            result.append(" (предел глубины)");
        }
        result.append("\n");
        path.add(child);
        appendChildren(child, prefix + (last ? "    " : "|   "), path, shown, result);
        path.remove(child);
    }
}
