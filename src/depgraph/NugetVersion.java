package depgraph;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Нормализация и сравнение версий пакетов NuGet. */
public class NugetVersion {

    private static final int MIN_PARTS = 3;
    private static final int MAX_PARTS = 4;
    private static final String FORMAT = "[0-9]+(\\.[0-9]+){0,3}"
            + "(-[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?"
            + "(\\+[0-9A-Za-z-]+(\\.[0-9A-Za-z-]+)*)?";

    private NugetVersion() {
    }

    /** Проверяет номер версии, включая необязательные суффиксы. */
    public static boolean isValid(String version) {
        return version != null && version.matches(FORMAT);
    }

    /** Приводит версию к виду, используемому в адресах NuGet v3. */
    public static String normalize(String version) {
        if (!isValid(version)) {
            throw new IllegalArgumentException("неверная версия пакета: " + version);
        }
        String withoutMetadata = version.split("\\+", 2)[0].toLowerCase(Locale.ROOT);
        String[] sections = withoutMetadata.split("-", 2);
        List<String> parts = new ArrayList<>();
        for (String part : sections[0].split("\\.")) {
            parts.add(new BigInteger(part).toString());
        }
        while (parts.size() < MIN_PARTS) {
            parts.add("0");
        }
        if (parts.size() == MAX_PARTS && parts.get(MAX_PARTS - 1).equals("0")) {
            parts.remove(MAX_PARTS - 1);
        }
        String result = String.join(".", parts);
        return sections.length > 1 ? result + "-" + sections[1] : result;
    }

    /** Сравнивает версии по числовым частям и суффиксу предварительного выпуска. */
    public static int compare(String first, String second) {
        String[] a = normalize(first).split("-", 2);
        String[] b = normalize(second).split("-", 2);
        String[] aParts = a[0].split("\\.");
        String[] bParts = b[0].split("\\.");
        for (int i = 0; i < MAX_PARTS; i++) {
            BigInteger aPart = partAt(aParts, i);
            BigInteger bPart = partAt(bParts, i);
            int result = aPart.compareTo(bPart);
            if (result != 0) {
                return result;
            }
        }
        return comparePrerelease(a, b);
    }

    private static BigInteger partAt(String[] parts, int index) {
        return index < parts.length ? new BigInteger(parts[index]) : BigInteger.ZERO;
    }

    private static int comparePrerelease(String[] a, String[] b) {
        if (a.length == 1 && b.length == 1) {
            return 0;
        }
        if (a.length == 1) {
            return 1;
        }
        if (b.length == 1) {
            return -1;
        }
        String[] aLabels = a[1].split("\\.");
        String[] bLabels = b[1].split("\\.");
        int count = Math.min(aLabels.length, bLabels.length);
        for (int i = 0; i < count; i++) {
            int result = compareLabel(aLabels[i], bLabels[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(aLabels.length, bLabels.length);
    }

    private static int compareLabel(String a, String b) {
        boolean aNumber = a.matches("[0-9]+");
        boolean bNumber = b.matches("[0-9]+");
        if (aNumber && bNumber) {
            return new BigInteger(a).compareTo(new BigInteger(b));
        }
        if (aNumber != bNumber) {
            return aNumber ? -1 : 1;
        }
        return a.compareToIgnoreCase(b);
    }
}
