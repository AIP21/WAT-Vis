package util.objects;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry implements Comparable<LogEntry> {
    public LocalDateTime time;
    public String playerName;
    public Vector3 position;
    public boolean show = true;

    public LogEntry(LocalDateTime time, String playerName, Vector3 position) {
        this.time = time;
        this.playerName = playerName;
        this.position = position;
    }

    public String toString() {
        return "Entry: " + this.playerName + ", " + this.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ", " + this.position.toString();
    }

    @Override
    public int compareTo(LogEntry other) {
        return this.time.compareTo(other.time);
    }
}
