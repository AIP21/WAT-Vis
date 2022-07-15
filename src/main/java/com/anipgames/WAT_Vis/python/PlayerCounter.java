package com.anipgames.WAT_Vis.python;

import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.anipgames.WAT_Vis.util.objects.LogEntry;
import com.seedfinding.latticg.util.Pair;
import com.anipgames.WAT_Vis.util.Dict;

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
    public Dict<String, Integer> analyzeDaily() {
        long cur = System.currentTimeMillis();

        Dict<String, Integer> playerCounts = new Dict<>();
                 Dict<String, HashSet<String>> playersPerDay = new Dict<>();
                 for (LogEntry entry : data.logEntries) {
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
    public Dict<String, Integer> analyzePerPeriod() {
        long cur = System.currentTimeMillis();

        Dict<String, Integer> playerCounts = new Dict<>();
        Dict<String, HashSet<String>> playersPerDay = new Dict<>();
        for (LogEntry entry : data.logEntries) {
            String playerName = entry.playerName;
            LocalDateTime dateTime = entry.time;

            playersPerDay.putIfAbsent(dateTime.toString(), new HashSet<>());

            if (!playersPerDay.get(dateTime.toString()).contains(playerName)) {
                playerCounts.merge(String.valueOf((24 * getWeekdayNumber(dateTime.toLocalDate()) - 1) + dateTime.getHour()), 1, Integer::sum);
            }

            playersPerDay.get(dateTime.toString()).add(playerName);
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

    private int getWeekdayNumber(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            case SATURDAY -> 6;
            case SUNDAY -> 7;
        };
    }
}