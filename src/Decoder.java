package src;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Decoder {
    public PlayerTrackerDecoder main;
    private final Settings settings;
    private final Logger logger;

    private final Random rand = new Random();

    private final ArrayList<File> inputFiles = new ArrayList<>();
    private final ArrayList<Color> generatedColors = new ArrayList<>();

    public ArrayList<LogEntry> logEntries = new ArrayList<>();
    public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
    public Map<String, Boolean> playerNameEnabledMap = new LinkedHashMap<>();
    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();
    public Map<String, Integer> playerCountMap = new LinkedHashMap<>();
    public ArrayList<LocalDateTime> logDates = new ArrayList<>();

    public int minX;
    public int maxX;
    public int minY;
    public int maxY;
    public int xRange;
    public int yRange;

    public String dataWorld;
    public String dataDate;

    public File[] files;

    private final boolean maxCheck;

    public Decoder(Settings set, Logger log) {
        logger = log;

        logger.info("Initializing decoder subsystem", 1);

        settings = set;

        maxCheck = settings.maxDataEntries != 0;

        logger.info("Successfully initialized decoder subsystem", 1);
    }

    public void decode() {
        final long nowMs = System.currentTimeMillis();

        if (files == null || files.length == 0) {
            logger.error("Error parsing log files and decoding data. They must be in the folder called \"inputs\" in the run directory");
            return;
        }

        logger.info("Decoding data", 1);

        try {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    inputFiles.add(file);
//                        String str = file.getName().substring(file.getName().lastIndexOf('-') - 7, file.getName().lastIndexOf('-') + 3);
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching text log files:\n   " + Arrays.toString(e.getStackTrace()));
        }

        inputFiles.sort(new Comparator<>() {
            @Override
            public int compare(File o1, File o2) {
                LocalDate d1 = extractDate(o1.getName());
                LocalDate d2 = extractDate(o2.getName());
                return d1.compareTo(d2);
            }

            private LocalDate extractDate(String name) {
                return LocalDate.parse(name.substring(name.lastIndexOf('-') - 7, name.lastIndexOf('-') + 3), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        });

        for (File inputFile : inputFiles) {
            logger.info("Decoding input file: " + inputFile.getName(), 0);

            try {
                BufferedReader br = new BufferedReader(new FileReader(inputFile));
                String line;
                while ((!maxCheck || logEntries.size() <= settings.maxDataEntries) && (line = br.readLine()) != null) {
                    if (line.charAt(line.length() - 1) != ';')
                        line = line + ";";

                    String[] items = line.split(";");

                    LocalDateTime date;
                    if (items[0].length() < 8) {
                        date = LocalDateTime.parse(inputFile.getName().substring(inputFile.getName().lastIndexOf('-') - 7, inputFile.getName().lastIndexOf('-') + 3) + ":" + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm"));
                    } else {
                        date = LocalDateTime.parse(inputFile.getName().substring(inputFile.getName().lastIndexOf('-') - 7, inputFile.getName().lastIndexOf('-') + 3) + ":" + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
                    }

                    Vector3 position = items[2].contains("(") ? Vector3.parseVector3(items[2]) : Vector3.parseVector3FromChunk(items[2], settings.convertChunkPosToBlockPos);

                    logEntries.add(new LogEntry(date, items[1], position));
                    logDates.add(date);

                    String player = items[1];

                    playerNameColorMap.putIfAbsent(player, randomColor(0));

                    playerNameEnabledMap.putIfAbsent(player, true);

                    playerLastPosMap.putIfAbsent(player, logEntries.get(logEntries.size() - 1).position);

                    if (!playerCountMap.containsKey(player))
                        playerCountMap.put(player, 1);
                    else {
                        playerCountMap.put(player, playerCountMap.get(player) + 1);
                    }

                    if (logEntries.get(logEntries.size() - 1).position.x < minX)
                        minX = logEntries.get(logEntries.size() - 1).position.x;

                    if (logEntries.get(logEntries.size() - 1).position.x > maxX)
                        maxX = logEntries.get(logEntries.size() - 1).position.x;

                    if (logEntries.get(logEntries.size() - 1).position.z < minY)
                        minY = logEntries.get(logEntries.size() - 1).position.z;

                    if (logEntries.get(logEntries.size() - 1).position.z > maxY)
                        maxY = logEntries.get(logEntries.size() - 1).position.z;
                }
                br.close();
            } catch (IOException e) {
                logger.error("Error reading input file:\n   " + Arrays.toString(e.getStackTrace()));
            }

            if (maxCheck && logEntries.size() > settings.maxDataEntries) {
                logger.warn("Max configured data entries reached, decoding aborted");
                break;
            }
        }

        logger.info("dates: " + logDates.size(), 1);

        dataWorld = "";
        String[] split = inputFiles.get(0).getName().split("-");
        dataDate = inputFiles.get(0).getName().substring(inputFiles.get(0).getName().indexOf("-log-") + 5, inputFiles.get(0).getName().length() - 4) + " to " + inputFiles.get(inputFiles.size() - 1).getName().substring(inputFiles.get(inputFiles.size() - 1).getName().indexOf("-log-") + 5, inputFiles.get(inputFiles.size() - 1).getName().length() - 4);
        if (split.length > 1) {
            dataWorld = split[1];
        }

        xRange = maxX - minX;
        yRange = maxY - minY;
        logger.info("maxX: " + maxX + " minX: " + minX + "; maxY: " + maxY + " minY: " + minY, 0);
        logger.info("xRange: " + xRange + " yRange: " + yRange, 0);

        final long durMs = System.currentTimeMillis() - nowMs;

        logger.info("Successfully decoded data. Loaded: " + logEntries.size() + " entries. Took " + durMs + "ms.", 1);
    }

    private Color randomColor(int iter) {
        Color col = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
        if (settings.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
            col = col.darker();
        } else {
            col = col.brighter();
            col = col.brighter(); // Intentionally done twice
        }
        if (containsSimilarColor(generatedColors, col) && iter < 100)
            return randomColor(iter + 1);
        generatedColors.add(col);
        return col;
    }

    private boolean containsSimilarColor(ArrayList<Color> listToCheck, Color colorToCheck) {
        for (Color col : listToCheck) {
            if (isSimilarColor(col, colorToCheck))
                return true;
        }
        return false;
    }

    private boolean isSimilarColor(Color a, Color b) {
        return (Utils.approximately(a.getRed(), b.getRed(), 20.0F) &&
                Utils.approximately(a.getGreen(), b.getGreen(), 20.0F) &&
                Utils.approximately(a.getBlue(), b.getBlue(), 20.0F));
    }

    public enum DrawType {
        Pixel, Dot, Line, Heat
    }
}
