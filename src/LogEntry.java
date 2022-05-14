package src;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogEntry implements Comparable {
  public LocalDateTime time;
  
  public String playerName;
  
  public Vector3 position;
  
  public boolean isChunk;
  
  public LogEntry(LocalDateTime time, String playerName, Vector3 position, boolean chunk) {
    this.time = time;
    this.playerName = playerName;
    this.position = position;
    this.isChunk = chunk;
  }
  
  public String toString() {
    return "Entry: " + this.playerName + ", " + this.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ", " + this.position.toString();
  }
  
  public int compareTo(Object o) {
    if (o instanceof LogEntry)
      return this.time.compareTo(((LogEntry)o).time); 
    throw new IllegalArgumentException("Not comparable");
  }
}
