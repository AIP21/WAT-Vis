package src.main.util;

import src.main.PlayerTrackerDecoder;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class Logger {
    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PlayerTrackerDecoder.class.getName());

    public static void registerLogger() {
        try {
            FileHandler handler = new FileHandler(PlayerTrackerDecoder.DIR_LOGS + File.separatorChar + "error%u%g.log", 1000000, 10);
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(handler);

            handler.setFormatter(new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format("[%1$tF %1$tT] [%2$-7s] %3$s %n", new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
                }
            });

            LOGGER.info("Initialing logger");

            LOGGER.info(String.format("Player Tracker Decoder App v%s - %s", PlayerTrackerDecoder.VERSION, new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z").format(new Date(System.currentTimeMillis()))));

            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

            Map<String, String> systemProperties = runtimeBean.getSystemProperties();
            Set<String> keys = systemProperties.keySet();
            String username = "";
            if (keys.contains("user.name")) {
                username = systemProperties.get("user.name");
            }

            for (String key : keys) {
                LOGGER.info(String.format("[%s] = %s.", key, systemProperties.get(key).replace(username, "XANONX").replaceAll("Users\\\\.*?\\\\", "Users\\\\ANONYM\\\\").replace("\r\n", "CRLF").replace("\n", "LF")));
            }
        } catch (IOException e) {
            LOGGER.severe(e.toString());
            e.printStackTrace();
        }

        LOGGER.info("Logger successfully initialized");
    }
}