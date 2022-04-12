import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class Settings {
    public int padding = 50;

    public int size = 1;

    public boolean convertChunkPosToBlockPos = true;

    public int maxDataEntries = 2500;

    public boolean dotShrinking = true;

    public float maxAreaDensity = 5.0F;

    public boolean pixelAlpha = true;

    public Decoder.DrawType _drawType = Decoder.DrawType.PIXEL;

    public int lineThreshold = 200;

    public int upscaleMultiplier = 1;

    public boolean fancyLines = false;

    public boolean hiddenLines = false;

    public boolean antialiasing = true;

    private Logger logger;

    public Settings(Logger log) {
        logger = log;

        logger.Log("Initializing settings subsystem", Logger.MessageType.INFO);

        try {
            if (!(new File("config.txt")).exists()) {
                logger.Log("Config file does not exist, creating it", Logger.MessageType.WARNING);

                try {
                    padding = 50;
                    size = 10;
                    convertChunkPosToBlockPos = true;
                    maxDataEntries = 0;
                    dotShrinking = true;
                    maxAreaDensity = 5.0F;
                    pixelAlpha = true;
                    _drawType = Decoder.DrawType.PIXEL;
                    lineThreshold = 200;
                    upscaleMultiplier = 1;
                    fancyLines = false;
                    hiddenLines = false;
                    antialiasing = true;

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
            writer.println("size: " + size + " //Change the default position marker dot radius, 0 disables using dots to instead use a pixel to mark a position");
            writer.println("convertChunkPositions: " + convertChunkPosToBlockPos + " //Convert logged chunk positions into block positions, this is done by multiplying the chunk position by 16");
            writer.println("maxEntries: " + maxDataEntries + " //The limit to the amount of data entries to compile into the final image or gif, useful when wanting a less-detailed, but quick output or when with low memory. Set to 0 to disable");
            writer.println("dotShrinking: " + dotShrinking + " //Whether or not to shrink logged dots if they are covering another logged dot. Disabled if individualPoints is false");
            writer.println("maxAreaDensity: " + maxAreaDensity + " //The maximum area density that can be used to grow the dot radius");
            writer.println("pixelAlpha: " + pixelAlpha + " //Whether or not pixels should be slightly transparent so they don't overlap");
            writer.println("drawType: " + drawTypeToInt(_drawType) + " //The way to represent the positions. 0 = Pixel, 1 = Dot, 2 = Lines");
            writer.println("lineThreshold: " + lineThreshold + " //The maximum distance a player can move until its position change doesn't draw a line. This is to fix issues where massive lines are drawn across the map when players nether travel or die.");
            writer.println("upscale: " + upscaleMultiplier + " //The scale multiplier. The higher the upscaling, the higher the final resolution. HIGHER VALUES DRASTICALLY INCREASE FILE SIZE AND PROCESSING TIME!");
            writer.println("fancyLines: " + fancyLines + " //Whether or not to show arrows at data points when drawing using lines");
            writer.println("antialiasing: " + antialiasing + " //Whether or not to use antialiasing when rendering. Used to smooth out the hard, pixelated edges");
            writer.print("hiddenLines: " + hiddenLines + " //Whether or not to show lines that were hidden for being above the threshold");
            writer.close();

            logger.Log("Successfully saved and wrote settings to config file", Logger.MessageType.INFO);
        } catch (Exception e) {
            logger.Log("Error saving and writing settings to config file:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
        }
    }

    private int drawTypeToInt(Decoder.DrawType dt) {
        if (dt == Decoder.DrawType.PIXEL)
            return 0;
        else if (dt == Decoder.DrawType.DOT)
            return 1;
        else if (dt == Decoder.DrawType.LINE)
            return 2;

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

        logger.Log("Settings: " + args.size(), Logger.MessageType.INFO);
        if (args.size() != 0)
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i).contains("size: ")) {
                    String str = args.get(i).replace("size: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    size = Integer.parseInt(str);
                } else if (args.get(i).contains("convertChunkPositions")) {
                    String str = args.get(i).replace("convertChunkPositions: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    convertChunkPosToBlockPos = Boolean.parseBoolean(str);
                } else if (args.get(i).contains("maxEntries")) {
                    String str = args.get(i).replace("maxEntries: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    maxDataEntries = Integer.parseInt(str);
                } else if (args.get(i).contains("dotShrinking")) {
                    String str = args.get(i).replace("dotShrinking: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    dotShrinking = Boolean.parseBoolean(str);
                } else if (args.get(i).contains("maxAreaDensity")) {
                    String str = args.get(i).replace("maxAreaDensity: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    maxAreaDensity = Float.parseFloat(str);
                } else if (args.get(i).contains("pixelAlpha")) {
                    String str = args.get(i).replace("pixelAlpha: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    pixelAlpha = Boolean.parseBoolean(str);
                } else if (args.get(i).contains("drawType")) {
                    String str = args.get(i).replace("drawType: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    int val = Integer.parseInt(str);
                    if (val == 0) {
                        _drawType = Decoder.DrawType.PIXEL;
                    } else if (val == 1) {
                        _drawType = Decoder.DrawType.DOT;
                    } else if (val == 2) {
                        _drawType = Decoder.DrawType.LINE;
                    }
                    logger.Log(_drawType, Logger.MessageType.INFO);
                } else if (args.get(i).contains("lineThreshold")) {
                    String str = args.get(i).replace("lineThreshold: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    lineThreshold = Integer.parseInt(str);
                } else if (args.get(i).contains("fancyLines")) {
                    String str = args.get(i).replace("fancyLines: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    fancyLines = Boolean.parseBoolean(str);
                } else if (args.get(i).contains("hiddenLines")) {
                    String str = args.get(i).replace("hiddenLines: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    hiddenLines = Boolean.parseBoolean(str);
                } else if (args.get(i).contains("antialiasing")) {
                    String str = args.get(i).replace("antialiasing: ", "");
                    str = str.substring(0, str.indexOf(" //"));
                    logger.Log(str, Logger.MessageType.INFO);
                    antialiasing = Boolean.parseBoolean(str);
                }
            }
    }
}