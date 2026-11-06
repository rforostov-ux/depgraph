package depgraph;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Получение прямых зависимостей пакета из репозитория NuGet (протокол v3, ресурс PackageBaseAddress).
 */
public class NugetRepository {

    private static final int HTTP_OK = 200;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int VERSION_PARTS = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(TIMEOUT)
            .build();
    private final String baseAddress;

    /**
     * Подключается к репозиторию: по URL индекса (…/index.json) находит адрес хранилища пакетов.
     *
     * @param repoUrl URL индекса репозитория или сразу адрес хранилища пакетов
     * @throws IOException если индекс не удалось загрузить или в нём нет нужного ресурса
     */
    public NugetRepository(String repoUrl) throws IOException {
        if (!repoUrl.endsWith("index.json")) {
            baseAddress = withSlash(repoUrl);
            return;
        }
        String index = download(repoUrl);
        if (index == null) {
            throw new IOException("индекс репозитория не найден: " + repoUrl);
        }
        String address = extractBaseAddress(index);
        if (address == null) {
            throw new IOException("в индексе репозитория нет ресурса PackageBaseAddress/3.0.0");
        }
        baseAddress = address;
    }

    /**
     * Определяет конкретную версию пакета: для latest — последнюю стабильную из репозитория.
     *
     * @param name    имя пакета
     * @param version версия из параметров или latest
     * @return номер версии в нормализованном виде
     * @throws IOException если пакет не найден или репозиторий недоступен
     */
    public String resolveVersion(String name, String version) throws IOException {
        if (!version.equals("latest")) {
            return normalizeVersion(version);
        }
        String json = download(baseAddress + name.toLowerCase(Locale.ROOT) + "/index.json");
        if (json == null) {
            throw new IOException("пакет " + name + " не найден в репозитории");
        }
        return pickLatest(json);
    }

    /**
     * Загружает файл .nuspec пакета заданной версии и извлекает из него прямые зависимости.
     *
     * @param name    имя пакета
     * @param version конкретная версия пакета
     * @return список прямых зависимостей
     * @throws IOException если пакет или версия не найдены, либо файл .nuspec некорректен
     */
    public List<Dependency> getDependencies(String name, String version) throws IOException {
        String id = name.toLowerCase(Locale.ROOT);
        String nuspec = download(baseAddress + id + "/" + version + "/" + id + ".nuspec");
        if (nuspec == null) {
            throw new IOException("пакет " + name + " версии " + version + " не найден в репозитории");
        }
        return parseNuspec(nuspec);
    }

    private String download(String url) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("загрузка прервана: " + url);
        } catch (IOException e) {
            throw new IOException("не удалось подключиться к " + url + " (" + e.getClass().getSimpleName() + ")");
        }
        if (response.statusCode() == HTTP_NOT_FOUND) {
            return null;
        }
        if (response.statusCode() != HTTP_OK) {
            throw new IOException("сервер вернул код " + response.statusCode() + " для " + url);
        }
        return response.body();
    }

    static String extractBaseAddress(String indexJson) {
        for (String resource : indexJson.split("\\{")) {
            if (resource.contains("\"PackageBaseAddress/3.0.0\"")) {
                String id = readJsonString(resource, "@id");
                if (id != null) {
                    return withSlash(id);
                }
            }
        }
        return null;
    }

    static String pickLatest(String versionsJson) throws IOException {
        int open = versionsJson.indexOf('[');
        int close = versionsJson.indexOf(']');
        if (open < 0 || close < open) {
            throw new IOException("некорректный список версий пакета");
        }
        String last = null;
        String lastStable = null;
        for (String item : versionsJson.substring(open + 1, close).split(",")) {
            String version = item.trim().replace("\"", "");
            if (!version.isEmpty()) {
                last = version;
                if (!version.contains("-")) {
                    lastStable = version;
                }
            }
        }
        if (last == null) {
            throw new IOException("у пакета нет ни одной версии");
        }
        if (lastStable != null) {
            return lastStable;
        }
        return last;
    }

    static List<Dependency> parseNuspec(String xml) throws IOException {
        Document document = readXml(xml);
        List<Dependency> result = new ArrayList<>();
        NodeList nodes = document.getElementsByTagNameNS("*", "dependency");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element dependency = (Element) nodes.item(i);
            Element parent = (Element) dependency.getParentNode();
            String framework = parent.getAttribute("targetFramework");
            result.add(new Dependency(dependency.getAttribute("id"), dependency.getAttribute("version"), framework));
        }
        return result;
    }

    static String normalizeVersion(String version) {
        String result = version;
        int parts = version.split("\\.").length;
        for (int i = parts; i < VERSION_PARTS; i++) {
            result = result + ".0";
        }
        return result;
    }

    private static Document readXml(String xml) throws IOException {
        String text = xml;
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            return builder.parse(new InputSource(new StringReader(text)));
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("некорректный файл .nuspec: " + e.getMessage());
        }
    }

    private static String readJsonString(String json, String key) {
        int keyPos = json.indexOf("\"" + key + "\"");
        if (keyPos < 0) {
            return null;
        }
        int colon = json.indexOf(':', keyPos);
        int open = json.indexOf('"', colon + 1);
        int close = json.indexOf('"', open + 1);
        if (colon < 0 || open < 0 || close < 0) {
            return null;
        }
        return json.substring(open + 1, close);
    }

    private static String withSlash(String url) {
        if (url.endsWith("/")) {
            return url;
        }
        return url + "/";
    }
}
