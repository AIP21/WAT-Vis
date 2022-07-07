package src.main.ui;

import com.seedfinding.mccore.state.Dimension;
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
    private JPanel bottomPanel;
    private JLabel titleText;
    private JLabel helpText;
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
        titleText = new JLabel();
        Font TitleFont = this.getFont(null, -1, 26, titleText.getFont());
        if (TitleFont != null) titleText.setFont(TitleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        titleText.setText("Help");
        add(titleText, BorderLayout.NORTH);

        helpText = new JLabel();
        helpText.setBorder(BorderFactory.createTitledBorder(null, "Controls", 0, 0, null, null));
        helpText.setText(Utils.convertToMultiline("""
                Pan: Left Click + Drag
                                
                Zoom: Scroll Wheel
                                
                Select Point: Left Click
                                
                Select Area: Right Click
                                
                 - [Modifier] Select Multiple: Shift
                 
                 - [Modifier] Select Hidden: Control
                 
                Show Hidden Points: Control
                                
                Select All Points: Control + A
                """));
        add(helpText, BorderLayout.CENTER);

        closeButton = new JButton();
        closeButton.setText("Got it!");
        closeButton.setHorizontalAlignment(0);
        closeButton.setHorizontalTextPosition(0);
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