import RangedSlider.RangeSlider;

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

    private JLabel sizeLabel;

    private JLabel sizeTypeLabel;

    private JSlider lineThresholdSlider;

    private JLabel lineThresholdLabel;

    private JLabel lineThresholdTitle;

    private JToggleButton fancyLinesToggle;
    private JToggleButton terminusPointsToggle;

    private JToggleButton showHiddenLinesToggle;

    private JLabel exportUpscaleLabel;

    private JLabel xLabel;
    private JLabel yLabel;
    private JLabel backgroundOpacityLabel;

    private File[] files;

    private ArrayList<LocalDateTime> logDates;

    private final Logger logger;

    private JTabbedPane tabbedPane;

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

    private final String version = "1.6.3";

    public PlayerTrackerDecoder() {
        logger = new Logger(version);

        logger.Log("Initializing primary systems", Logger.MessageType.INFO);

        settings = new Settings(logger);
        decoder = new Decoder(settings, logger);
        initMainFrame();

        try {
            logger.Log("Loading resources", Logger.MessageType.INFO);
            playIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/play.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Play");
            pauseIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/pause.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Pause");
            replayIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/replay.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Replay");
            speedIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/fastForward.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Fast Forward");
            importIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/import.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Import");
            exportIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(getClass().getResource("resources/export.png"))).getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH), "Export");
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
            int returnVal = chooser.showOpenDialog(PlayerTrackerDecoder.this);
            files = chooser.getSelectedFiles();
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    decodeAndDisplay();
                } catch (IOException e) {
                    logger.Log("Error decoding the selected input log files:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
                }

                initDataSettingsToolBar(alreadyImported);
                toolbar.setVisible(true);
                mainPanel.SelectedEntryLabel.setVisible(true);

                logger.Log("Selected files: " + files.length, Logger.MessageType.INFO);

                File[] worldImages = new File("worldImages").listFiles();
                if (worldImages == null || worldImages.length == 0) {
                    logger.Log("No world background images present in folder \"WorldImages\"", Logger.MessageType.WARNING);
                } else {
                    logger.Log("World background images are present, opening selection screen", Logger.MessageType.INFO);
                    new Thread(new Runnable() {
                        public void run() {
                            loadWorldImage();
                        }
                    }).start();
                }

                alreadyImported = true;
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                logger.Log("Error selecting input files", Logger.MessageType.ERROR);
            } else {
                logger.Log("No input files selected", Logger.MessageType.WARNING);
            }
        });

        logger.Log("Successfully initialized all subsystems", Logger.MessageType.INFO);
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
        mainPanel.CoordinateLabel.setText("(0, 0) | ");
        mainPanel.SelectedEntryLabel = new JLabel("Nothing Selected");
        mainPanel.SelectedEntryLabel.setVisible(false);
        scrollPane.setDoubleBuffered(true);
        add(scrollPane);
        add(mainPanel);
        mainPanel.setVisible(true);
        revalidate();

        logger.Log("Successfully initialized primary frame subsystem", Logger.MessageType.INFO);
    }

    private void loadWorldImage() {
        logger.Log("World background image is present, loading world background image", Logger.MessageType.INFO);

        JFileChooser imgChooser = new JFileChooser("worldImages");
        imgChooser.setMultiSelectionEnabled(false);
        imgChooser.addChoosableFileFilter(new ImageFileFilter());
        imgChooser.setAcceptAllFileFilterUsed(false);

        int returnVal = imgChooser.showOpenDialog(PlayerTrackerDecoder.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            logger.Log("Selected world background images: " + files.length, Logger.MessageType.INFO);

            try {
                File imgFile = imgChooser.getSelectedFile();
                mainPanel.backgroundImage = mainPanel.LoadBackgroundImage(imgFile);

                JComponent backgroundImagePanel = new JPanel();
                backgroundImagePanel.setLayout(new GridLayout(3, 5));

                backgroundImagePanel.add(new JLabel("World Background Image Offset:   "));

                int width = mainPanel.backgroundImage.getWidth();
                int height = mainPanel.backgroundImage.getHeight();
                int defaultX = imgFile.getName().contains("Nether") ? -1008 : -6384;
                int defaultY = imgFile.getName().contains("Nether") ? -1969 : -5376;

                backgroundImagePanel.add(new JLabel("Overworld offset: (-6384, -5376) Nether offset: (-1008, -1969)"));

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

                JSlider yOffsetSlider = new JSlider(0, -height, height, defaultY);
                yOffsetSlider.setPreferredSize(new Dimension(100, 48));
                yOffsetSlider.setPaintTicks(false);
                yOffsetSlider.setMajorTickSpacing(1);
                yOffsetSlider.setMinorTickSpacing(0);
                yOffsetSlider.setPaintLabels(false);
                mainPanel.yBackgroundOffset = defaultY;
                yLabel = new JLabel("Y Offset: " + defaultY);
                yOffsetSlider.addChangeListener(e -> {
                    JSlider source = (JSlider) e.getSource();
                    int y = source.getValue();
                    yLabel.setText("Y Offset: " + y);
                    mainPanel.yBackgroundOffset = y;
                    mainPanel.update = true;
                    mainPanel.repaint();

                    logger.Log("Changed the world background image Y offset to: " + y, Logger.MessageType.INFO);
                });
                backgroundImagePanel.add(yOffsetSlider);
                backgroundImagePanel.add(yLabel);

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

                tabbedPane.insertTab("Background", null, backgroundImagePanel, "World background image", 3);

                logger.Log("Successfully loaded world background image", Logger.MessageType.INFO);
            } catch (IOException e) {
                logger.Log("Error reading selected world background image:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
            }
        } else if (returnVal == JFileChooser.ERROR_OPTION) {
            logger.Log("Error selecting world background images", Logger.MessageType.ERROR);
        } else {
            logger.Log("No world background images selected", Logger.MessageType.WARNING);
        }
    }

    private void initDataSettingsToolBar(boolean remove) {
        logger.Log("Initializing toolbar subsystem", Logger.MessageType.INFO);

//        dataSettingsToolBar.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        if (remove) toolbar.removeAll();
        toolbar.setDoubleBuffered(true);

        tabbedPane = new JTabbedPane();
        toolbar.add(tabbedPane);

        //region Data
        JComponent dataPanel = new JPanel();
//        dataPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        dataPanel.add(new JLabel("Dates To Represent:   "));
        dateRangeSlider = new RangeSlider(0, logDates.size() - 1);
        dateRangeSlider.setPreferredSize(new Dimension(600, 25));
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

        tabbedPane.addTab("Data", null, dataPanel, "Data settings");
        //endregion

        //region Players
        JComponent playerPanel = new JPanel();
        playerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        playerPanel.add(new JLabel("Players"));
        for (String player : mainPanel.playerNameColorMap.keySet()) {
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
        tabbedPane.addTab("Player", null, playerPanel, "Player settings");
        //endregion

        //region Render
        JComponent renderPanel = new JPanel();
        renderPanel.setLayout(new GridLayout(2, 5));

        drawTypeChooser = new JComboBox<>(new Decoder.DrawType[]{Decoder.DrawType.PIXEL, Decoder.DrawType.DOT, Decoder.DrawType.LINE});
        drawTypeChooser.setPreferredSize(new Dimension(85, 24));
        drawTypeChooser.setSelectedItem(settings._drawType);
        drawTypeChooser.addActionListener(event -> {
            settings._drawType = (Decoder.DrawType) drawTypeChooser.getSelectedItem();
            if (fancyLinesToggle != null)
                fancyLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);
            if (showHiddenLinesToggle != null)
                showHiddenLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);
            if (lineThresholdSlider != null)
                lineThresholdSlider.setVisible(settings._drawType == Decoder.DrawType.LINE);
            if (lineThresholdLabel != null)
                lineThresholdLabel.setVisible(settings._drawType == Decoder.DrawType.LINE);
            if (lineThresholdTitle != null)
                lineThresholdTitle.setVisible(settings._drawType == Decoder.DrawType.LINE);
            if (terminusPointsToggle != null)
                terminusPointsToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);

            mainPanel.updatePoints(true);
            sizeTypeLabel.setText((settings._drawType == Decoder.DrawType.DOT) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.PIXEL) ? "Square Size" : ((settings._drawType == Decoder.DrawType.LINE) ? "Line Thickness" : "   -")));
            settings.SaveSettings();
            mainPanel.update = true;
            mainPanel.repaint();

            logger.Log("Changed draw type to: " + settings._drawType, Logger.MessageType.INFO);
        });
        renderPanel.add(drawTypeChooser);
        sizeTypeLabel = new JLabel((settings._drawType == Decoder.DrawType.DOT) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.PIXEL) ? "Square Size" : ((settings._drawType == Decoder.DrawType.LINE) ? "Line Thickness" : "   -")));
        renderPanel.add(sizeTypeLabel);

        JSlider sizeSlider = new JSlider(0, 0, settings.size > 50 ? (int) (settings.size + (settings.size * 0.1f)) : 50, settings.size);
        sizeSlider.setPreferredSize(new Dimension(200, 48));
        sizeSlider.setPaintTicks(true);
        sizeSlider.setMajorTickSpacing(10);
        sizeSlider.setMinorTickSpacing(0);
        sizeSlider.setPaintLabels(true);
        sizeLabel = new JLabel(Integer.toString(settings.size));
        sizeSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int radius = source.getValue();
            settings.size = radius;
            sizeLabel.setText(Integer.toString(radius));
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            logger.Log("Changed draw size to: " + settings.size, Logger.MessageType.INFO);
        });
        renderPanel.add(sizeSlider);
        renderPanel.add(sizeLabel);

        lineThresholdTitle = new JLabel("   Line Threshold");
        renderPanel.add(lineThresholdTitle);

        lineThresholdSlider = new JSlider(0, 0, settings.lineThreshold > 200 ? (int) (settings.lineThreshold + (settings.lineThreshold * 0.1f)) : 200, settings.lineThreshold);
        lineThresholdSlider.setPreferredSize(new Dimension(200, 48));
        lineThresholdSlider.setPaintTicks(true);
        lineThresholdSlider.setMajorTickSpacing(settings.lineThreshold / 4);
        lineThresholdSlider.setMinorTickSpacing(0);
        lineThresholdSlider.setPaintLabels(true);
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
        renderPanel.add(lineThresholdSlider);
        renderPanel.add(lineThresholdLabel);
        lineThresholdSlider.setVisible((settings._drawType == Decoder.DrawType.LINE));
        lineThresholdLabel.setVisible((settings._drawType == Decoder.DrawType.LINE));
        lineThresholdTitle.setVisible((settings._drawType == Decoder.DrawType.LINE));

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
        renderPanel.add(fancyLinesToggle);
        fancyLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);

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
        renderPanel.add(terminusPointsToggle);
        terminusPointsToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);

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
        renderPanel.add(showHiddenLinesToggle);
        showHiddenLinesToggle.setVisible((settings._drawType == Decoder.DrawType.LINE));

        JToggleButton ageFadeToggle = new JToggleButton("Age Fade", settings.ageFade);
        ageFadeToggle.setIcon(settings.ageFade ? toggleIconON : toggleIconOFF);
//        ageFadeToggle.setPreferredSize(new Dimension(24, 24));
        ageFadeToggle.setMargin(new Insets(2, 2, 2, 2));
        ageFadeToggle.setBorder(BorderFactory.createEmptyBorder());
        renderPanel.add(ageFadeToggle);

        JLabel ageFadeThresholdTitle = new JLabel("   Age Fade Threshold");
        JSlider ageFadeThresholdSlider = new JSlider(0, 0, settings.ageFadeThreshold > 200 ? (int) (settings.ageFadeThreshold + (settings.ageFadeThreshold * 0.1f)) : 200, settings.ageFadeThreshold);
        ageFadeThresholdSlider.setPreferredSize(new Dimension(150, 48));
        ageFadeThresholdSlider.setPaintTicks(true);
        ageFadeThresholdSlider.setMajorTickSpacing(settings.ageFadeThreshold / 4);
        ageFadeThresholdSlider.setMinorTickSpacing(0);
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
        ageFadeThresholdSlider.setVisible(settings.ageFade);
        ageFadeThresholdTitle.setVisible(settings.ageFade);
        ageFadeThresholdLabel.setVisible(settings.ageFade);

        ageFadeToggle.addItemListener(ev -> {
            settings.ageFade = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();

            ageFadeToggle.setIcon(settings.ageFade ? toggleIconON : toggleIconOFF);
            ageFadeThresholdSlider.setVisible(settings.ageFade);
            ageFadeThresholdTitle.setVisible(settings.ageFade);
            ageFadeThresholdLabel.setVisible(settings.ageFade);

            logger.Log("Toggled age fade to: " + settings.ageFade, Logger.MessageType.INFO);
        });
        renderPanel.add(ageFadeThresholdTitle);
        renderPanel.add(ageFadeThresholdSlider);
        renderPanel.add(ageFadeThresholdLabel);

        tabbedPane.addTab("Render", null, renderPanel, "Render settings");
        //endregion

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

        tabbedPane.addTab("Export", null, exportPanel, "Export as an image");
        //endregion

        toolbar.add(tabbedPane);

        revalidate();

        logger.Log("Successfully initialized toolbar subsystem", Logger.MessageType.INFO);
    }

    private void decodeAndDisplay() throws IOException {
        logger.Log("Decoding process started", Logger.MessageType.INFO);

        decoder.Decode(files);
        mainPanel.setSize(new Dimension(decoder.xRange, decoder.yRange));
        logDates = decoder.logDates;
        startDate = logDates.get(0);
        endDate = logDates.get(logDates.size() - 1);

        mainPanel.startDate = startDate;
        mainPanel.endDate = endDate;
        //mainPanel.selectedDate = selectedDate;
        mainPanel.setData(decoder);
        mainPanel.updatePoints(true);

        logger.Log("Decoding process finished successfully", Logger.MessageType.INFO);
    }
}