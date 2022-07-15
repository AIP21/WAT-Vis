package com.anipgames.WAT_Vis.ui;

import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;

public class HelpForm extends JDialog {
    public HelpForm(PlayerTrackerDecoder main) {
        super(main);

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
        Font titleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (titleFont != null) titleText.setFont(titleFont);
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
                        <b>WAT:Vis (Where are they? : Visualizer)</b> is a tool to decode player positional data and visualize it in an intuitive and interactive way while also providing incredibly useful analytical insights for game developers and game server owners.<br>
                        <a href="https://github.com/AIP21/WAT-Vis">Github page for this tool</a><br>
                        <br>
                        <a href="https://github.com/AIP21/WAT-mod">Github page for the WAT Logging Minecraft mod, which produces data that this tool can visualize.</a><br>
                        <br>
                """ + String.format("Version %s with build id %s, built on %s<br> <a href=\"%s\">Release URL</a><br> Release notes: %s <br>", PlayerTrackerDecoder.VERSION, PlayerTrackerDecoder.BUILD_INFO[0], PlayerTrackerDecoder.BUILD_INFO[1], PlayerTrackerDecoder.BUILD_INFO[2], PlayerTrackerDecoder.BUILD_INFO[3]) + """
                        <br>
                        Copyright © 2022 Alexander Irausquin-Petit
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