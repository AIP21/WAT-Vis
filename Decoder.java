import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Decoder {
  private final Random rand = new Random();
  
  private final Runtime runtime = Runtime.getRuntime();
  
  private final NumberFormat format = NumberFormat.getInstance();
  
  private final ArrayList<File> inputFiles = new ArrayList<>();
  
  private final ArrayList<Color> generatedColors = new ArrayList<>();
  
  private final Settings settings;
  
  public ArrayList<LogEntry> logEntries = new ArrayList<>();
  
  public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
  
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
  
  public Decoder(Settings set, Logger log) {
    settings = set;
    logger = log;
  }
  
  public void Decode(File[] files) {
    if (files != null && files.length != 0) {
      try {
        for (File file : files) {
          if (file.isFile() && file.getName().endsWith(".txt")) {
            inputFiles.add(file);
            String str = file.getName().substring(file.getName().lastIndexOf('-') - 7, file.getName().lastIndexOf('-') + 3);
          } 
        } 
      } catch (Exception e) {
        print("Error fetching text log files");
        e.printStackTrace();
        print(e.getStackTrace());
      } 
      inputFiles.sort(Comparator.comparing(File::getName));
      for (File inputFile : inputFiles) {
        try {
          BufferedReader br = new BufferedReader(new FileReader(inputFile));
          try {
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
              
                if (!playerLastPosMap.containsKey(items[1]))
                    playerLastPosMap.put(items[1], ((LogEntry)logEntries.get(logEntries.size() - 1)).position); 
              
                if (((LogEntry)logEntries.get(logEntries.size() - 1)).position.x < minX)
                    minX = ((LogEntry)logEntries.get(logEntries.size() - 1)).position.x; 
              
                if (((LogEntry)logEntries.get(logEntries.size() - 1)).position.x > maxX)
                    maxX = ((LogEntry)logEntries.get(logEntries.size() - 1)).position.x; 
             
                if (((LogEntry)logEntries.get(logEntries.size() - 1)).position.z < minY)
                    minY = ((LogEntry)logEntries.get(logEntries.size() - 1)).position.z; 
            
                if (((LogEntry)logEntries.get(logEntries.size() - 1)).position.z > maxY)
                    maxY = ((LogEntry)logEntries.get(logEntries.size() - 1)).position.z; 
            } 
            br.close();
            
          } catch (Throwable throwable) {
            try {
              br.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            } 
            throw throwable;
          } 
        } catch (IOException e) {
          print("Error reading input file: " + e);
        } 
      } 
      xRange = maxX - minX;
      yRange = maxY - minY;
      print("xRange: " + xRange + " yRange: " + yRange + "\n");
      print("Decoding and parsing text file successfully completed");
    } else {
      print("Error fetching text log files. They must be in the folder called \"inputs\" in the run directory");
    } 
  }
  
  private Color randomColor() {
    Color col = (new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())).brighter();
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
  
  public void print(Object input) {
    logger.Log(input);
  }
  
  public enum DrawType {
    PIXEL, DOT, LINE;
  }
}
