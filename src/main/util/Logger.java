package util;

import main.PlayerTrackerDecoder;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

public class Logger {
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PlayerTrackerDecoder.class.getName());

    public static void registerLogger() {
        try {
            FileHandler handler = new FileHandler(PlayerTrackerDecoder.DIR_LOGS + File.separatorChar + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " %u%g.log", 1000000, 10);
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(false);

//            LOGGER.addHandler(new StreamHandler(System.out, new SimpleFormatter()));

            handler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format("[%1$tF %1$tT] [%2$-7s] %3$s %n", new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
                }
            });

            Logger.info("Initialing logger");

            Logger.info(String.format("Player Tracker Decoder App v%s ; %s", PlayerTrackerDecoder.VERSION, new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z").format(new Date(System.currentTimeMillis()))));

            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

            Map<String, String> systemProperties = runtimeBean.getSystemProperties();
            Set<String> keys = systemProperties.keySet();
            String username = "";
            if (keys.contains("user.name")) {
                username = systemProperties.get("user.name");
            }

            Logger.info("\n********BEGIN DEVICE INFO********\n");

            for (String key : keys) {
                Logger.info(String.format("[%s] = %s.", key, systemProperties.get(key).replace(username, "XANONX").replaceAll("Users\\\\.*?\\\\", "Users\\\\ANONYM\\\\").replace("\r\n", "CRLF").replace("\n", "LF")));
            }

            Logger.info("\n*********END DEVICE INFO*********\n");
        } catch (IOException e) {
            Logger.err("Error:\n F"+e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }

        Logger.info("Logger successfully initialized");
    }

    public static void info(Object message) {
        System.out.println(message.toString());
        LOGGER.info(message.toString());
    }

    public static void warn(Object message) {
        System.out.println("\u001B[33m" + message.toString() + "\u001B[0m");
        LOGGER.warning(message.toString());
    }

    public static void err(Object message) {
        String msg = message.toString().replace(", ", "\n ");
        System.err.println(msg);
        LOGGER.severe(msg);
    }
}