package src.main.config;

import src.main.Decoder.DrawType;
import src.main.PlayerTrackerDecoder;
import src.main.PlayerTrackerDecoder.HeatDrawType;
import src.main.PlayerTrackerDecoder.UITheme;
import src.main.util.Logger;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static src.main.util.Logger.LOGGER;

public class Settings {
    public UITheme uiTheme = UITheme.Light;
    public float size = 1.0f;
    public boolean convertChunkPosToBlockPos = true;
    public int maxDataEntries = 0;
    public DrawType _drawType = DrawType.Pixel;
    public HeatDrawType _heatDrawType = HeatDrawType.Size;
    public float heatMapStrength = 1.0f;
    public int lineThreshold = 200;
    public boolean fancyLines = false;
    public boolean hiddenLines = false;
    public boolean fancyRendering = true;
    public boolean terminusPoints = true;
    public boolean ageFade = false;
    public float ageFadeStrength = 1.0f;
    public int mouseSensitivity = 100;
    public int fpsLimit = 60;

    public HashMap<RenderingHints.Key, Object> renderingHints = new HashMap<>();

    private final int settingCount = 16;


    public Settings() {
        Logger.info("Initializing settings subsystem");

        try {
            if (!(new File(PlayerTrackerDecoder.DIR_CONFIG + File.separatorChar + "config.txt")).exists()) {
                LOGGER.warning("Config file does not exist, creating it");

                try {
                    (new File(PlayerTrackerDecoder.DIR_CONFIG + File.separatorChar +"config.txt")).createNewFile();

                    Logger.info("Successfully created config file");
                    SaveSettings();
                    Logger.info("Successfully saved and wrote default settings to config file");
                } catch (Exception e) {
                    LOGGER.severe("Error creating/saving/writing to config file:\n   " + Arrays.toString(e.getStackTrace()));
                }
            }

            Logger.info("Fetching and parsing settings from config file");
            getFromFile(new File(PlayerTrackerDecoder.DIR_CONFIG + File.separatorChar +"config.txt"));
            Logger.info("Successfully fetched and parsed settings from config file");
        } catch (IOException e) {
            LOGGER.severe("Error fetching and parsing settings from config file:\n   " + Arrays.toString(e.getStackTrace()));
        }

        Logger.info("Successfully initialized settings subsystem");
    }

    public void SaveSettings() {
        Logger.info("Saving and writing settings to config file");

        try {
            PrintWriter writer = new PrintWriter(PlayerTrackerDecoder.DIR_CONFIG + File.separatorChar +"config.txt", StandardCharsets.UTF_8);
            writer.println("/// Player Tracker Decoder v" + PlayerTrackerDecoder.VERSION + " - CONFIG \\\\\\");
            writer.println("/// Delete this config file to reset values to their default settings \\\\\\\n");

            writer.println("theme: " + themeToInt(uiTheme) + " // Change the UI theme (Light = 0, Dark = 1)");
            writer.println("fpsLimit: " + fpsLimit + " // The framerate limit");
            writer.println("size: " + size + " // Change the position marker or line size");
            writer.println("convertChunkPositions: " + convertChunkPosToBlockPos + " // Convert logged chunk positions into block positions, this is done by multiplying the chunk position by 16");
            writer.println("maxEntries: " + maxDataEntries + " // The limit to the amount of data entries to compile into the final image or gif, useful when wanting a less-detailed, but quick output or when with low memory. Set to 0 to disable");
            writer.println("drawType: " + drawTypeToInt(_drawType) + " // The way to represent the positions. 0 = Pixel, 1 = Dot, 2 = Lines, 3 = Heatmap");
            writer.println("lineThreshold: " + lineThreshold + " // The maximum distance a player can move until its position change doesn't draw a line. This is to fix issues where massive lines are drawn across the map when players nether travel or die.");
            writer.println("fancyLines: " + fancyLines + " // Show arrows at data points when drawing using lines");
            writer.println("fancyRendering: " + fancyRendering + " // Improve visual fidelity at the cost of performance");
            writer.println("hiddenLines: " + hiddenLines + " // Show lines that were hidden for being above the threshold");
            writer.println("terminusPoints: " + terminusPoints + " // Show dots at the start and end of lines");
            writer.println("ageFade: " + ageFade + " // Fade out older log markers, showing the age of the marker");
            writer.println("ageFadeStrength: " + ageFadeStrength + " // How much to fade out older log markers. If 0, then it uses the max amount of log markers");
            writer.println("heatDrawType: " + heatDrawTypeToInt(_heatDrawType) + " // The way to represent the heatmap. 0 = Change size, 1 = Change color");
            writer.println("heatMapStrength: " + heatMapStrength + " // How much to change colors on the heatmap");
            writer.println("mouseSensitivity: " + mouseSensitivity + " // How sensitive mouse inputs should be");
            writer.close();

            Logger.info("Successfully saved and wrote settings to config file");
        } catch (Exception e) {
            LOGGER.severe("Error saving and writing settings to config file:\n   " + Arrays.toString(e.getStackTrace()));
        }

    }

    private int drawTypeToInt(DrawType dt) {
        if (dt == DrawType.Pixel) {
            return 0;
        } else if (dt == DrawType.Dot) {
            return 1;
        } else if (dt == DrawType.Line) {
            return 2;
        } else {
            return dt == DrawType.Heat ? 3 : -1;
        }
    }

    private int heatDrawTypeToInt(HeatDrawType hdt) {
        if (hdt == HeatDrawType.Size) {
            return 0;
        } else {
            return hdt == HeatDrawType.Color ? 1 : -1;
        }
    }

    private int themeToInt(UITheme theme) {
        if (theme == UITheme.Light) {
            return 0;
        } else {
            return theme == UITheme.Dark ? 1 : -1;
        }
    }

    private void getFromFile(File inputFile) throws IOException {
        ArrayList<String> args = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(inputFile));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                args.add(line);
            }

            br.close();
        } catch (Exception e) {
            br.close();

            throw e;
        }

        int count = 0;
        if (args.size() != 0) {
            for (String arg : args) {
                String str;
                if (arg.contains("/// Player Tracker Decoder v")) {
                    str = arg.replace("/// Player Tracker Decoder v", "");
                    str = str.substring(0, str.indexOf(" - "));
                    Logger.info(str + ", " + count);

                    if (!str.equals(PlayerTrackerDecoder.VERSION)) {
                        SaveSettings();
                        LOGGER.warning("Updating log file version");
                    }
                } else {
                    int val;
                    if (arg.contains("theme: ")) {
                        str = arg.replace("theme: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        val = Integer.parseInt(str);
                        if (val == 0) {
                            uiTheme = UITheme.Light;
                        } else if (val == 1) {
                            uiTheme = UITheme.Dark;
                        }

                        count++;
                        Logger.info(uiTheme + ", " + count);
                    } else if (arg.contains("fpsLimit: ")) {
                        str = arg.replace("fpsLimit: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        fpsLimit = Integer.parseInt(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("size: ")) {
                        str = arg.replace("size: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        size = Float.parseFloat(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("convertChunkPositions: ")) {
                        str = arg.replace("convertChunkPositions: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        convertChunkPosToBlockPos = Boolean.parseBoolean(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("maxEntries: ")) {
                        str = arg.replace("maxEntries: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        maxDataEntries = Integer.parseInt(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("drawType: ")) {
                        str = arg.replace("drawType: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        val = Integer.parseInt(str);
                        if (val == 0) {
                            _drawType = DrawType.Pixel;
                        } else if (val == 1) {
                            _drawType = DrawType.Dot;
                        } else if (val == 2) {
                            _drawType = DrawType.Line;
                        } else if (val == 3) {
                            _drawType = DrawType.Heat;
                        }

                        count++;
                        Logger.info(_drawType + ", " + count);
                    } else if (arg.contains("lineThreshold: ")) {
                        str = arg.replace("lineThreshold: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        lineThreshold = Integer.parseInt(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("fancyLines: ")) {
                        str = arg.replace("fancyLines: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        fancyLines = Boolean.parseBoolean(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("hiddenLines: ")) {
                        str = arg.replace("hiddenLines: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        hiddenLines = Boolean.parseBoolean(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("fancyRendering: ")) {
                        str = arg.replace("fancyRendering: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        fancyRendering = Boolean.parseBoolean(str);

                        toggleRenderMode();

                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("terminusPoints: ")) {
                        str = arg.replace("terminusPoints: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        terminusPoints = Boolean.parseBoolean(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("ageFade: ")) {
                        str = arg.replace("ageFade: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        ageFade = Boolean.parseBoolean(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("ageFadeStrength: ")) {
                        str = arg.replace("ageFadeStrength: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        ageFadeStrength = Float.parseFloat(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("heatDrawType: ")) {
                        str = arg.replace("heatDrawType: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        val = Integer.parseInt(str);
                        if (val == 0) {
                            _heatDrawType = HeatDrawType.Size;
                        } else if (val == 1) {
                            _heatDrawType = HeatDrawType.Color;
                        }

                        count++;
                        Logger.info(_heatDrawType + ", " + count);
                    } else if (arg.contains("heatMapStrength: ")) {
                        str = arg.replace("heatMapStrength: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        heatMapStrength = Float.parseFloat(str);
                        count++;
                        Logger.info(str + ", " + count);
                    } else if (arg.contains("mouseSensitivity: ")) {
                        str = arg.replace("mouseSensitivity: ", "");
                        str = str.substring(0, str.indexOf(" //"));
                        mouseSensitivity = Integer.parseInt(str);
                        count++;
                        Logger.info(str + ", " + count);
                    }
                }
            }
        }

        Logger.info("Settings count: " + count);
        if (count != settingCount) {
            LOGGER.warning("Incomplete or old config file, updating the config file. Counted: " + count + " settings, expected: " + settingCount + ". Updating config file");
            SaveSettings();
        }
    }

    public void toggleRenderMode() {
        if (fancyRendering) {
            renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
            renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
            renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
    }
}