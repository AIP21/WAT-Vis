package src;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Logger {
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

        for (File file : logFiles) {
            if (file.getName().contains(name)) {
                count++;
            }
        }

        name += (count != 0 ? " " + count : "") + ".log.txt";

        logFile = new File("logs/" + name);
        LogFirst("Player Tracker Decoder App v" + version + " - LOG FILE", MessageType.INFO);
        Log("Initializing logger", MessageType.INFO);

        if (created) Log("Log file directory didn't exist, so it was created", MessageType.WARNING);

        Log("Logger successfully initialized", MessageType.INFO);
    }

    public void LogFirst(Object message, MessageType type) {
        String toLog = ("   [" + type.toString() + "] <" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss.SSS")) + "> " + message + "\n   ");

        if (type == MessageType.ERROR) {
            System.err.print("\u001B[0m" + toLog.replace(", ", "\n   "));
        } else if (type == MessageType.WARNING) {
            System.out.print("\u001B[33m" + toLog);
        } else {
            System.out.print("\u001B[0m" + toLog);
        }

        try {
            FileWriter writer = new FileWriter(logFile, true);
            writer.append(toLog);
            writer.close();
        } catch (Exception e) {
            Log("Error logging message:\n   " + Arrays.toString(e.getStackTrace()), MessageType.ERROR);
        }
    }

    public void Log(Object message, MessageType type) {
//        EventQueue.invokeLater(()->{
            String toLog = ("[" + type.toString() + "] <" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd; HH:mm:ss.SSS")) + "> " + message + "\n   ");

            if (type == MessageType.ERROR) {
                System.err.print("\u001B[0m" + toLog.replace(", ", "\n   "));
            } else if (type == MessageType.WARNING) {
                System.out.print("\u001B[33m" + toLog);
            } else {
                System.out.print("\u001B[0m" + toLog);
            }

            try {
                FileWriter writer = new FileWriter(logFile, true);
                writer.append(toLog);
                writer.close();
            } catch (Exception e) {
                Log("Error logging message:\n   " + Arrays.toString(e.getStackTrace()), MessageType.ERROR);
            }
//        });
    }

    public enum MessageType {
        INFO,
        WARNING,
        ERROR,
    }
}