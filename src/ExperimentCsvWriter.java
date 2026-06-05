import java.io.*;
import java.nio.file.*;

public class ExperimentCsvWriter {
    private static final Path OUTPUT = Paths.get("experiments", "result-summary.csv");
    private static final String HEADER =
            "dataset_source,dataset_name,n,capacity,algorithm,value,optimum,gap_percent,time_ms,status";

    public static synchronized void write(
            String datasetSource,
            String datasetName,
            int n,
            int capacity,
            String algorithm,
            String value,
            String optimum,
            String gapPercent,
            String timeMs,
            String status) throws IOException {

        Files.createDirectories(OUTPUT.getParent());
        boolean needsHeader = !Files.exists(OUTPUT) || Files.size(OUTPUT) == 0;
        BufferedWriter writer = Files.newBufferedWriter(
                OUTPUT,
                java.nio.charset.StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
        try {
            if (needsHeader) {
                writer.write(HEADER);
                writer.newLine();
            }
            writer.write(joinCsv(new String[] {
                    datasetSource, datasetName, String.valueOf(n), String.valueOf(capacity),
                    algorithm, value, optimum, gapPercent, timeMs, status
            }));
            writer.newLine();
        } finally {
            writer.close();
        }

        writeSingleResultFile(datasetSource, datasetName, n, capacity, algorithm,
                value, optimum, gapPercent, timeMs, status);
    }

    public static String value(int value) {
        return String.valueOf(value);
    }

    public static String empty() {
        return "";
    }

    public static String timeMs(long startNanos, long endNanos) {
        return String.format(java.util.Locale.US, "%.3f", (endNanos - startNanos) / 1e6);
    }

    public static String gapPercent(int value, int optimum) {
        if (optimum <= 0) {
            return "";
        }
        double gap = (double) (optimum - value) / optimum * 100.0;
        return String.format(java.util.Locale.US, "%.2f", gap);
    }

    private static String joinCsv(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean quote = value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0;
        if (!quote) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static void writeSingleResultFile(
            String datasetSource,
            String datasetName,
            int n,
            int capacity,
            String algorithm,
            String value,
            String optimum,
            String gapPercent,
            String timeMs,
            String status) throws IOException {
        String fileName = slug(algorithm) + "-knapsack01-" + slug(datasetSource)
                + "-" + slug(datasetName) + "-c" + capacity + ".txt";
        Path path = OUTPUT.getParent().resolve(fileName);

        BufferedWriter writer = Files.newBufferedWriter(
                path,
                java.nio.charset.StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        try {
            writer.write("dataset_source=" + datasetSource);
            writer.newLine();
            writer.write("dataset_name=" + datasetName);
            writer.newLine();
            writer.write("n=" + n);
            writer.newLine();
            writer.write("capacity=" + capacity);
            writer.newLine();
            writer.write("algorithm=" + algorithm);
            writer.newLine();
            writer.write("value=" + nullToEmpty(value));
            writer.newLine();
            writer.write("optimum=" + nullToEmpty(optimum));
            writer.newLine();
            writer.write("gap_percent=" + nullToEmpty(gapPercent));
            writer.newLine();
            writer.write("time_ms=" + nullToEmpty(timeMs));
            writer.newLine();
            writer.write("status=" + status);
            writer.newLine();
        } finally {
            writer.close();
        }
    }

    private static String slug(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }
        String lower = value.trim().toLowerCase(java.util.Locale.US);
        String slug = lower.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
        return slug.isEmpty() ? "unknown" : slug;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
