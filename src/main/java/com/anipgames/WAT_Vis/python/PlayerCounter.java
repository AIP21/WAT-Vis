package com.anipgames.WAT_Vis.python;

import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.anipgames.WAT_Vis.util.objects.LogEntry;
import com.anipgames.WAT_Vis.util.Dict;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerCounter {
    private final DecodedData data;

    public PlayerCounter(DecodedData data) {
        this.data = data;

        Logger.info(analyzeActivityWeek());

        Logger.info(analyzePerPeriod());
    }

    public static ImmutablePair<ArrayList<String>, ArrayList<Integer>> analyzeActivityAll(DecodedData data) {
        long cur = System.currentTimeMillis();

        LinkedHashMap<String, Integer> playerCounts = new LinkedHashMap<>();
        LinkedHashMap<String, HashSet<String>> playersPerDay = new LinkedHashMap<>();
        for (LogEntry entry : data.logEntries.values()) {
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

        return new ImmutablePair<>(new ArrayList<>(playerCounts.keySet()), new ArrayList<>(playerCounts.values()));
    }

    // A: Date, B: List of players for that day
    public Dict<String, Integer> analyzeActivityWeek() {
        long cur = System.currentTimeMillis();

        Dict<String, Integer> playerCounts = new Dict<>();
        Dict<String, HashSet<String>> playersPerDay = new Dict<>();
        for (LogEntry entry : data.logEntries.values()) {
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

    public static ImmutablePair<ArrayList<String>, ArrayList<Integer>> analyzeActivityWeek(DecodedData data) {
        long cur = System.currentTimeMillis();

        LinkedHashMap<String, Integer> playerCounts = new LinkedHashMap<>();
        LinkedHashMap<String, HashSet<String>> playersPerDay = new LinkedHashMap<>();
        for (LogEntry entry : data.logEntries.values()) {
            String playerName = entry.playerName;
            String date = entry.time.getDayOfWeek().toString();

            playersPerDay.putIfAbsent(date, new HashSet<>());

            if (!playersPerDay.get(date).contains(playerName)) {
                playerCounts.merge(date, 1, Integer::sum);
            }

            playersPerDay.get(date).add(playerName);
        }

        long diff = System.currentTimeMillis() - cur;

        Logger.info(String.format("Finished analyzing daily player counts in: %d ms", diff));

        return new ImmutablePair<>(new ArrayList<>(playerCounts.keySet()), new ArrayList<>(playerCounts.values()));
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
        for (LogEntry entry : data.logEntries.values()) {
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

    public static ImmutablePair<ArrayList<String>, ArrayList<Integer>> analyzeActivityDay(DecodedData data) {
        long cur = System.currentTimeMillis();

        LinkedHashMap<String, Integer> playerCounts = new LinkedHashMap<>();
        LinkedHashMap<String, HashSet<String>> playersPerDay = new LinkedHashMap<>();
        for (LogEntry entry : data.logEntries.values()) {
            String playerName = entry.playerName;
            int hour = entry.time.getHour();

            playersPerDay.putIfAbsent(String.valueOf(hour), new HashSet<>());

            if (!playersPerDay.get(String.valueOf(hour)).contains(playerName)) {
                playerCounts.merge(String.valueOf(hour), 1, Integer::sum);
            }

            playersPerDay.get(String.valueOf(hour)).add(playerName);
        }

        long diff = System.currentTimeMillis() - cur;

        Logger.info(String.format("Finished analyzing day period player counts in: %d ms", diff));

        return new ImmutablePair<>(new ArrayList<>(playerCounts.keySet()), new ArrayList<>(playerCounts.values()));
    }

    public static ImmutablePair<ArrayList<String>, ArrayList<Integer>> analyzeDailyEntries(DecodedData data) {
        long cur = System.currentTimeMillis();

        LinkedHashMap<String, Integer> entryCountsPerDay = new LinkedHashMap<>();

        for (LogEntry entry : data.logEntries.values()) {
            entryCountsPerDay.merge(entry.time.toLocalDate().toString(), 1, Integer::sum);
        }

        long diff = System.currentTimeMillis() - cur;

        Logger.info(String.format("Finished analyzing day period player counts in: %d ms", diff));

        return new ImmutablePair<>(new ArrayList<>(entryCountsPerDay.keySet()), new ArrayList<>(entryCountsPerDay.values()));
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

    private static int getWeekdayNumber(LocalDate date) {
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