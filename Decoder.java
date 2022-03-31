import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.spi.URLStreamHandlerProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;

public class Decoder {
    private final Random rand = new Random();
    private final Runtime runtime = Runtime.getRuntime();
    private final NumberFormat format = NumberFormat.getInstance();

    public ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();

    public Map<String, Color> playerNameColorMap = new LinkedHashMap<String, Color>();

    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<String, Vector3>();
    
    public ArrayList<LocalDate> logDates = new ArrayList<LocalDate>();

    private Settings settings;

    public int minX, maxX;
    public int minY, maxY;

    public int xRange, yRange;

    private final ArrayList<File> inputFiles = new ArrayList<File>();
    
    public enum DrawType {
        PIXEL,
        DOT,
        LINE,
    }
    
    public Decoder(Settings set){
        this.settings = set;
    }

    public void Decode(File[] files) {
        if (files != null && files.length != 0) {
            try {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".txt")) {
                        inputFiles.add(file);
                        String str = file.getName().substring(file.getName().lastIndexOf('-') - 7, file.getName().lastIndexOf('-') + 3);
                        LocalDate date = LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        logDates.add(date);
                    }
                }
            } catch (Exception e) {
                print("Error fetching text log files. They must be in the folder called \"inputs\" in the run directory: " + e);
            }

            inputFiles.sort(Comparator.comparing(File::getName));

            String dataWorld = "";
            String[] split = inputFiles.get(0).getName().split("-");
            print("log date: " + logDates.get(0).toString());
            String dataDate = logDates.get(0).toString();
            if (split.length > 1) {
                dataWorld = split[1];
            }

            print("\nData range: " + dataDate + "\n");

            String[] items;

            // Iterate through every provided file
            for (File inputFile : inputFiles) {
                // Read the file and parse each line into lists of entries
                try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                    for (String line; (line = br.readLine()) != null; ) {
                        if(line.charAt(line.length() - 1) != ';')
                            line += ';';
                        items = line.split(";");
                        
                        if (items[0].length() < 8) {
                            logEntries.add(new LogEntry(LocalDateTime.parse(inputFile.getName().substring(inputFile.getName().lastIndexOf('-') - 7, inputFile.getName().lastIndexOf('-') + 3) + ":" + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm")), items[1], items[2].contains("(") ? Vector3.parseVector3(items[2], settings.upscaleMultiplier) : Vector3.parseVector3FromChunk(items[2], settings.convertChunkPosToBlockPos, settings.upscaleMultiplier), items[2].contains("[")));
                        } else {
                            logEntries.add(new LogEntry(LocalDateTime.parse(inputFile.getName().substring(inputFile.getName().lastIndexOf('-') - 7, inputFile.getName().lastIndexOf('-') + 3) + ":" + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss")), items[1], items[2].contains("(") ? Vector3.parseVector3(items[2], settings.upscaleMultiplier) : Vector3.parseVector3FromChunk(items[2], settings.convertChunkPosToBlockPos, settings.upscaleMultiplier), items[2].contains("[")));
                        }

                        if (!playerNameColorMap.containsKey(items[1])) {
                            playerNameColorMap.put(items[1], randomColor());
                        }

                        if (!playerLastPosMap.containsKey(items[1])) {
                            playerLastPosMap.put(items[1], logEntries.get(logEntries.size() - 1).position);
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
                } catch (IOException e) {
                    print("Error reading input file: " + e);
                }
            }

            xRange = (maxX - minX);
            yRange = (maxY - minY);

            print("xRange: " + xRange + " yRange: " + yRange + "\n");
            
            print("FINISHED! Decoding and parsing text file successfully completed");
        } else {
            print("Error fetching text log files. They must be in the folder called \"inputs\" in the run directory");
            new File(System.getProperty("user.dir") + "/inputs").mkdir();
            print("Inputs folder was just created for you at the run directory, put your input text log files in that inputs folder");
        }
    }

    private  final ArrayList<Color> generatedColors = new ArrayList<Color>();

    private  Color randomColor() {
        Color col = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()).brighter();
        if (containsSimilarColor(generatedColors, col)) {
            return randomColor();
        } else {
            generatedColors.add(col);
            return col;
        }
    }

    private  boolean containsSimilarColor(ArrayList<Color> listToCheck, Color colorToCheck) {
        for (Color col : listToCheck) {
            if (isSimilarColor(col, colorToCheck))
                return true;
        }

        return false;
    }

    private  boolean isSimilarColor(Color a, Color b) {
        return approximately(a.getRed(), b.getRed(), 20) &&
                approximately(a.getGreen(), b.getGreen(), 20) &&
                approximately(a.getBlue(), b.getBlue(), 20);
    }

    private  boolean approximately(int a, int b, float threshold) {
        return a - b < threshold;
    }
    
    public  void print(Object input) {
        System.out.println(input);
    }

    private  void printMemoryStats() {
        print(("Used memory: " + format.format((runtime.totalMemory() - runtime.freeMemory()) / 1024) + "\n") + ("Max memory: " + format.format(runtime.maxMemory() / 1024) + "\n"));
    }
}
