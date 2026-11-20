package depgraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Построение графа зависимостей рекурсивным обходом в глубину. */
public class DependencyGraph {

    private final Map<String, List<String>> testRepository;
    private final NugetRepository nugetRepository;
    private final int maxDepth;
    private final String filter;
    private final Map<String, Set<String>> graph = new LinkedHashMap<>();
    private final Map<String, Set<String>> cycleEdges = new LinkedHashMap<>();
    private final Map<String, Integer> expandedDepth = new HashMap<>();
    private final Map<String, List<Dependency>> dependencyCache = new HashMap<>();
    private final Set<String> currentPath = new HashSet<>();

    /** Создаёт построитель графа для тестового репозитория. */
    public DependencyGraph(Map<String, List<String>> repository, int maxDepth, String filter) {
        this.testRepository = repository;
        this.nugetRepository = null;
        this.maxDepth = maxDepth;
        this.filter = filter.toLowerCase(Locale.ROOT);
    }

    /** Создаёт построитель графа для репозитория NuGet. */
    public DependencyGraph(NugetRepository repository, int maxDepth, String filter) {
        this.testRepository = null;
        this.nugetRepository = repository;
        this.maxDepth = maxDepth;
        this.filter = filter.toLowerCase(Locale.ROOT);
    }

    /** Строит граф; глубина исходного пакета равна нулю. */
    public void build(String name, String version) throws IOException {
        graph.clear();
        cycleEdges.clear();
        expandedDepth.clear();
        dependencyCache.clear();
        currentPath.clear();
        if (excluded(name)) {
            return;
        }
        if (testRepository != null && !testRepository.containsKey(name)) {
            throw new IOException("пакет " + name + " не найден в тестовом репозитории");
        }
        String actualVersion = testRepository == null ? nugetRepository.resolveVersion(name, version) : "";
        dfs(name, actualVersion, 0);
    }

    private void dfs(String name, String version, int depth) throws IOException {
        String current = nodeName(name, version);
        addNode(current);
        if (depth >= maxDepth || alreadyExpanded(current, depth)) {
            return;
        }
        expandedDepth.put(current, depth);
        currentPath.add(current);
        try {
            for (Dependency dependency : dependencies(name, version, current)) {
                if (excluded(dependency.name)) {
                    continue;
                }
                String nextVersion = testRepository == null
                        ? nugetRepository.resolveDependencyVersion(dependency.name, dependency.version) : "";
                String next = nodeName(dependency.name, nextVersion);
                graph.get(current).add(next);
                addNode(next);
                if (currentPath.contains(next)) {
                    addCycle(current, next);
                } else {
                    dfs(dependency.name, nextVersion, depth + 1);
                }
            }
        } finally {
            currentPath.remove(current);
        }
    }

    private List<Dependency> dependencies(String name, String version, String node) throws IOException {
        if (!dependencyCache.containsKey(node)) {
            List<Dependency> result;
            if (testRepository == null) {
                result = nugetRepository.getDependencies(name, version);
            } else {
                result = testDependencies(name);
            }
            dependencyCache.put(node, result);
        }
        return dependencyCache.get(node);
    }

    private List<Dependency> testDependencies(String name) {
        List<Dependency> result = new ArrayList<>();
        List<String> names = testRepository.getOrDefault(name, List.of());
        for (String dependency : names) {
            result.add(new Dependency(dependency, "", ""));
        }
        return result;
    }

    /** Повторный обход нужен только если вершина найдена на меньшей глубине. */
    private boolean alreadyExpanded(String node, int depth) {
        Integer previousDepth = expandedDepth.get(node);
        return previousDepth != null && previousDepth <= depth;
    }

    private boolean excluded(String name) {
        return !filter.isEmpty() && name.toLowerCase(Locale.ROOT).contains(filter);
    }

    private String nodeName(String name, String version) {
        return testRepository == null ? name.toLowerCase(Locale.ROOT) + "@" + version : name;
    }

    private void addNode(String node) {
        if (!graph.containsKey(node)) {
            graph.put(node, new LinkedHashSet<>());
        }
    }

    private void addCycle(String from, String to) {
        if (!cycleEdges.containsKey(from)) {
            cycleEdges.put(from, new LinkedHashSet<>());
        }
        cycleEdges.get(from).add(to);
    }

    /** Возвращает вершины и их соседей в порядке обнаружения. */
    public Map<String, Set<String>> getGraph() {
        return graph;
    }

    /** Проверяет, замкнуло ли ребро цикл при обходе. */
    public boolean isCycle(String from, String to) {
        return cycleEdges.containsKey(from) && cycleEdges.get(from).contains(to);
    }

    /** Проверяет, были ли просмотрены зависимости вершины. */
    public boolean isExpanded(String node) {
        return expandedDepth.containsKey(node);
    }
}
