package depgraph;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/** Ограничение версии зависимости NuGet и выбор подходящей версии. */
public class VersionRange {

    private final String text;
    private String lower;
    private String upper;
    private String prefix;
    private boolean includeLower;
    private boolean includeUpper;
    private boolean allowPrerelease;
    private boolean chooseHighest;
    private boolean latest;

    /** Создаёт ограничение из версии, интервала или числового шаблона с *. */
    public VersionRange(String value) {
        text = value == null ? "" : value.trim();
        parse();
    }

    private void parse() {
        if (text.equals("latest")) {
            latest = true;
            chooseHighest = true;
        } else if (text.contains("*")) {
            parseFloating();
        } else if (text.startsWith("[") || text.startsWith("(")) {
            parseInterval();
        } else if (!text.isEmpty()) {
            lower = validVersion(text);
            includeLower = true;
        }
        allowPrerelease = (lower != null && lower.contains("-")) || (upper != null && upper.contains("-"));
    }

    private void parseFloating() {
        if (!text.matches("([0-9]+\\.){0,3}\\*")) {
            throw new IllegalArgumentException("неподдерживаемый шаблон версии: " + text);
        }
        StringBuilder result = new StringBuilder();
        String numbers = text.substring(0, text.length() - 1);
        for (String part : numbers.split("\\.")) {
            if (!part.isEmpty()) {
                result.append(new BigInteger(part)).append('.');
            }
        }
        prefix = result.toString();
        chooseHighest = true;
    }

    private void parseInterval() {
        if (!(text.endsWith("]") || text.endsWith(")"))) {
            throw new IllegalArgumentException("неверный диапазон версий: " + text);
        }
        includeLower = text.startsWith("[");
        includeUpper = text.endsWith("]");
        String inner = text.substring(1, text.length() - 1);
        String[] bounds = inner.split(",", -1);
        if (bounds.length == 1) {
            parseExact(bounds[0]);
        } else if (bounds.length == 2) {
            lower = optionalVersion(bounds[0]);
            upper = optionalVersion(bounds[1]);
            checkBounds();
        } else {
            throw new IllegalArgumentException("неверный диапазон версий: " + text);
        }
    }

    private void parseExact(String value) {
        if (!includeLower || !includeUpper || value.trim().isEmpty()) {
            throw new IllegalArgumentException("неверная точная версия: " + text);
        }
        lower = validVersion(value.trim());
        upper = lower;
    }

    private String optionalVersion(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : validVersion(trimmed);
    }

    private String validVersion(String value) {
        if (!NugetVersion.isValid(value)) {
            throw new IllegalArgumentException("неверная версия в ограничении: " + text);
        }
        return NugetVersion.normalize(value);
    }

    private void checkBounds() {
        if (lower == null || upper == null) {
            return;
        }
        int comparison = NugetVersion.compare(lower, upper);
        if (comparison > 0 || (comparison == 0 && (!includeLower || !includeUpper))) {
            throw new IllegalArgumentException("пустой диапазон версий: " + text);
        }
    }

    /** Выбирает из доступных версий наименьшую подходящую или наибольшую для *. */
    public String select(List<String> versions) throws IOException {
        String result = selectFrom(versions, allowPrerelease);
        if (result == null && latest) {
            result = selectFrom(versions, true);
        }
        if (result == null) {
            throw new IOException("не найдена версия для ограничения: " + text);
        }
        return result;
    }

    private String selectFrom(List<String> versions, boolean withPrerelease) {
        String result = null;
        for (String candidate : versions) {
            if (!NugetVersion.isValid(candidate) || !matches(candidate, withPrerelease)) {
                continue;
            }
            if (result == null || better(candidate, result)) {
                result = candidate;
            }
        }
        return result;
    }

    private boolean better(String candidate, String current) {
        int comparison = NugetVersion.compare(candidate, current);
        return chooseHighest ? comparison > 0 : comparison < 0;
    }

    private boolean matches(String version, boolean withPrerelease) {
        String normalized = NugetVersion.normalize(version);
        if (!withPrerelease && normalized.contains("-")) {
            return false;
        }
        if (prefix != null && !normalized.startsWith(prefix)) {
            return false;
        }
        return satisfiesLower(normalized) && satisfiesUpper(normalized);
    }

    private boolean satisfiesLower(String version) {
        if (lower == null) {
            return true;
        }
        int comparison = NugetVersion.compare(version, lower);
        return comparison > 0 || (comparison == 0 && includeLower);
    }

    private boolean satisfiesUpper(String version) {
        if (upper == null) {
            return true;
        }
        int comparison = NugetVersion.compare(version, upper);
        return comparison < 0 || (comparison == 0 && includeUpper);
    }
}
