package com.anipgames.WAT_Vis.python;

import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.anipgames.WAT_Vis.util.objects.LogEntry;
import com.seedfinding.latticg.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;

public class PlayerCounter {
    private final DecodedData data;

    public PlayerCounter(DecodedData data) {
        this.data = data;

        Logger.info(analyzeDaily());

        Logger.info(analyzePerPeriod());
    }

    // A: Date, B: List of players for that day
    public HashMap<String, Integer> analyzeDaily() {
        long cur = System.currentTimeMillis();

        HashMap<String, Integer> playerCounts = new HashMap<>();
        HashMap<String, HashSet<String>> playersPerDay = new HashMap<>();
        for (LogEntry entry : data.logEntriesByTime.values()) {
            String playerName = entry.playerName;
            String date = entry.time.toLocalDate().toString();

            playersPerDay.putIfAbsent(date, new HashSet<>());

            if (!playersPerDay.get(date).contains(playerName)) {
                playerCounts.merge(date, 1, Integer::sum);
            }

            playersPerDay.get(date).add(playerName);
        }

        long diff = System.currentTimeMillis() - cur;

        Logger.info(String.format("Finished analyzing daily player counts in: %d ms", diff));

        return playerCounts;
    }

    // A: Date and its period, B: Avg player count during the morning, midday, afternoon, and evening, night
    /*
    Periods:
        1. 5-10
        2. 11-13
        3. 14-17
        4. 18-21
        5. 22-4
     */
    // Long: Date hashcode
    // Integer #1: Period
    public HashMap<Pair<Integer, Integer>, Integer> analyzePerPeriod() {
        long cur = System.currentTimeMillis();

        HashMap<Pair<Integer, Integer>, Integer> playerCounts = new HashMap<>();
        for (LocalDateTime time : data.logEntriesByTime.keySet()) {
            playerCounts.merge(new Pair<>(hashLocalDate(time.toLocalDate()), getPeriod(time.toLocalTime())), 1, Integer::sum);
        }

        long diff = System.currentTimeMillis() - cur;

        Logger.info(String.format("Finished analyzing day period player counts in: %d ms", diff));

        return playerCounts;
    }

    private int getPeriod(LocalTime time) {
        int hour = time.getHour();
        if (hour <= 4) {
            return 5;
        } else if (hour <= 10) {
            return 1;
        } else if (hour <= 13) {
            return 2;
        } else if (hour <= 17) {
            return 3;
        } else if (hour <= 21) {
            return 4;
        } else {
            return 5;
        }
    }

    private int hashLocalDate(LocalDate date) {
        return Integer.parseInt(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
}