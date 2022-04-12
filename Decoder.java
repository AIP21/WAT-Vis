import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Decoder {
    private final Random rand = new Random();

    private final Runtime runtime = Runtime.getRuntime();

    private final NumberFormat format = NumberFormat.getInstance();

    private final ArrayList<File> inputFiles = new ArrayList<>();

    private final ArrayList<Color> generatedColors = new ArrayList<>();

    private final Settings settings;

    public ArrayList<LogEntry> logEntries = new ArrayList<>();

    public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();

    public Map<String, Boolean> playerNameEnabledMap = new LinkedHashMap<>();

    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();

    public ArrayList<LocalDateTime> logDates = new ArrayList<>();

    public int minX;

    public int maxX;

    public int minY;

    public int maxY;

    public int xRange;

    public int yRange;

    private Logger logger;

    public LocalDateTime firstDate = null;
    public LocalDateTime lastDate = null;

    public String dataWorld;
    public String dataDate;

    public Decoder(Settings set, Logger log) {
        logger = log;

        logger.Log("Initializing decoder subsystem", Logger.MessageType.INFO);

        settings = set;

        logger.Log("Successfully initialized decoder subsystem", Logger.MessageType.INFO);
    }

    public void Decode(File[] files) {
        logger.Log("Decoding data", Logger.MessageType.INFO);

        if (files != null && files.length != 0) {
            try {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        inputFiles.add(file);
                        String str = file.getName().substring(file.getName().lastIndexOf('-') - 7, file.getName().lastIndexOf('-') + 3);

                        logger.Log("Fetched and indexed input file", Logger.MessageType.INFO);
                    }
                }
            } catch (Exception e) {
                logger.Log("Error fetching text log files:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
            }

            inputFiles.sort(Comparator.comparing(File::getName));

            for (File inputFile : inputFiles) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(inputFile));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.charAt(line.length() - 1) != ';')
                            line = line + ";";

                        String[] items = line.split(";");

                        if (items[0].length() < 8) {
                            LocalDateTime date = LocalDateTime.parse(inputFile.getName().substring(inputFile.getName().lastIndexOf('-') - 7, inputFile.getName().lastIndexOf('-') + 3) + ":" + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm"));
                            logEntries.add(new LogEntry(date, items[1], items[2].contains("(") ? Vector3.parseVector3(items[2], settings.upscaleMultiplier) : Vector3.parseVector3FromChunk(items[2], settings.convertChunkPosToBlockPos, settings.upscaleMultiplier), items[2].contains("[")));
                            logDates.add(date);
                        } else {
                            LocalDateTime date = LocalDateTime.parse(inputFile.getName().substring(inputFile.getName().lastIndexOf('-') - 7, inputFile.getName().lastIndexOf('-') + 3) + ":" + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
                            logEntries.add(new LogEntry(date, items[1], items[2].contains("(") ? Vector3.parseVector3(items[2], settings.upscaleMultiplier) : Vector3.parseVector3FromChunk(items[2], settings.convertChunkPosToBlockPos, settings.upscaleMultiplier), items[2].contains("[")));
                            logDates.add(date);
                        }

                        if (!playerNameColorMap.containsKey(items[1]))
                            playerNameColorMap.put(items[1], randomColor());

                        if (!playerNameEnabledMap.containsKey(items[1]))
                            playerNameEnabledMap.put(items[1], true);

                        if (!playerLastPosMap.containsKey(items[1]))
                            playerLastPosMap.put(items[1], ((LogEntry) logEntries.get(logEntries.size() - 1)).position);

                        if (((LogEntry) logEntries.get(logEntries.size() - 1)).position.x < minX)
                            minX = ((LogEntry) logEntries.get(logEntries.size() - 1)).position.x;

                        if (((LogEntry) logEntries.get(logEntries.size() - 1)).position.x > maxX)
                            maxX = ((LogEntry) logEntries.get(logEntries.size() - 1)).position.x;

                        if (((LogEntry) logEntries.get(logEntries.size() - 1)).position.z < minY)
                            minY = ((LogEntry) logEntries.get(logEntries.size() - 1)).position.z;

                        if (((LogEntry) logEntries.get(logEntries.size() - 1)).position.z > maxY)
                            maxY = ((LogEntry) logEntries.get(logEntries.size() - 1)).position.z;
                    }
                    br.close();
                } catch (IOException e) {
                    logger.Log("Error reading input file:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
                }
            }

            dataWorld = "";
            String[] split = inputFiles.get(0).getName().split("-");
            dataDate = inputFiles.get(0).getName().substring(inputFiles.get(0).getName().indexOf("-log-") + 5, inputFiles.get(0).getName().length() - 4) + " to " + inputFiles.get(inputFiles.size() - 1).getName().substring(inputFiles.get(inputFiles.size() - 1).getName().indexOf("-log-") + 5, inputFiles.get(inputFiles.size() - 1).getName().length() - 4);
            if (split.length > 1) {
                dataWorld = split[1];
            }

            xRange = maxX - minX;
            yRange = maxY - minY;
            logger.Log("maxX: " + maxX + " minX: " + minX + "; maxY: " + maxY + " minY: " + minY, Logger.MessageType.INFO);
            logger.Log("xRange: " + xRange + " yRange: " + yRange, Logger.MessageType.INFO);

            logger.Log("Successfully decoded data", Logger.MessageType.INFO);
        } else {
            logger.Log("Error parsing log files and decoding data. They must be in the folder called \"inputs\" in the run directory", Logger.MessageType.ERROR);
        }
    }

    private Color randomColor() {
        Color col = (new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())).darker();
        col = col.darker();
        if (containsSimilarColor(generatedColors, col))
            return randomColor();
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
        return (approximately(a.getRed(), b.getRed(), 20.0F) &&
                approximately(a.getGreen(), b.getGreen(), 20.0F) &&
                approximately(a.getBlue(), b.getBlue(), 20.0F));
    }

    private boolean approximately(int a, int b, float threshold) {
        return ((a - b) < threshold);
    }

    public enum DrawType {
        PIXEL, DOT, LINE;
    }
}
