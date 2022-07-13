package ui;

import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.EngineOptions;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.js.JsAccessible;
import com.teamdev.jxbrowser.js.JsObject;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFinished;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import main.PlayerTrackerDecoder;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class Dashboard {
    public static void main(String[] args) {
        JEditorPane jep = new JEditorPane();
        jep.setEditable(false);
        try {
            jep.setPage("file:///" + PlayerTrackerDecoder.DIR_ROOT + "/src/main/java/ui/dashboard/index.html");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.print(e);
        }
        JScrollPane scrollPane = new JScrollPane(jep);
        JFrame f = new JFrame("Test HTML");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(scrollPane);
        f.setPreferredSize(new Dimension(800, 600));
        f.setVisible(true);
    }
}
