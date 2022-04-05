import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Logger
{
    private File logFile;
    
    public Logger()
    {
        if(!new File("logs").exists()){
            new File("logs").mkdir();
        }
        
        logFile = new File("logs/log-" + LocalDateTime.now().toString() + ".txt");
    }
    
    public void Log(Object message) {
        System.out.println(message);
        try {
            PrintWriter writer = new PrintWriter(logFile, StandardCharsets.UTF_8);
            writer.println(LocalTime.now() + ": " +  message);
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}