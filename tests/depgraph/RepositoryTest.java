package depgraph;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Тесты получения зависимостей без обращения к сети: разбор индекса, списка версий, .nuspec и тестового файла.
 */
public class RepositoryTest {

    private static final String INDEX = "{\"resources\": ["
            + "{\"@id\": \"https://search.example/query\", \"@type\": \"SearchQueryService\"},"
            + "{\"@id\": \"https://api.nuget.org/v3-flatcontainer/\", \"@type\": \"PackageBaseAddress/3.0.0\"}]}";
    private static final String NUSPEC = "﻿<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<package xmlns=\"http://schemas.microsoft.com/packaging/2013/05/nuspec.xsd\"><metadata>"
            + "<dependencies><group targetFramework=\"net6.0\">"
            + "<dependency id=\"Serilog\" version=\"4.0.0\" /><dependency id=\"System.Memory\" version=\"4.5.5\" />"
            + "</group><group targetFramework=\"net462\" /></dependencies></metadata></package>";
    private static final String FLAT_NUSPEC = "<package><metadata><dependencies>"
            + "<dependency id=\"Newtonsoft.Json\" version=\"[13.0.1, )\" /></dependencies></metadata></package>";
    private static int failed;

    /**
     * Запускает тесты; при провале завершает процесс с кодом 1.
     *
     * @param args не используются
     * @throws IOException если тестовые данные не удалось разобрать
     */
    public static void main(String[] args) throws IOException {
        check("адрес хранилища из индекса",
                "https://api.nuget.org/v3-flatcontainer/".equals(NugetRepository.extractBaseAddress(INDEX)));
        check("нет PackageBaseAddress", NugetRepository.extractBaseAddress("{\"resources\": []}") == null);
        check("последняя стабильная версия",
                NugetRepository.pickLatest("{\"versions\":[\"1.0.0\",\"2.0.0\",\"2.1.0-beta\"]}").equals("2.0.0"));
        check("только предрелизы", NugetRepository.pickLatest("{\"versions\":[\"1.0.0-rc1\"]}").equals("1.0.0-rc1"));
        check("нормализация версии", NugetRepository.normalizeVersion("6.0").equals("6.0.0"));

        List<Dependency> deps = NugetRepository.parseNuspec(NUSPEC);
        check("зависимости из групп", deps.size() == 2 && deps.get(0).name.equals("Serilog"));
        check("версия и платформа", deps.get(1).version.equals("4.5.5") && deps.get(1).framework.equals("net6.0"));
        List<Dependency> flat = NugetRepository.parseNuspec(FLAT_NUSPEC);
        check("зависимости без групп", flat.size() == 1 && flat.get(0).framework.isEmpty());
        check("пакет без зависимостей", NugetRepository.parseNuspec("<package><metadata/></package>").isEmpty());
        expectError("сломанный .nuspec", "<package><metadata>");

        Map<String, List<String>> graph = TestRepository.load("tests/data/test_repo.txt");
        check("тестовый репозиторий: A -> B C", graph.get("A").equals(List.of("B", "C")));
        check("тестовый репозиторий: D без зависимостей", graph.get("D").isEmpty());

        System.out.println(failed == 0 ? "Все тесты пройдены" : "Провалено: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void expectError(String name, String xml) {
        try {
            NugetRepository.parseNuspec(xml);
            check(name, false);
        } catch (IOException e) {
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
