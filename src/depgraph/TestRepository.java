package depgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Тестовый репозиторий: файл, где каждая строка имеет вид {@code A: B C}
 * (пакет A напрямую зависит от пакетов B и C).
 */
public class TestRepository {

    /**
     * Читает файл тестового репозитория.
     *
     * @param path путь к файлу
     * @return словарь «пакет → список прямых зависимостей»
     * @throws IOException если файл не читается или строка записана не по формату
     */
    public static Map<String, List<String>> load(String path) throws IOException {
        Map<String, List<String>> graph = new LinkedHashMap<>();
        List<String> lines = Files.readAllLines(Path.of(path));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon < 0) {
                throw new IOException("строка " + (i + 1) + " тестового репозитория: ожидается формат 'A: B C'");
            }
            String name = line.substring(0, colon).trim();
            List<String> dependencies = new ArrayList<>();
            for (String dependency : line.substring(colon + 1).trim().split("\\s+")) {
                if (!dependency.isEmpty()) {
                    dependencies.add(dependency);
                }
            }
            graph.put(name, dependencies);
        }
        return graph;
    }
}
