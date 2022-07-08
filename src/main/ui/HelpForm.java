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

        GridBagConstraints gbc = new GridBagConstraints();

        JLabel titleText = new JLabel();
        Font TitleFont = this.getFont(null, -1, 26, titleText.getFont());
        if (TitleFont != null) titleText.setFont(TitleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        titleText.setText("Help");
        add(titleText, BorderLayout.NORTH);

        JPanel helpPanel = new JPanel();
        helpPanel.setLayout(new GridBagLayout());
        add(helpPanel, BorderLayout.CENTER);

        JTextPane helpText = new JTextPane();
        helpText.setBorder(BorderFactory.createTitledBorder(null, "", 0, 0, null, null));
        helpText.setContentType("text/html");
        helpText.setEditable(false);
        helpText.setText("""
                <html>
                  <head>
                    <center>
                      <h2>
                        <font face="Segoe UI">
                          Controls
                        </font>
                      </h2>
                    </center>
                  </head>
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        helpPanel.add(helpText, gbc);

        JTextPane aboutText = new JTextPane();
        aboutText.setBorder(BorderFactory.createTitledBorder((Border) null, "", 0, 0, (Font) null, (Color) null));
        aboutText.setContentType("text/html");
        aboutText.setEditable(false);
        aboutText.setText("""
                <html>
                  <head>
                    <center>
                      <h2>
                        <font face="Segoe UI">
                          About
                        </font>
                      </h2>
                    </center>
                  </head>
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
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(30, 30, 30, 30);
        helpPanel.add(aboutText, gbc);

        closeButton = new JButton();
        closeButton.setText("Got it");
        closeButton.setMargin(new Insets(30, 60, 30, 60));
        add(closeButton, BorderLayout.SOUTH);

        setCallbacks();
    }

    private void setCallbacks() {
        closeButton.addActionListener(event -> {
            this.setVisible(false);
        });
    }

    private Font getFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }
}