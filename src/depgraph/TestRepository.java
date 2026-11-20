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
            if (colon < 0 || colon != line.lastIndexOf(':')) {
                throw new IOException("строка " + (i + 1) + " тестового репозитория: ожидается формат 'A: B C'");
            }
            String name = line.substring(0, colon).trim();
            checkName(name, i + 1);
            if (graph.containsKey(name)) {
                throw new IOException("строка " + (i + 1) + ": пакет " + name + " указан повторно");
            }
            List<String> dependencies = new ArrayList<>();
            for (String dependency : line.substring(colon + 1).trim().split("\\s+")) {
                if (!dependency.isEmpty()) {
                    checkName(dependency, i + 1);
                    dependencies.add(dependency);
                }
            }
            graph.put(name, dependencies);
        }
        return graph;
    }

    private static void checkName(String name, int line) throws IOException {
        if (!name.matches("[A-Z]+")) {
            throw new IOException("строка " + line + ": имя пакета должно состоять из заглавных латинских букв");
        }
    }
}
