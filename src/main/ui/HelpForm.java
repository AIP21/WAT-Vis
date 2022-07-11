package src.main.ui;

import com.seedfinding.mccore.version.MCVersion;
import src.main.PlayerTrackerDecoder;
import src.main.config.Settings;
import src.main.importing.filters.ImageFileFilter;
import src.main.importing.filters.TextFileFilter;
import src.main.util.Logger;
import src.main.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;

public class HelpForm extends JDialog {
    private JButton closeButton;

    public HelpForm(PlayerTrackerDecoder main) {
        super(main, "Help");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        setSize(new java.awt.Dimension(720, 480));
        setResizable(false);

        initComponents();

        Logger.info("Help page opened");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        GridBagConstraints gbc;

        JLabel titleText = new JLabel("Help");
        Font TitleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (TitleFont != null) titleText.setFont(TitleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        add(titleText, BorderLayout.NORTH);

        //region Help Text
        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new GridBagLayout());
        add(helpPanel, BorderLayout.CENTER);

        JTextPane helpText = new JTextPane();
        helpText.setBorder(BorderFactory.createTitledBorder(null, "Controls", 0, 0, null, null));
        helpText.setContentType("text/html");
        helpText.setEditable(false);
        helpText.setText("""
                <html>
                  <body>
                    <center>
                      Pan: Left Click + Drag<br>
                      Zoom: Scroll Wheel<br>
                      Select Point: Left Click<br>
                      Select Area: Right Click<br>
                      [Modifier] Select Multiple: Shift<br>
                      [Modifier] Select Hidden: Control<br>
                      Show Hidden Points: Control<br>
                      Select All Points: Control + A<br>
                    </center>
                  </body>
                </html>
                """);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 5);
        helpPanel.add(helpText, gbc);

        JTextPane aboutText = new JTextPane();
        aboutText.setBorder(BorderFactory.createTitledBorder( null, "About", 0, 0,  null, (Color) null));
        aboutText.setContentType("text/html");
        aboutText.setEditable(false);
        aboutText.setText("""
                <html>
                  <body>
                    <center>
                      <font face="Segoe UI">
                        <b>Player Tracker Decoder</b> is a tool to decode the data logged in the format used by the Minecraft mod WAT (Where are they?)<br>
                        <a href="https://github.com/AIP21/TrackerDecoderApp">Github Page</a><br>
                        <a href="https://github.com/AIP21/WAT-mod">WAT mod Github Page</a><br>
                        <a href="https://modrinth.com/mod/wat">WAT mod Modrinth Page</a><br>
                        <br>
                """ + String.format("Version %s with build id %s, built on %s<br> <a href=\"%s\">Release URL</a><br> Release notes: %s <br>", PlayerTrackerDecoder.VERSION, PlayerTrackerDecoder.BUILD_INFO[0], PlayerTrackerDecoder.BUILD_INFO[1], PlayerTrackerDecoder.BUILD_INFO[2], PlayerTrackerDecoder.BUILD_INFO[3]) + """
                      </font>
                    </center>
                  </body>
                </html>
                """);
        gbc.gridx++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 5, 10, 10);
        helpPanel.add(aboutText, gbc);
        //endregion
    }
}