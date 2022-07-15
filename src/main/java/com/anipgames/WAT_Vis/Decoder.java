package com.anipgames.WAT_Vis;

import com.anipgames.WAT_Vis.config.Settings;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.anipgames.WAT_Vis.util.objects.LogEntry;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;
import com.anipgames.WAT_Vis.util.objects.Vector3;

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
    private static final ArrayList<Color> generatedColors = new ArrayList<>();

    public static DecodedData Decode(PlayerTrackerDecoder main, ArrayList<File> inputFiles, int maxEntries, boolean convertChunkPositions) {
        Logger.info("Initializing new decoding process");

        final long nowMs = System.currentTimeMillis();
        final boolean limitEntries = maxEntries > 0;

        // Check if there are actually any input files
        if (inputFiles == null || inputFiles.size() == 0) {
            Logger.warn("Unable to load log files, no input files supplied.");
            return null;
        }

        // Remove all directories and non .txt files
        inputFiles.removeIf(file -> file.isDirectory() || (file.isFile() && !file.getName().endsWith(".txt")));
        Logger.info("Pruned input data");

        // Sort files by date
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
        Logger.info("Sorted input data");

        Logger.info("Successfully initialized new decoding process");

        Logger.info("Decoding data...");

        LinkedHashMap<LocalDateTime, LogEntry> logEntriesByTime = new LinkedHashMap<>();
        HashMap<String, Color> playerNameColorMap = new HashMap<>();
        HashMap<String, Boolean> playerNameEnabledMap = new HashMap<>();
        HashMap<String, Vector3> playerLastPosMap = new HashMap<>();
        HashMap<String, Integer> playerCountMap = new HashMap<>();
        generatedColors.clear();

        String dataWorld = null;
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        boolean reachedLimit = false;

        int lines = 0;

        for (File file : inputFiles) {
            Logger.info("Decoding input file: " + file.getName());

            String fileDate = file.getName().substring(file.getName().lastIndexOf('-') - 7, file.getName().lastIndexOf('-') + 3) + ";";
            if (dataWorld == null) {
                dataWorld = file.getName().substring(0, file.getName().indexOf('-'));
                Logger.info("Data world: " + dataWorld);
            }

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    if (limitEntries && logEntriesByTime.size() > maxEntries) {
                        Logger.warn("Max configured data entries reached, stopped decoding");
                        reachedLimit = true;
                        break;
                    }

                    if (line.charAt(line.length() - 1) != ';')
                        line += ';';

                    String[] items = line.split(";");

                    LocalDateTime dateTime;
                    if (items[0].length() < 8) {
                        dateTime = LocalDateTime.parse(fileDate + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm"));
                    } else {
                        dateTime = LocalDateTime.parse(fileDate + items[0], DateTimeFormatter.ofPattern("yyyy-MM-dd;HH:mm:ss"));
                    }

                    if (startTime == null) {
                        startTime = dateTime;
                    }
                    endTime = dateTime;

                    Vector3 position = Vector3.parseVector3(items[2], convertChunkPositions);

                    String playerName = items[1];

                    logEntriesByTime.put(dateTime, new LogEntry(dateTime, playerName, position));

                    playerNameColorMap.putIfAbsent(playerName, randomColor(0));
                    playerNameEnabledMap.putIfAbsent(playerName, true);
                    playerLastPosMap.putIfAbsent(playerName, position);
                    playerCountMap.merge(playerName, 1, Integer::sum);

                    if (position.x < minX)
                        minX = position.x;

                    if (position.x > maxX)
                        maxX = position.x;

                    if (position.z < minY)
                        minY = position.z;

                    if (position.z > maxY)
                        maxY = position.z;

                    lines++;
                }
                br.close();
            } catch (IOException e) {
                Logger.error("Error reading input file:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            }

            if (reachedLimit)
                break;
        }

        Logger.info("Lines decoded: " + lines);

        int xRange = maxX - minX;
        int yRange = maxY - minY;
        Logger.info("maxX: " + maxX + " minX: " + minX + "; maxY: " + maxY + " minY: " + minY);
        Logger.info("xRange: " + xRange + ", yRange: " + yRange);

        final long durMs = System.currentTimeMillis() - nowMs;

        Logger.info("Successfully decoded " + logEntriesByTime.size() + " entries. Took " + durMs + "ms");

        return new DecodedData(logEntriesByTime, playerNameColorMap, playerNameEnabledMap, playerLastPosMap, playerCountMap, minX, maxX, minY, maxY, xRange, yRange, dataWorld, startTime, endTime);
    }

    private static Color randomColor(int iter) {
        Color col = Utils.randColor();

        // Modify the color to better fit the current ui theme
        if (Settings.INSTANCE.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
            col = col.darker();
        } else {
            col = col.brighter();
            col = col.brighter(); // Intentionally done twice
        }

        // Make sure that the color is not too similar to any other color
        Color finalCol = col;
        if (generatedColors.stream().anyMatch(x -> Utils.approximately(x, finalCol)) && iter < 100)
            return randomColor(iter + 1);

        generatedColors.add(col);
        return col;
    }
}