package util.objects;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;

public class DecodedData {
    public LinkedHashMap<LocalDateTime, LogEntry> logEntriesByTime;
    public HashMap<String, Color> playerNameColorMap;
    public HashMap<String, Boolean> playerNameEnabledMap;
    public HashMap<String, Vector3> playerLastPosMap;
    public HashMap<String, Integer> playerCountMap;
    public int minX;
    public int maxX;
    public int minY;
    public int maxY;
    public int xRange;
    public int yRange;

    public LocalDateTime startTime;
    public LocalDateTime endTime;

    public String dataWorld;

    public DecodedData(LinkedHashMap<LocalDateTime, LogEntry> logEntriesByTime, HashMap<String, Color> playerNameColorMap, HashMap<String, Boolean> playerNameEnabledMap, HashMap<String, Vector3> playerLastPosMap, HashMap<String, Integer> playerCountMap, int minX, int maxX, int minY, int maxY, int xRange, int yRange, String dataWorld, LocalDateTime startTime, LocalDateTime endTime) {
        this.logEntriesByTime = logEntriesByTime;
        this.playerNameColorMap = playerNameColorMap;
        this.playerNameEnabledMap = playerNameEnabledMap;
        this.playerLastPosMap = playerLastPosMap;
        this.playerCountMap = playerCountMap;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.xRange = xRange;
        this.yRange = yRange;
        this.dataWorld = dataWorld;

        this.startTime = startTime;
        this.endTime = endTime;
    }
}