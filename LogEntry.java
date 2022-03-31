import java.time.LocalDateTime;

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
        
        //if (chunk) {
            //this.position = new Vector3((int)Math.floor(Math.random()*(50+50+1)+-50), (int)Math.floor(Math.random()*(50+50+1)+-50), (int)Math.floor(Math.random()*(50+50+1)+-50));
        //}
    }
    
    public String toString() {
        return "Entry: " + playerName + ", " + time.toString() + ", " + position.toString();
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof LogEntry){
            return this.time.compareTo(((LogEntry) o).time);
        }
        throw new IllegalArgumentException("Not comparable");
    }
}