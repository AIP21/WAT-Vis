package src;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Settings {
    public float size = 1.0f;
    public boolean convertChunkPosToBlockPos = true;
    public int maxDataEntries = 0;
    public Decoder.DrawType _drawType = Decoder.DrawType.Pixel;
    public PlayerTrackerDecoder.HeatDrawType _heatDrawType = PlayerTrackerDecoder.HeatDrawType.Change_Size;
    public int heatMapThreshold = 0;
    public int lineThreshold = 200;
    public boolean fancyLines = false;
    public boolean hiddenLines = false;
    public boolean antialiasing = true;
    public boolean terminusPoints = true;
    public boolean ageFade = false;
    public int ageFadeThreshold = 0;

    private final int settingCount = 13;

    private final Logger logger;

    public Settings(Logger log) {
        logger = log;

        logger.Log("Initializing settings subsystem", Logger.MessageType.INFO);

        try {
            if (!(new File("config.txt")).exists()) {
                logger.Log("Config file does not exist, creating it", Logger.MessageType.WARNING);

                try {
                    size = 1.0f;
                    convertChunkPosToBlockPos = true;
                    maxDataEntries = 0;
                    _drawType = Decoder.DrawType.Pixel;
                    lineThreshold = 200;
                    fancyLines = false;
                    hiddenLines = false;
                    antialiasing = true;
                    terminusPoints = true;
                    ageFade = false;
                    ageFadeThreshold = 0;
                    _heatDrawType = PlayerTrackerDecoder.HeatDrawType.Change_Size;
                    heatMapThreshold = 0;

                    new File("config.txt").createNewFile();

                    logger.Log("Successfully created config file", Logger.MessageType.INFO);

                    SaveSettings();

                    logger.Log("Successfully saved and wrote default settings to config file", Logger.MessageType.INFO);
                } catch (Exception e) {
                    logger.Log("Error creating/saving/writing to config file:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
                }
            }

            logger.Log("Fetching and parsing settings from config file", Logger.MessageType.INFO);

            getFromFile(new File("config.txt"));

            logger.Log("Successfully fetched and parsed settings from config file", Logger.MessageType.INFO);
        } catch (IOException ioe) {
            logger.Log("Error fetching and parsing settings from config file:\n   " + Arrays.toString(ioe.getStackTrace()), Logger.MessageType.ERROR);
        }

        logger.Log("Successfully initialized settings subsystem", Logger.MessageType.INFO);
    }

    public void SaveSettings() {
        logger.Log("Saving and writing settings to config file", Logger.MessageType.INFO);

        try {
            PrintWriter writer = new PrintWriter("config.txt", StandardCharsets.UTF_8);
            writer.println("/// Player Tracker Decoder v" + PlayerTrackerDecoder.version + " - CONFIG \\\\\\");
            writer.println("/// Delete this config file to reset values to their default settings \\\\\\\n");
            writer.println("size: " + size + " // Change the position marker or line size");
            writer.println("convertChunkPositions: " + convertChunkPosToBlockPos + " // Convert logged chunk positions into block positions, this is done by multiplying the chunk position by 16");
            writer.println("maxEntries: " + maxDataEntries + " // The limit to the amount of data entries to compile into the final image or gif, useful when wanting a less-detailed, but quick output or when with low memory. Set to 0 to disable");
            writer.println("drawType: " + drawTypeToInt(_drawType) + " // The way to represent the positions. 0 = Pixel, 1 = Dot, 2 = Lines, 3 = Heatmap");
            writer.println("lineThreshold: " + lineThreshold + " // The maximum distance a player can move until its position change doesn't draw a line. This is to fix issues where massive lines are drawn across the map when players nether travel or die.");
            writer.println("fancyLines: " + fancyLines + " // Show arrows at data points when drawing using lines");
            writer.println("antialiasing: " + antialiasing + " // Use antialiasing when rendering. Used to smooth out the hard, pixelated edges");
            writer.println("hiddenLines: " + hiddenLines + " // Show lines that were hidden for being above the threshold");
            writer.println("terminusPoints: " + terminusPoints + " // Show dots at the start and end of lines");
            writer.println("ageFade: " + ageFade + " // Fade out older log markers, showing the age of the marker");
            writer.println("ageFadeThreshold: " + ageFadeThreshold + " // How much to fade out older log markers. If 0, then it uses the max amount of log markers");
            writer.println("heatDrawType: " + heatDrawTypeToInt(_heatDrawType) + " // The way to represent the heatmap. 0 = Change size, 1 = Change color");
            writer.println("heatMapThreshold: " + heatMapThreshold + " // How much to change colors on the heatmap");

            writer.close();

            logger.Log("Successfully saved and wrote settings to config file", Logger.MessageType.INFO);
        } catch (Exception e) {
            logger.Log("Error saving and writing settings to config file:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
        }
    }

    private int drawTypeToInt(Decoder.DrawType dt) {
        if (dt == Decoder.DrawType.Pixel)
            return 0;
        else if (dt == Decoder.DrawType.Dot)
            return 1;
        else if (dt == Decoder.DrawType.Line)
            return 2;
        else if (dt == Decoder.DrawType.Heat)
            return 3;

        return -1;
    }

    private int heatDrawTypeToInt(PlayerTrackerDecoder.HeatDrawType hdt) {
        if (hdt == PlayerTrackerDecoder.HeatDrawType.Change_Size)
            return 0;
        else if (hdt == PlayerTrackerDecoder.HeatDrawType.Change_Color)
            return 1;

        return -1;
    }

    private void getFromFile(File inputFile) throws IOException {
        ArrayList<String> args = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        try {
            String line;
            while ((line = br.readLine()) != null)
                args.add(line);
            br.close();
        } catch (Throwable throwable) {
            try {
                br.close();
            } catch (Throwable throwable1) {
                throwable.addSuppressed(throwable1);
            }
            throw throwable;
        }

        int count = 0;
        if (args.size() != 0)
            for (String arg : args) {
                if (arg.contains("size: ")) {
                    String str = arg.replace("size: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    size = Float.parseFloat(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("convertChunkPositions: ")) {
                    String str = arg.replace("convertChunkPositions: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    convertChunkPosToBlockPos = Boolean.parseBoolean(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("maxEntries: ")) {
                    String str = arg.replace("maxEntries: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    maxDataEntries = Integer.parseInt(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("drawType: ")) {
                    String str = arg.replace("drawType: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    int val = Integer.parseInt(str);
                    if (val == 0) {
                        _drawType = Decoder.DrawType.Pixel;
                    } else if (val == 1) {
                        _drawType = Decoder.DrawType.Dot;
                    } else if (val == 2) {
                        _drawType = Decoder.DrawType.Line;
                    } else if (val == 3) {
                        _drawType = Decoder.DrawType.Heat;
                    }
                    count++;
                    logger.Log(_drawType + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("lineThreshold: ")) {
                    String str = arg.replace("lineThreshold: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    lineThreshold = Integer.parseInt(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("fancyLines: ")) {
                    String str = arg.replace("fancyLines: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    fancyLines = Boolean.parseBoolean(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("hiddenLines: ")) {
                    String str = arg.replace("hiddenLines: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    hiddenLines = Boolean.parseBoolean(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("antialiasing: ")) {
                    String str = arg.replace("antialiasing: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    antialiasing = Boolean.parseBoolean(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("terminusPoints: ")) {
                    String str = arg.replace("terminusPoints: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    terminusPoints = Boolean.parseBoolean(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("ageFade: ")) {
                    String str = arg.replace("ageFade: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    ageFade = Boolean.parseBoolean(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("ageFadeThreshold: ")) {
                    String str = arg.replace("ageFadeThreshold: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    ageFadeThreshold = Integer.parseInt(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("heatDrawType: ")) {
                    String str = arg.replace("heatDrawType: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    int val = Integer.parseInt(str);
                    if (val == 0) {
                        _heatDrawType = PlayerTrackerDecoder.HeatDrawType.Change_Size;
                    } else if (val == 1) {
                        _heatDrawType = PlayerTrackerDecoder.HeatDrawType.Change_Color;
                    }
                    count++;
                    logger.Log(_heatDrawType + ", " + count, Logger.MessageType.INFO);
                } else if (arg.contains("heatMapThreshold: ")) {
                    String str = arg.replace("heatMapThreshold: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    heatMapThreshold = Integer.parseInt(str);
                    count++;
                    logger.Log(str + ", " + count, Logger.MessageType.INFO);
                }
            }

        logger.Log("Settings count: " + count, Logger.MessageType.INFO);

        if (count != settingCount) {
            logger.Log("Incomplete or old config file, updating the config file. Counted: " + count + " settings, expected: " + settingCount, Logger.MessageType.WARNING);
            SaveSettings();
        }
    }
}