package src.main.util;

import src.main.PlayerTrackerDecoder;
import src.main.Settings;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Logger {
    public Settings settings;

    private File logFile;

    public Logger(String version) {

        boolean created = false;
        if (!new File("logs").exists()) {
            new File("logs").mkdir();
            created = true;
        }

        String name = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        File[] logFiles = new File("logs/").listFiles();
        int count = 0;

        if (logFiles != null) {
            for (File file : logFiles) {
                if (file.getName().contains(name)) {
                    count++;
                }
            }
        }

        name += (count != 0 ? " " + count : "") + ".log.txt";

        logFile = new File("logs/" + name);
        logFirst("Player Tracker Decoder App v" + version + " - LOG FILE");
        info("Initializing logger", 0);
        if (PlayerTrackerDecoder.debugMode) {
            warn("DEBUG MODE IS ON, PERFORMANCE MAY BE LOWER");
        }

        if (created) warn("Log file directory didn't exist, so it was created");

        info("Logger successfully initialized", 1);
    }

    private void logFirst(Object message) {
        String toLog = ("   [INFO.1] <" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss.SSS")) + "> " + message + "\n   ");

        System.out.print("\u001B[0m" + toLog);

        try {
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(toLog);
            writer.close();
        } catch (Exception e) {
            error("Error logging message:\n   " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void info(Object message, int level) {
        EventQueue.invokeLater(() -> {
            String toLog = ("[INFO." + level + "] <" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss.SSS")) + "> " + message + "\n   ");

            if (PlayerTrackerDecoder.debugMode || level > 0) {
                System.out.print("\u001B[0m" + toLog);
            }

            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.append(toLog);
                writer.close();
            } catch (Exception e) {
                error("Error logging message:\n   " + Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void warn(Object message) {
        EventQueue.invokeLater(() -> {
            String toLog = ("[WARNING] <" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss.SSS")) + "> " + message + "\n   ");

            System.out.print("\u001B[33m" + toLog);

            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.append(toLog);
                writer.close();
            } catch (Exception e) {
                error("Error logging message:\n   " + Arrays.toString(e.getStackTrace()));
            }
        });
    }

    public void error(Object message) {
        EventQueue.invokeLater(() -> {
            String toLog = ("[ERROR] <" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss.SSS")) + "> " + message + "\n   ");

            System.err.print("\u001B[0m" + toLog.replace(", ", "\n   "));

            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.append(toLog);
                writer.close();
            } catch (Exception e) {
                error("Error logging message:\n   " + Arrays.toString(e.getStackTrace()));
            }
        });
    }
}