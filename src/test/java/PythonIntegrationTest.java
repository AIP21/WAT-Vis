import com.anipgames.WAT_Vis.python.PythonIntegration;
import org.junit.jupiter.api.Test;

public class PythonIntegrationTest {
    @Test
    public void testPythonScript() throws Exception {
        System.out.println(PythonIntegration.executePython("OnlinePlayerCount.py", "yo mama"));
    }
}