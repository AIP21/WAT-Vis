package src;

import src.ui.ImportForm;
import src.ui.RangedSlider.RangeSlider;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class PlayerTrackerDecoder extends JFrame {
    private final JFileChooser chooser;

    private final JToolBar toolbar;

    private final Decoder decoder;

    private final Settings settings;

    public Panel mainPanel;

    public JScrollPane scrollPane;

    private boolean alreadyImported = false;

    public RangeSlider dateRangeSlider;

    public JLabel startDateLabel;
    public JLabel endDateLabel;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private JComboBox<Decoder.DrawType> drawTypeChooser;
    private JComboBox<HeatDrawType> heatDrawTypeChooser;

    private JPanel heatMapComponent;
    private JLabel heatMapThresholdTitle;
    private JSlider heatMapThresholdSlider;
    private JLabel heatMapThresholdLabel;

    private JPanel drawSizeComponent;
    private JSlider sizeSlider;
    private JLabel sizeLabel;
    private JLabel sizeTypeLabel;

    private JPanel lineThresholdComponent;
    private JSlider lineThresholdSlider;
    private JLabel lineThresholdLabel;
    private JLabel lineThresholdTitle;

    private JToggleButton fancyLinesToggle;
    private JToggleButton terminusPointsToggle;

    private JToggleButton showHiddenLinesToggle;

    private JPanel ageFadeComponent;
    private JToggleButton ageFadeToggle;

    private JLabel exportUpscaleLabel;

    private JLabel xLabel;
    private JLabel zLabel;
    private JLabel backgroundOpacityLabel;

    private JComponent backgroundImagePanel;

    private File[] files;

    private ArrayList<LocalDateTime> logDates;

    private final Logger logger;

    private JTabbedPane tabbedPane;

    private ImportForm importForm;

    //region Resources
    private ImageIcon playIcon;
    private ImageIcon pauseIcon;
    private ImageIcon replayIcon;
    private ImageIcon speedIcon;
    private ImageIcon importIcon;
    private ImageIcon exportIcon;
    private ImageIcon toggleIconON;
    private ImageIcon toggleIconOFF;
    private ImageIcon radioIconON;
    private ImageIcon radioIconOFF;
    private ImageIcon checkBoxIconON;
    private ImageIcon checkBoxIconOFF;
    //endregion

    private boolean hasBackgroundImage = false;
    private final int TARGET_FPS = 30;
    private final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    public static final String version = "1.8.3";

    public PlayerTrackerDecoder() {
        logger = new Logger(version);

        logger.Log("Initializing primary systems", Logger.MessageType.INFO);

        settings = new Settings(logger);
        decoder = new Decoder(settings, logger);

        initMainFrame();

        try {
            logger.Log("Loading resources", Logger.MessageType.INFO);
            playIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/play.png"))).getScaledInstance(24, 24, Image.SCALE_SMOOTH), "Play");
            pauseIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/pause.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Pause");
            replayIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/replay.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Replay");
            speedIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/fastForward.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Fast Forward");
            importIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/import.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Import");
            exportIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/export.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "exportPanel");
            toggleIconON = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/toggle-true.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Disable");
            toggleIconOFF = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/toggle-false.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Enable");
            radioIconON = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/radio-true.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Hide");
            radioIconOFF = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/radio-false.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Show");
            checkBoxIconON = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/checkBox-true.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "On");
            checkBoxIconOFF = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/checkBox-false.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Off");
        } catch (Exception e) {
            logger.Log("Error loading icon resources:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
        }

        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("inputs"));
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(new TextFileFilter());
        chooser.setAcceptAllFileFilterUsed(false);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setJMenuBar(menuBar);

        JMenuItem dataFileImportButton = new JMenuItem("");
        dataFileImportButton.setIcon(importIcon);
        dataFileImportButton.setPreferredSize(new Dimension(48, 48));
        dataFileImportButton.setMinimumSize(new Dimension(47, 47));
        menuBar.add(dataFileImportButton);

        toolbar = new JToolBar("Settings");
        toolbar.setVisible(false);
        menuBar.add(toolbar);

        JMenuBar bottomMenuBar = new JMenuBar();
        add(bottomMenuBar, "South");

        JLabel label = new JLabel();
        mainPanel.RenderedPointsLabel = label;
        bottomMenuBar.add(label);
        bottomMenuBar.add(mainPanel.CoordinateLabel);
        bottomMenuBar.add(mainPanel.SelectedEntryLabel);

        dataFileImportButton.addActionListener(event -> {
            hasBackgroundImage = false;
            mainPanel.isPlaying = false;
            mainPanel.ShouldDraw = false;

            if (importForm != null) {
                importForm.setVisible(false);
                importForm = null;
            }
            importForm = new ImportForm(this, settings, logger);
            importForm.setVisible(true);
            importForm.setLocationRelativeTo(this);
        });

        logger.Log("Successfully initialized all subsystems", Logger.MessageType.INFO);
    }

    public void ConfirmImport(ArrayList<File> files) {
        this.files = files.toArray(new File[0]);

        try {
            if (alreadyImported) {
                mainPanel.Reset();
            }

            decodeAndDisplay();
        } catch (IOException e) {
            logger.Log("Error decoding the selected input log files:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            PlayerTrackerDecoder myFrame = new PlayerTrackerDecoder();
            myFrame.setVisible(true);
        });
    }

    private void initMainFrame() {
        logger.Log("Initializing primary frame subsystem", Logger.MessageType.INFO);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.Log("Error setting system look and feel for UI:\n   " + Arrays.toString(ex.getStackTrace()), Logger.MessageType.ERROR);
        }

        setTitle("Player Tracker Decoder App - v" + version);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.DARK_GRAY);
        mainPanel = new Panel(settings, logger, this);
        (new Thread(mainPanel)).start();
        mainPanel.setDoubleBuffered(true);
        scrollPane = new JScrollPane(mainPanel);
        mainPanel.CoordinateLabel = new JLabel();
        mainPanel.CoordinateLabel.setText("   (0, 0) | ");
        mainPanel.SelectedEntryLabel = new JLabel("Nothing Selected");
        mainPanel.SelectedEntryLabel.setVisible(false);
        scrollPane.setDoubleBuffered(true);
        add(scrollPane);
        add(mainPanel);
        mainPanel.setVisible(true);
        revalidate();

        logger.Log("Successfully initialized primary frame subsystem", Logger.MessageType.INFO);
    }

    public void LoadWorldImage(File imgFile) {
        hasBackgroundImage = true;

        try {
            mainPanel.backgroundImage = mainPanel.LoadBackgroundImage(imgFile);

            backgroundImagePanel = new JPanel();
            backgroundImagePanel.setLayout(new GridLayout(3, 5));

            backgroundImagePanel.add(new JLabel("World Background Image Offset:   "));

            int width = mainPanel.backgroundImage.getWidth();
            int height = mainPanel.backgroundImage.getHeight();
            int defaultX = mainPanel.xBackgroundOffset;
            int defaultZ = mainPanel.zBackgroundOffset;

            backgroundImagePanel.add(new JLabel("Overworld offset: (-6384, -5376)  Nether offset: (-1008, -1969)"));

            JSlider xOffsetSlider = new JSlider(0, -width, width, defaultX);
            xOffsetSlider.setPreferredSize(new Dimension(100, 48));
            xOffsetSlider.setPaintTicks(false);
            xOffsetSlider.setMajorTickSpacing(1);
            xOffsetSlider.setMinorTickSpacing(0);
            xOffsetSlider.setPaintLabels(false);
            mainPanel.xBackgroundOffset = defaultX;
            xLabel = new JLabel("X Offset: " + defaultX);
            xOffsetSlider.addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                int x = source.getValue();
                xLabel.setText("X Offset: " + x);
                mainPanel.xBackgroundOffset = x;
                mainPanel.update = true;
                mainPanel.repaint();

                logger.Log("Changed the world background image X offset to: " + x, Logger.MessageType.INFO);
            });
            backgroundImagePanel.add(xOffsetSlider);
            backgroundImagePanel.add(xLabel);

            JSlider zOffsetSlider = new JSlider(0, -height, height, defaultZ);
            zOffsetSlider.setPreferredSize(new Dimension(100, 48));
            zOffsetSlider.setPaintTicks(false);
            zOffsetSlider.setMajorTickSpacing(1);
            zOffsetSlider.setMinorTickSpacing(0);
            zOffsetSlider.setPaintLabels(false);
            mainPanel.zBackgroundOffset = defaultZ;
            zLabel = new JLabel("Y Offset: " + defaultZ);
            zOffsetSlider.addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                int z = source.getValue();
                zLabel.setText("Y Offset: " + z);
                mainPanel.zBackgroundOffset = z;
                mainPanel.update = true;
                mainPanel.repaint();

                logger.Log("Changed the world background image Y offset to: " + z, Logger.MessageType.INFO);
            });
            backgroundImagePanel.add(zOffsetSlider);
            backgroundImagePanel.add(zLabel);

            JSlider backgroundOpacitySlider = new JSlider(0, 0, 100, 50);
            backgroundOpacitySlider.setPreferredSize(new Dimension(100, 48));
            backgroundOpacitySlider.setPaintTicks(true);
            backgroundOpacitySlider.setMajorTickSpacing(10);
            backgroundOpacitySlider.setMinorTickSpacing(5);
            backgroundOpacitySlider.setPaintLabels(true);
            backgroundOpacityLabel = new JLabel("Opacity: " + 50 + "%");
            backgroundOpacitySlider.addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                int opacity = source.getValue();
                backgroundOpacityLabel.setText("Opacity: " + opacity + "%");
                mainPanel.backgroundOpacity = opacity / 100.0F;
                mainPanel.update = true;
                mainPanel.repaint();

                logger.Log("Changed the world background image opacity to: " + opacity, Logger.MessageType.INFO);
            });
            backgroundImagePanel.add(backgroundOpacitySlider);
            backgroundImagePanel.add(backgroundOpacityLabel);

            logger.Log("Successfully loaded world background image", Logger.MessageType.INFO);
        } catch (IOException e) {
            logger.Log("Error reading selected world background image:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
        }
    }

    private void initDataSettingsToolBar(boolean remove) {
        logger.Log("Initializing toolbar subsystem", Logger.MessageType.INFO);

        toolbar.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        if (remove) toolbar.removeAll();
        toolbar.setDoubleBuffered(true);

        tabbedPane = new JTabbedPane();
        toolbar.add(tabbedPane);

        //region Data
        JComponent dataPanel = new JPanel();
//        dataPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        dataPanel.add(new JLabel("Dates To Represent:   "));
        dateRangeSlider = new RangeSlider(0, logDates.size() - 1);
        dateRangeSlider.setPreferredSize(new Dimension(600, 48));
        dateRangeSlider.setValue(0);
        dateRangeSlider.setUpperValue(logDates.size() - 1);
        dateRangeSlider.setPaintTicks(false);
        dateRangeSlider.setMajorTickSpacing(1);
        dateRangeSlider.setMinorTickSpacing(0);
        dateRangeSlider.setPaintLabels(false);
        dateRangeSlider.setSnapToTicks(false);
        startDate = logDates.get(0);
        endDate = logDates.get(logDates.size() - 1);
        startDateLabel = new JLabel(startDate.toString().replace("T", "; "));
        endDateLabel = new JLabel(endDate.toString().replace("T", "; "));
        dateRangeSlider.addChangeListener(e -> {
            RangeSlider source = (RangeSlider) e.getSource();
            int value1 = source.getValue();
            int value2 = source.getUpperValue();
            startDate = logDates.get(value1);
            endDate = logDates.get(value2);
            mainPanel.startDate = startDate;
            mainPanel.endDate = endDate;
            startDateLabel.setText(startDate.toString().replace("T", "; "));
            endDateLabel.setText(endDate.toString().replace("T", "; "));

            if (!mainPanel.isPlaying) {
                mainPanel.updatePoints(true);
                logger.Log("Changed date range slider: From " + startDate.toString() + " to " + endDate.toString(), Logger.MessageType.INFO);
            }
        });
        dataPanel.add(startDateLabel);
        dataPanel.add(dateRangeSlider);
        dataPanel.add(endDateLabel);

        JToggleButton animatePlayPause = new JToggleButton("", false);
        animatePlayPause.setIcon(mainPanel.isPlaying ? pauseIcon : playIcon);
        animatePlayPause.setPreferredSize(new Dimension(24, 24));
        animatePlayPause.setMargin(new Insets(2, 2, 2, 2));
        animatePlayPause.setBorder(BorderFactory.createEmptyBorder());

//        button.setRolloverIcon(ICON_CLOSE);
//        button.setRolloverEnabled(true);

        animatePlayPause.addItemListener(ev -> {
            mainPanel.isPlaying = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.dateTimeIndex = dateRangeSlider.getUpperValue();

            animatePlayPause.setIcon(mainPanel.isPlaying ? pauseIcon : playIcon);
            logger.Log(mainPanel.isPlaying ? "Started playing animation" : "Stopped playing animation", Logger.MessageType.INFO);
        });
        dataPanel.add(animatePlayPause);

        tabbedPane.addTab("Data", null, dataPanel, "Data range settings");
        //endregion

        //region Players
        JComponent playerPanel = new JPanel();
        playerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        playerPanel.add(new JLabel("Players"));
        String[] nameSet = mainPanel.playerNameColorMap.keySet().toArray(new String[0]);
        for (int i = 0; i < nameSet.length && i < 25; i++) {
            String player = nameSet[i];
            JMenuItem colButton = new JMenuItem(player);
            JToggleButton toggle = new JToggleButton("", mainPanel.playerNameEnabledMap.get(player));
            colButton.setForeground(mainPanel.playerNameColorMap.get(player));
            toggle.setIcon(mainPanel.playerNameEnabledMap.get(player) ? radioIconON : radioIconOFF);
            toggle.setPreferredSize(new Dimension(24, 24));
            toggle.setMargin(new Insets(2, 2, 2, 2));
            toggle.setBorder(BorderFactory.createEmptyBorder());

            playerPanel.add(colButton);
            playerPanel.add(toggle);

            colButton.addActionListener(event -> {
                Color selectedColor = JColorChooser.showDialog(PlayerTrackerDecoder.this, "Select player color", mainPanel.playerNameColorMap.get(player));
                colButton.setForeground(selectedColor);
                mainPanel.playerNameColorMap.put(player, selectedColor);
                mainPanel.update = true;
                mainPanel.repaint();

                logger.Log("Changed " + player + "'s color", Logger.MessageType.INFO);
            });

            toggle.addItemListener(ev -> {
                boolean value = (ev.getStateChange() == ItemEvent.SELECTED);
                mainPanel.playerNameEnabledMap.put(player, value);
                mainPanel.update = true;
                mainPanel.repaint();
                toggle.setIcon(value ? radioIconON : radioIconOFF);

                logger.Log((value ? "Showed " : "Hid ") + player + "'s data", Logger.MessageType.INFO);
            });
        }
        tabbedPane.addTab("Player", null, playerPanel, "Player display settings");
        //endregion

        //region Render
        JComponent renderPanel = new JPanel();
        renderPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));

        drawTypeChooser = new JComboBox<>(new Decoder.DrawType[]{Decoder.DrawType.Pixel, Decoder.DrawType.Dot, Decoder.DrawType.Line, Decoder.DrawType.Heat});
//        drawTypeChooser.setPreferredSize(new Dimension(85, 24));
        drawTypeChooser.setSelectedItem(settings._drawType);
        drawTypeChooser.addActionListener(event -> {
            settings._drawType = (Decoder.DrawType) drawTypeChooser.getSelectedItem();
            if (drawSizeComponent != null) {
//                drawSizeComponent.setVisible(settings._drawType == Decoder.DrawType.Line);
                drawSizeComponent.setEnabled(settings._drawType == Decoder.DrawType.Line);
            }
            if (lineThresholdComponent != null) {
//                lineThresholdComponent.setVisible(settings._drawType == Decoder.DrawType.Line);
                lineThresholdComponent.setEnabled(settings._drawType == Decoder.DrawType.Line);
            }
            if (terminusPointsToggle != null) {
//                terminusPointsToggle.setVisible(settings._drawType == Decoder.DrawType.Line);
                terminusPointsToggle.setEnabled(settings._drawType == Decoder.DrawType.Line);
            }

            if (ageFadeToggle != null) {
//                ageFadeToggle.setVisible(settings._drawType != Decoder.DrawType.Heat);
                ageFadeToggle.setEnabled(settings._drawType != Decoder.DrawType.Heat);
            }
            if (ageFadeComponent != null) {
//                ageFadeComponent.setVisible(settings._drawType != Decoder.DrawType.Heat);
                ageFadeComponent.setEnabled(settings._drawType != Decoder.DrawType.Heat);
            }

            if (heatDrawTypeChooser != null) {
//                heatDrawTypeChooser.setVisible(settings._drawType == Decoder.DrawType.Heat);
                heatDrawTypeChooser.setEnabled(settings._drawType == Decoder.DrawType.Heat);
            }
            if (heatMapComponent != null) {
//                heatMapComponent.setVisible(settings._drawType == Decoder.DrawType.Heat);
                heatMapComponent.setEnabled(settings._drawType == Decoder.DrawType.Heat);
            }

//            mainPanel.updatePoints(true);
            sizeTypeLabel.setText((settings._drawType == Decoder.DrawType.Dot) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.Pixel || settings._drawType == Decoder.DrawType.Heat) ? "Pixel Size" : ((settings._drawType == Decoder.DrawType.Line) ? "Line Thickness" : "   -")));
            settings.SaveSettings();
            mainPanel.update = true;
            mainPanel.repaint();

            toolbar.validate();
            toolbar.repaint();

            logger.Log("Changed draw type to: " + settings._drawType, Logger.MessageType.INFO);
        });
        renderPanel.add(drawTypeChooser, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        heatDrawTypeChooser = new JComboBox<>(new HeatDrawType[]{HeatDrawType.Change_Size, HeatDrawType.Change_Color});
//        heatDrawTypeChooser.setPreferredSize(new Dimension(85, 24));
        heatDrawTypeChooser.setSelectedItem(settings._heatDrawType);
        heatDrawTypeChooser.addActionListener(event -> {
            settings._heatDrawType = (HeatDrawType) heatDrawTypeChooser.getSelectedItem();

            settings.SaveSettings();
            mainPanel.update = true;
            mainPanel.repaint();

            logger.Log("Changed heat draw type to: " + settings._heatDrawType, Logger.MessageType.INFO);
        });
        renderPanel.add(heatDrawTypeChooser, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        heatDrawTypeChooser.setEnabled(settings._drawType == Decoder.DrawType.Heat);

        drawSizeComponent = new JPanel();
        renderPanel.add(drawSizeComponent, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        sizeTypeLabel = new JLabel((settings._drawType == Decoder.DrawType.Dot) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.Pixel || settings._drawType == Decoder.DrawType.Heat) ? "Pixel Size" : ((settings._drawType == Decoder.DrawType.Line) ? "Line Thickness" : "-")));
        drawSizeComponent.add(sizeTypeLabel);

        sizeSlider = new JSlider(0, 0, Math.max(0, settings.size > 50 ? (int) (settings.size + (settings.size * 0.1f)) : 50), (int) settings.size * 10);
//        sizeSlider.setPreferredSize(new Dimension(200, 24));
        sizeSlider.setPaintTicks(true);
        sizeSlider.setMajorTickSpacing((int) settings.size / 4);
        sizeSlider.setMinorTickSpacing(10);
        sizeSlider.setPaintLabels(true);
        sizeLabel = new JLabel(Float.toString(settings.size));
        sizeSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            settings.size = (float) source.getValue() / 10.0f;
            sizeLabel.setText(Float.toString(settings.size));
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            logger.Log("Changed draw size to: " + settings.size, Logger.MessageType.INFO);
        });
        drawSizeComponent.add(sizeSlider);
        drawSizeComponent.add(sizeLabel);

        lineThresholdComponent = new JPanel();
        renderPanel.add(lineThresholdComponent, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        lineThresholdTitle = new JLabel("Line Threshold");
        lineThresholdComponent.add(lineThresholdTitle);

        lineThresholdSlider = new JSlider(0, 0, Math.max(0, settings.lineThreshold > 200 ? (int) (settings.lineThreshold + (settings.lineThreshold * 0.1f)) : 200), settings.lineThreshold);
//        lineThresholdSlider.setPreferredSize(new Dimension(200, 24));
        lineThresholdSlider.setPaintTicks(true);
        lineThresholdSlider.setMajorTickSpacing(settings.lineThreshold / 4);
        lineThresholdSlider.setMinorTickSpacing(10);
        lineThresholdSlider.setPaintLabels(false);
        lineThresholdLabel = new JLabel(Integer.toString(settings.lineThreshold));
        lineThresholdSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int threshold = source.getValue();
            settings.lineThreshold = threshold;
            lineThresholdLabel.setText(Integer.toString(threshold));
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            logger.Log("Changed line threshold to: " + settings.lineThreshold, Logger.MessageType.INFO);
        });
        lineThresholdComponent.setEnabled(settings._drawType == Decoder.DrawType.Line);
        lineThresholdComponent.add(lineThresholdSlider);
        lineThresholdComponent.add(lineThresholdLabel);

        heatMapComponent = new JPanel();
        renderPanel.add(heatMapComponent, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        heatMapThresholdTitle = new JLabel("Activity Threshold");
        heatMapThresholdSlider = new JSlider(0, Math.min(-1, settings.heatMapThreshold < -100 ? (int) (settings.heatMapThreshold + (settings.heatMapThreshold * 0.1f)) : -100), Math.max(0, settings.heatMapThreshold > 100 ? (int) (settings.heatMapThreshold + (settings.heatMapThreshold * 0.1f)) : 100), settings.heatMapThreshold);
//        heatMapThresholdSlider.setPreferredSize(new Dimension(150, 24));
        heatMapThresholdSlider.setPaintTicks(true);
        heatMapThresholdSlider.setMajorTickSpacing(settings.heatMapThreshold / 4);
        heatMapThresholdSlider.setMinorTickSpacing(0);
        heatMapThresholdSlider.setPaintLabels(true);
        heatMapThresholdLabel = new JLabel(Integer.toString(settings.heatMapThreshold));
        heatMapThresholdSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int threshold = source.getValue();
            settings.heatMapThreshold = threshold;
            heatMapThresholdLabel.setText(Integer.toString(threshold));

            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            logger.Log("Changed activity threshold to: " + settings.heatMapThreshold, Logger.MessageType.INFO);
        });
        heatMapComponent.setEnabled(settings._drawType == Decoder.DrawType.Heat);
        heatMapComponent.add(heatMapThresholdTitle);
        heatMapComponent.add(heatMapThresholdSlider);
        heatMapComponent.add(heatMapThresholdLabel);

        ageFadeComponent = new JPanel();
        renderPanel.add(ageFadeComponent, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        JLabel ageFadeThresholdTitle = new JLabel("Age Fade Threshold");
        JSlider ageFadeThresholdSlider = new JSlider(0, 0, Math.max(0, settings.ageFadeThreshold > 200 ? (int) (settings.ageFadeThreshold + (settings.ageFadeThreshold * 0.1f)) : 200), settings.ageFadeThreshold);
//        ageFadeThresholdSlider.setPreferredSize(new Dimension(150, 24));
        ageFadeThresholdSlider.setPaintTicks(true);
        ageFadeThresholdSlider.setMajorTickSpacing(settings.ageFadeThreshold / 4);
        ageFadeThresholdSlider.setMinorTickSpacing(10);
        ageFadeThresholdSlider.setPaintLabels(true);
        JLabel ageFadeThresholdLabel = new JLabel(Integer.toString(settings.ageFadeThreshold));

        ageFadeThresholdSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int threshold = source.getValue();
            settings.ageFadeThreshold = threshold;
            ageFadeThresholdLabel.setText(Integer.toString(threshold));
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            logger.Log("Changed age fade threshold to: " + settings.ageFadeThreshold, Logger.MessageType.INFO);
        });
        ageFadeThresholdSlider.setEnabled(settings.ageFade);
        ageFadeThresholdTitle.setEnabled(settings.ageFade);
        ageFadeThresholdLabel.setEnabled(settings.ageFade);
        ageFadeComponent.add(ageFadeThresholdTitle);
        ageFadeComponent.add(ageFadeThresholdSlider);
        ageFadeComponent.add(ageFadeThresholdLabel);

        ageFadeToggle = new JToggleButton("Age Fade", settings.ageFade);
        ageFadeToggle.setIcon(settings.ageFade ? toggleIconON : toggleIconOFF);
//        ageFadeToggle.setPreferredSize(new Dimension(24, 24));
        ageFadeToggle.setMargin(new Insets(2, 2, 2, 2));
        ageFadeToggle.setBorder(BorderFactory.createEmptyBorder());
        renderPanel.add(ageFadeToggle, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        ageFadeToggle.addItemListener(ev -> {
            settings.ageFade = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            ageFadeToggle.setIcon(settings.ageFade ? toggleIconON : toggleIconOFF);
            ageFadeThresholdSlider.setEnabled(settings.ageFade);
            ageFadeThresholdTitle.setEnabled(settings.ageFade);
            ageFadeThresholdLabel.setEnabled(settings.ageFade);

            logger.Log("Toggled age fade to: " + settings.ageFade, Logger.MessageType.INFO);
        });

        fancyLinesToggle = new JToggleButton("Fancy Lines", settings.fancyLines);
        fancyLinesToggle.setIcon(settings.fancyLines ? toggleIconON : toggleIconOFF);
//        fancyLinesToggle.setPreferredSize(new Dimension(24, 24));
        fancyLinesToggle.setMargin(new Insets(2, 2, 2, 2));
        fancyLinesToggle.setBorder(BorderFactory.createEmptyBorder());

        fancyLinesToggle.addItemListener(ev -> {
            settings.fancyLines = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            fancyLinesToggle.setIcon(settings.fancyLines ? toggleIconON : toggleIconOFF);

            logger.Log("Toggled fancy lines to: " + settings.fancyLines, Logger.MessageType.INFO);
        });
        fancyLinesToggle.setEnabled(settings._drawType == Decoder.DrawType.Line);
        renderPanel.add(fancyLinesToggle, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        terminusPointsToggle = new JToggleButton("Terminus Points", settings.terminusPoints);
        terminusPointsToggle.setIcon(settings.terminusPoints ? toggleIconON : toggleIconOFF);
//        terminusPointsToggle.setPreferredSize(new Dimension(24, 24));
        terminusPointsToggle.setMargin(new Insets(2, 2, 2, 2));
        terminusPointsToggle.setBorder(BorderFactory.createEmptyBorder());

        terminusPointsToggle.addItemListener(ev -> {
            settings.terminusPoints = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            terminusPointsToggle.setIcon(settings.terminusPoints ? toggleIconON : toggleIconOFF);

            logger.Log("Toggled terminus points to: " + settings.terminusPoints, Logger.MessageType.INFO);
        });
        terminusPointsToggle.setEnabled(settings._drawType == Decoder.DrawType.Line);
        renderPanel.add(terminusPointsToggle, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        showHiddenLinesToggle = new JToggleButton("Show Hidden Lines", settings.hiddenLines);
        showHiddenLinesToggle.setIcon(settings.hiddenLines ? toggleIconON : toggleIconOFF);
//        showHiddenLinesToggle.setPreferredSize(new Dimension(24, 24));
        showHiddenLinesToggle.setMargin(new Insets(2, 2, 2, 2));
        showHiddenLinesToggle.setBorder(BorderFactory.createEmptyBorder());

        showHiddenLinesToggle.addItemListener(ev -> {
            settings.hiddenLines = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            showHiddenLinesToggle.setIcon(settings.hiddenLines ? toggleIconON : toggleIconOFF);

            logger.Log("Toggled hidden lines to: " + settings.hiddenLines, Logger.MessageType.INFO);
        });
        showHiddenLinesToggle.setEnabled((settings._drawType == Decoder.DrawType.Line));
        renderPanel.add(showHiddenLinesToggle, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

//        EventQueue.invokeLater(() -> {
//            drawSizeComponent.updateUI();
//            lineThresholdComponent.updateUI();
//            ageFadeComponent.updateUI();
//            heatMapComponent.updateUI();
//        });

        tabbedPane.addTab("Render", null, renderPanel, "Rendering settings");
        //endregion

        if (hasBackgroundImage) {
            tabbedPane.insertTab("Background", null, backgroundImagePanel, "World background image settings", 3);
        }

        //region Export
        JComponent exportPanel = new JPanel();
        exportPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JMenuItem exportAsImageButton = new JMenuItem();
        exportAsImageButton.setIcon(exportIcon);
        exportAsImageButton.setPreferredSize(new Dimension(48, 48));
        exportAsImageButton.setMargin(new Insets(2, 2, 2, 2));
        exportAsImageButton.setBorder(BorderFactory.createEmptyBorder());

        exportAsImageButton.addActionListener(event -> mainPanel.SaveAsImage(PlayerTrackerDecoder.this));
        exportPanel.add(exportAsImageButton);

        exportPanel.add(new JLabel("   Up-scaling"));
        JSlider exportUpscaleSlider = new JSlider(0, 1, 6, mainPanel.upscale);
        exportUpscaleSlider.setPaintTicks(true);
        exportUpscaleSlider.setMajorTickSpacing(1);
        exportUpscaleSlider.setMinorTickSpacing(0);
        exportUpscaleSlider.setPaintLabels(true);
        exportUpscaleSlider.setSnapToTicks(true);
        exportUpscaleLabel = new JLabel(Integer.toString(mainPanel.upscale));
        JLabel imageExportStatus = new JLabel("   No export in progress");
        mainPanel.imageExportStatus = imageExportStatus;
        exportUpscaleSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int upscale = source.getValue();
            mainPanel.upscale = upscale;

            exportUpscaleLabel.setText(Integer.toString(mainPanel.upscale));

            logger.Log("Changed export upscale to: " + upscale, Logger.MessageType.INFO);
        });
        exportPanel.add(exportUpscaleSlider);
        exportPanel.add(exportUpscaleLabel);
        exportPanel.add(new JLabel("   WARNING (Don't increase too high!)"));
        exportPanel.add(imageExportStatus);

        tabbedPane.addTab("Export", null, exportPanel, "Export data as an image");
        //endregion

        toolbar.add(tabbedPane);

        revalidate();

        logger.Log("Successfully initialized toolbar subsystem", Logger.MessageType.INFO);
    }

    private void decodeAndDisplay() throws IOException {
        logger.Log("Selected files: " + files.length, Logger.MessageType.INFO);
        logger.Log("Decoding process started", Logger.MessageType.INFO);

        decoder.files = files;
        decoder.main = this;
        (new Thread(decoder)).start();

        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void DisplayDecodedData() {
        mainPanel.setSize(new Dimension(decoder.xRange, decoder.yRange));
        logDates = decoder.logDates;
        startDate = logDates.get(0);
        endDate = logDates.get(logDates.size() - 1);

        mainPanel.startDate = startDate;
        mainPanel.endDate = endDate;
        //mainPanel.selectedDate = selectedDate;
        mainPanel.setData(decoder);
        mainPanel.updatePoints(true);

        initDataSettingsToolBar(alreadyImported);
        toolbar.setVisible(true);
        mainPanel.SelectedEntryLabel.setVisible(true);

        mainPanel.ShouldDraw = true;

        logger.Log("Selected files: " + files.length, Logger.MessageType.INFO);

        alreadyImported = true;

        logger.Log("Decoding process finished successfully", Logger.MessageType.INFO);
    }

    public enum HeatDrawType {
        Change_Size,
        Change_Color
    }
}