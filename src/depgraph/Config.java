package depgraph;

import java.io.File;

/**
 * Параметры приложения, заданные опциями командной строки, и их проверка.
 */
public class Config {

    private static final int MIN_DEPTH = 1;
    private static final int MAX_DEPTH = 1000;

    String packageName = null;
    String repository = null;
    String repoMode = "remote";
    String version = "latest";
    boolean asciiTree = false;
    int maxDepth = 5;
    String filter = "";

    /**
     * Читает аргументы командной строки и проверяет их.
     *
     * @param args аргументы командной строки
     * @return заполненные параметры
     * @throws IllegalArgumentException если какой-либо параметр задан неверно
     */
    public static Config parse(String[] args) {
        Config config = new Config();
        int i = 0;
        while (i < args.length) {
            String name = args[i];
            if (name.equals("--ascii-tree")) {
                config.asciiTree = true;
                i = i + 1;
            } else {
                if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                    throw new IllegalArgumentException("у опции " + name + " нет значения");
                }
                config.setOption(name, args[i + 1]);
                i = i + 2;
            }
        }
        config.validate();
        return config;
    }

    /**
     * Выводит все параметры в формате ключ=значение.
     */
    public void print() {
        System.out.println("package=" + packageName);
        System.out.println("repository=" + repository);
        System.out.println("repo_mode=" + repoMode);
        System.out.println("version=" + version);
        System.out.println("ascii_tree=" + asciiTree);
        System.out.println("max_depth=" + maxDepth);
        System.out.println("filter=" + filter);
    }

    private void setOption(String name, String value) {
        switch (name) {
            case "--package":
                packageName = value;
                break;
            case "--repo":
                repository = value;
                break;
            case "--repo-mode":
                repoMode = value;
                break;
            case "--version":
                version = value;
                break;
            case "--max-depth":
                maxDepth = parseDepth(value);
                break;
            case "--filter":
                filter = value;
                break;
            default:
                throw new IllegalArgumentException("неизвестная опция " + name);
        }
    }

    private void validate() {
        if (packageName == null) {
            throw new IllegalArgumentException("не указано имя пакета (--package)");
        }
        if (repository == null) {
            throw new IllegalArgumentException("не указан репозиторий (--repo)");
        }
        if (!repoMode.equals("remote") && !repoMode.equals("test")) {
            throw new IllegalArgumentException("режим должен быть remote или test, а не " + repoMode);
        }
        if (maxDepth < MIN_DEPTH || maxDepth > MAX_DEPTH) {
            throw new IllegalArgumentException("глубина должна быть от 1 до 1000, а не " + maxDepth);
        }
        checkPackageName();
        checkRepository();
        checkVersion();
    }

    private void checkPackageName() {
        if (packageName.isEmpty()) {
            throw new IllegalArgumentException("имя пакета пустое");
        }
        for (char c : packageName.toCharArray()) {
            boolean ok;
            if (repoMode.equals("test")) {
                ok = c >= 'A' && c <= 'Z';
            } else {
                ok = Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_';
            }
            if (!ok) {
                throw new IllegalArgumentException("недопустимый символ '" + c + "' в имени пакета " + packageName);
            }
        }
    }

    private void checkRepository() {
        if (repoMode.equals("test")) {
            if (!new File(repository).isFile()) {
                throw new IllegalArgumentException("файл тестового репозитория не найден: " + repository);
            }
        } else {
            if (!repository.startsWith("http://") && !repository.startsWith("https://")) {
                throw new IllegalArgumentException("URL должен начинаться с http:// или https://: " + repository);
            }
        }
    }

    private void checkVersion() {
        if (version.equals("latest")) {
            return;
        }
        for (char c : version.toCharArray()) {
            if (!Character.isDigit(c) && c != '.') {
                throw new IllegalArgumentException("версия должна быть latest или вида 13.0.3, а не " + version);
            }
        }
    }

    private int parseDepth(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("глубина должна быть числом, а не " + value);
        }
    }
}
