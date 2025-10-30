package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_ROOT = Paths.get("automated_library_storage_system", "logs");

    private static final Pattern RAW_PATTERN = Pattern.compile(
            "^(TASKS|RESOURCES|STORAGE|SYSTEM|COMMON):([^|]*)\\|([A-Z]+)\\|(.+)$");

    public enum Scope { TASKS, RESOURCES, STORAGE, SYSTEM, COMMON }

    public static void logSystem(String level, String message) {
        write(Scope.SYSTEM, "all", level, message);
    }

    public static void logTasks(String level, String message) {
        write(Scope.TASKS, "all", level, message);
    }

    public static void logResources(String name, String level, String message) {
        write(Scope.RESOURCES, name, level, message);
    }

    public static void logStorage(String name, String level, String message) {
        write(Scope.STORAGE, name, level, message);
    }

    public static void logCommon(String level, String message) {
        write(Scope.COMMON, "all", level, message);
    }

    // Backward-compat convenience: map to RESOURCES
    public static void logRobot(String robotId, String level, String message) {
        write(Scope.RESOURCES, robotId, level, message);
    }

    // Backward-compat convenience: map to RESOURCES
    public static void logCharging(String stationName, String level, String message) {
        write(Scope.RESOURCES, stationName, level, message);
    }

    public static void logFromRaw(String raw) {
        Matcher m = RAW_PATTERN.matcher(raw);
        if (!m.matches()) {
            logSystem("ERROR", "RAW_FORMAT_INVALID: " + raw);
            return;
        }
        Scope cat = Scope.valueOf(m.group(1));
        String name = m.group(2).isEmpty() ? "all" : m.group(2);
        String level = m.group(3);
        String msg = m.group(4).trim();
        write(cat, name, level, msg);
    }

    public static List<String> openLogByDate(Scope scope, LocalDate date) throws IOException {
        Path file = resolveLogFile(scope, "all", date);
        if (Files.exists(file)) {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        }
        return List.of();
    }

    private static void write(Scope scope, String name, String level, String message) {
        ensureLogRoot();
        LocalDateTime now = LocalDateTime.now();
        String line = String.format("[%s] %s.%s(%s): %s", now.format(TS_FMT), scope, level, name, message);
        Path file = resolveLogFile(scope, name, now.toLocalDate());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file.toFile(), true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Byte stream simulation: also write raw bytes to a lightweight daily bin
        Path bin = LOG_ROOT.resolve("byte-stream-" + now.format(DATE_FMT) + ".bin");
        try (FileOutputStream fos = new FileOutputStream(bin.toFile(), true)) {
            byte[] payload = (line + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            fos.write(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Path resolveLogFile(Scope scope, String name, LocalDate date) {
        String baseName;
        switch (scope) {
            case SYSTEM:
                baseName = "system-" + date.format(DATE_FMT) + ".log";
                break;
            case TASKS:
                baseName = "tasks-" + date.format(DATE_FMT) + ".log";
                break;
            case RESOURCES:
                baseName = "resources-" + date.format(DATE_FMT) + ".log";
                break;
            case STORAGE:
                baseName = "storage-" + date.format(DATE_FMT) + ".log";
                break;
            case COMMON:
                baseName = "common-" + date.format(DATE_FMT) + ".log";
                break;
            default:
                baseName = "log-" + date.format(DATE_FMT) + ".log";
        }
        Path p = LOG_ROOT.resolve(baseName);
        ensureParent(p);
        return p;
    }

    private static void ensureLogRoot() {
        if (!Files.exists(LOG_ROOT)) {
            try {
                Files.createDirectories(LOG_ROOT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void ensureParent(Path p) {
        File parent = p.toFile().getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private static String sanitize(String s) {
        return s == null ? "all" : s.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}


