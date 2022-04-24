import RangedSlider.RangeSlider;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class PlayerTrackerDecoder extends JFrame {
    private final JFileChooser chooser;

    private final JMenuBar menuBar;

    private final JMenuBar bottomMenuBar;

    private final JMenu dataFileMenu;

    private final JMenuItem dataFileImportButton;

    private final JToolBar dataSettingsToolBar;

    private final Decoder decoder;

    private final Settings settings;

    public Panel mainPanel;

    public JScrollPane scrollPane;

    private boolean alreadyImported = false;


    //private JToggleButton singleDayToggle;

    private RangeSlider dateRangeSlider;

    private JLabel startDateLabel;
    private JLabel endDateLabel;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    //private JComboBox<LocalDateTime> dateStartChooser;

    //private JComboBox<LocalDateTime> dateEndChooser;

    //public LocalDateTime startDate;

    //public LocalDateTime endDate;

    //private JToggleButton singleTimeToggle;

    //private JSlider singleTimeSlider;

    //private JLabel selectedSingleTimeLabel;

    //private int selectedHour;

    //private JSpinner timeStartSpinner;

    //private JSpinner timeEndSpinner;

    //public int startHour;

    //public int endHour;

    private JComboBox<Decoder.DrawType> drawTypeChooser;

    private ArrayList<JMenuItem> playerColorButtons;
    private ArrayList<JToggleButton> playerColorToggles;

    private JSlider sizeSlider;

    private JLabel sizeLabel;

    private JLabel sizeTypeLabel;

    private JSlider lineThresholdSlider;

    private JLabel lineThresholdLabel;

    private JLabel lineThresholdTitle;

    private JToggleButton fancyLinesToggle;

    private JToggleButton showHiddenLinesToggle;

    private JMenuItem exportAsImageButton;

    private JSlider exportUpscaleSlider;

    private JLabel exportUpscaleLabel;

    private JLabel imageExportStatus;

    private JSlider xOffsetSlider;
    private JLabel xLabel;
    private JSlider yOffsetSlider;
    private JLabel yLabel;
    private JSlider backgroundOpacitySlider;
    private JLabel backgroundOpacityLabel;

    private File[] files;

    private ArrayList<LocalDateTime> logDates;

    private Logger logger;

    private JTabbedPane tabbedPane;

  /* private Integer[] hours = new Integer[] { 
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 
      11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 
      21, 22, 23, 24 };
   
      private Integer[] minutes = new Integer[] { 
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 
      11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 
      21, 22, 23, 24 };*/

    private final String version = "1.5.5";

    public PlayerTrackerDecoder() {
        logger = new Logger(version);

        logger.Log("Initializing primary systems", Logger.MessageType.INFO);

        settings = new Settings(logger);
        decoder = new Decoder(settings, logger);
        initMainFrame();

        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("inputs"));
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(new TextFileFilter());
        chooser.setAcceptAllFileFilterUsed(false);

        menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setJMenuBar(menuBar);

        dataFileMenu = new JMenu("Data");
        menuBar.add(dataFileMenu);

        dataFileImportButton = new JMenuItem("Import");
        dataFileMenu.add(dataFileImportButton);

        dataSettingsToolBar = new JToolBar("Data Settings");
        dataSettingsToolBar.setVisible(false);
        menuBar.add(dataSettingsToolBar);

        bottomMenuBar = new JMenuBar();
        add(bottomMenuBar, "South");

        JLabel label = new JLabel();
        mainPanel.RenderedPointsLabel = label;
        bottomMenuBar.add(label);
        bottomMenuBar.add(mainPanel.CoordinateLabel);
        bottomMenuBar.add(mainPanel.SelectedEntryLabel);

        dataFileImportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int returnVal = chooser.showOpenDialog(PlayerTrackerDecoder.this);
                files = chooser.getSelectedFiles();
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        decodeAndDisplay();
                    } catch (IOException e) {
                        logger.Log("Error decoding the selected input log files:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
                    }

                    initDataSettingsToolBar(alreadyImported);
                    dataSettingsToolBar.setVisible(true);
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
            }
        });

        logger.Log("Successfully initialized all subsystems", Logger.MessageType.INFO);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                PlayerTrackerDecoder myFrame = new PlayerTrackerDecoder();
                myFrame.setVisible(true);
            }
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
        setDefaultCloseOperation(3);
        setSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());
        setLocationRelativeTo((Component) null);
        getContentPane().setBackground(Color.DARK_GRAY);
        mainPanel = new Panel(settings, logger);
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
                backgroundImagePanel.setLayout(new GridLayout(0, 2));

                backgroundImagePanel.add(new JLabel("World Background Image Offset:   "));

                int width = mainPanel.backgroundImage.getWidth();
                int height = mainPanel.backgroundImage.getHeight();
                int defaultX = imgFile.getName().contains("Nether") ? -1008 : -6384;
                int defaultY = imgFile.getName().contains("Nether") ? -1969 : -5376;

                backgroundImagePanel.add(new JLabel("Overworld offset: (-6384, -5376) Nether offset: (-1008, -1969)"));

                xOffsetSlider = new JSlider(0, -width, width, defaultX);
                xOffsetSlider.setPreferredSize(new Dimension(100, 10));
                xOffsetSlider.setPaintTicks(false);
                xOffsetSlider.setMajorTickSpacing(1);
                xOffsetSlider.setMinorTickSpacing(0);
                xOffsetSlider.setPaintLabels(false);
                mainPanel.xBackgroundOffset = defaultX;
                xLabel = new JLabel("X Offset: " + Integer.toString(defaultX));
                xOffsetSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider) e.getSource();
                        int x = source.getValue();
                        xLabel.setText("X Offset: " + Integer.toString(x));
                        mainPanel.xBackgroundOffset = x;
                        mainPanel.update = true;
                        mainPanel.repaint();

                        logger.Log("Changed the world background image X offset to: " + x, Logger.MessageType.INFO);
                    }
                });
                backgroundImagePanel.add(xOffsetSlider);
                backgroundImagePanel.add(xLabel);

                yOffsetSlider = new JSlider(0, -height, height, defaultY);
                yOffsetSlider.setPaintTicks(false);
                yOffsetSlider.setMajorTickSpacing(1);
                yOffsetSlider.setMinorTickSpacing(0);
                yOffsetSlider.setPaintLabels(false);
                mainPanel.yBackgroundOffset = defaultY;
                yLabel = new JLabel("Y Offset: " + Integer.toString(defaultY));
                yOffsetSlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider) e.getSource();
                        int y = source.getValue();
                        yLabel.setText("Y Offset: " + Integer.toString(y));
                        mainPanel.yBackgroundOffset = y;
                        mainPanel.update = true;
                        mainPanel.repaint();

                        logger.Log("Changed the world background image Y offset to: " + y, Logger.MessageType.INFO);
                    }
                });
                backgroundImagePanel.add(yOffsetSlider);
                backgroundImagePanel.add(yLabel);

                backgroundOpacitySlider = new JSlider(0, 0, 100, 50);
                backgroundOpacitySlider.setPaintTicks(true);
                backgroundOpacitySlider.setMajorTickSpacing(10);
                backgroundOpacitySlider.setMinorTickSpacing(5);
                backgroundOpacitySlider.setPaintLabels(true);
                backgroundOpacityLabel = new JLabel("Opacity: " + Float.toString(0.5F));
                backgroundOpacitySlider.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        JSlider source = (JSlider) e.getSource();
                        int opacity = source.getValue();
                        backgroundOpacityLabel.setText("Opacity: " + Float.toString(opacity / 100.0F));
                        mainPanel.backgroundOpacity = opacity / 100.0F;
                        mainPanel.update = true;
                        mainPanel.repaint();

                        logger.Log("Changed the world background image opacity to: " + opacity, Logger.MessageType.INFO);
                    }
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

        dataSettingsToolBar.setLayout(new FlowLayout(1, 5, 5));

        if (remove) dataSettingsToolBar.removeAll();

        tabbedPane = new JTabbedPane();
        dataSettingsToolBar.add(tabbedPane);

        //region Data
        JComponent dataPanel = new JPanel();
        dataPanel.setLayout(new FlowLayout(1, 5, 5));

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
        dateRangeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                RangeSlider source = (RangeSlider) e.getSource();
                int value1 = source.getValue();
                int value2 = source.getUpperValue();
                startDate = logDates.get(value1);
                endDate = logDates.get(value2);
                mainPanel.startDate = startDate;
                mainPanel.endDate = endDate;
                startDateLabel.setText(startDate.toString().replace("T", "; "));
                endDateLabel.setText(endDate.toString().replace("T", "; "));
                mainPanel.updatePoints();

                logger.Log("Changed date range slider: From " + startDate.toString() + " to " + endDate.toString(), Logger.MessageType.INFO);
            }
        });
        dataPanel.add(startDateLabel);
        dataPanel.add(dateRangeSlider);
        dataPanel.add(endDateLabel);

        tabbedPane.addTab("Data", null, dataPanel, "Data settings");
        //endregion

        //region Players
        JComponent playerPanel = new JPanel();
        playerPanel.setLayout(new FlowLayout(1, 5, 5));

        playerPanel.add(new JLabel("Players"));
        playerColorButtons = new ArrayList<>();
        playerColorToggles = new ArrayList<>();
        for (String player : mainPanel.playerNameColorMap.keySet()) {
            JMenuItem colButton = new JMenuItem(player);
            JToggleButton toggle = new JToggleButton("", mainPanel.playerNameEnabledMap.get(player));
            colButton.setForeground(mainPanel.playerNameColorMap.get(player));
            playerColorButtons.add(colButton);
            playerColorToggles.add(toggle);
            playerPanel.add(colButton);
            playerPanel.add(toggle);

            colButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Color selectedColor = JColorChooser.showDialog(PlayerTrackerDecoder.this, "Select player color", mainPanel.playerNameColorMap.get(player));
                    colButton.setForeground(selectedColor);
                    mainPanel.playerNameColorMap.put(player, selectedColor);
                    mainPanel.update = true;
                    mainPanel.repaint();

                    logger.Log("Changed " + player + "'s color", Logger.MessageType.INFO);
                }
            });

            toggle.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent ev) {
                    Boolean value = (ev.getStateChange() == 1);
                    mainPanel.playerNameEnabledMap.put(player, value);
                    mainPanel.update = true;
                    mainPanel.repaint();

                    logger.Log((value ? "Showed " : "Hid ") + player + "'s data", Logger.MessageType.INFO);
                }
            });
        }
        tabbedPane.addTab("Player", null, playerPanel, "Player settings");
        //endregion

        //region Render
        JComponent renderPanel = new JPanel();
        renderPanel.setLayout(new FlowLayout(1, 5, 5));

        drawTypeChooser = new JComboBox<>(new Decoder.DrawType[]{Decoder.DrawType.PIXEL, Decoder.DrawType.DOT, Decoder.DrawType.LINE});
        drawTypeChooser.setSelectedItem(settings._drawType);
        drawTypeChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                settings._drawType = (Decoder.DrawType) drawTypeChooser.getSelectedItem();
                if (fancyLinesToggle != null)
                    fancyLinesToggle.setVisible((settings._drawType == Decoder.DrawType.LINE));
                if (showHiddenLinesToggle != null)
                    showHiddenLinesToggle.setVisible((settings._drawType == Decoder.DrawType.LINE));
                if (lineThresholdSlider != null)
                    lineThresholdSlider.setVisible((settings._drawType == Decoder.DrawType.LINE));
                if (lineThresholdLabel != null)
                    lineThresholdLabel.setVisible((settings._drawType == Decoder.DrawType.LINE));
                if (lineThresholdTitle != null)
                    lineThresholdTitle.setVisible((settings._drawType == Decoder.DrawType.LINE));

                mainPanel.updatePoints();
                sizeTypeLabel.setText((settings._drawType == Decoder.DrawType.DOT) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.PIXEL) ? "Square Size" : ((settings._drawType == Decoder.DrawType.LINE) ? "Line Thickness" : "   -")));
                settings.SaveSettings();
                mainPanel.update = true;
                mainPanel.repaint();

                logger.Log("Changed draw type to: " + settings._drawType, Logger.MessageType.INFO);
            }
        });
        renderPanel.add(drawTypeChooser);
        sizeTypeLabel = new JLabel((settings._drawType == Decoder.DrawType.DOT) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.PIXEL) ? "Square Size" : ((settings._drawType == Decoder.DrawType.LINE) ? "Line Thickness" : "   -")));
        renderPanel.add(sizeTypeLabel);

        sizeSlider = new JSlider(0, 0, settings.size > 50 ? (int) (settings.size + (settings.size * 0.1f)) : 50, settings.size);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setMajorTickSpacing(settings.size / 10);
        sizeSlider.setMinorTickSpacing(0);
        sizeSlider.setPaintLabels(true);
        sizeLabel = new JLabel(Integer.toString(settings.size));
        sizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int radius = source.getValue();
                settings.size = radius;
                sizeLabel.setText(Integer.toString(radius));
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();

                logger.Log("Changed draw size to: " + settings.size, Logger.MessageType.INFO);
            }
        });
        renderPanel.add(sizeSlider);
        renderPanel.add(sizeLabel);

        lineThresholdTitle = new JLabel("   Line Threshold");
        renderPanel.add(lineThresholdTitle);
        lineThresholdSlider = new JSlider(0, 0, settings.lineThreshold > 200 ? (int) (settings.lineThreshold + (settings.lineThreshold * 0.1f)) : 200, settings.lineThreshold);
        lineThresholdSlider.setPaintTicks(true);
        lineThresholdSlider.setMajorTickSpacing(settings.lineThreshold / 4);
        lineThresholdSlider.setMinorTickSpacing(0);
        lineThresholdSlider.setPaintLabels(true);
        lineThresholdLabel = new JLabel(Integer.toString(settings.lineThreshold));
        lineThresholdSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int threshold = source.getValue();
                settings.lineThreshold = threshold;
                lineThresholdLabel.setText(Integer.toString(threshold));
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();

                logger.Log("Changed line threshold to: " + settings.lineThreshold, Logger.MessageType.INFO);
            }
        });
        renderPanel.add(lineThresholdSlider);
        renderPanel.add(lineThresholdLabel);
        lineThresholdSlider.setVisible((settings._drawType == Decoder.DrawType.LINE));
        lineThresholdLabel.setVisible((settings._drawType == Decoder.DrawType.LINE));
        lineThresholdTitle.setVisible((settings._drawType == Decoder.DrawType.LINE));

        fancyLinesToggle = new JToggleButton("Fancy Lines", settings.fancyLines);
        fancyLinesToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                settings.fancyLines = (ev.getStateChange() == 1);
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();

                logger.Log("Toggled fancy lines to: " + settings.fancyLines, Logger.MessageType.INFO);
            }
        });
        renderPanel.add(fancyLinesToggle);
        fancyLinesToggle.setVisible((settings._drawType == Decoder.DrawType.LINE));

        showHiddenLinesToggle = new JToggleButton("Show Hidden Lines", settings.hiddenLines);
        showHiddenLinesToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                settings.hiddenLines = (ev.getStateChange() == 1);
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();

                logger.Log("Toggled hidden lines to: " + settings.hiddenLines, Logger.MessageType.INFO);
            }
        });
        renderPanel.add(showHiddenLinesToggle);
        showHiddenLinesToggle.setVisible((settings._drawType == Decoder.DrawType.LINE));
        tabbedPane.addTab("Render", null, renderPanel, "Render settings");
        //endregion

        //region Export
        JComponent exportPanel = new JPanel();
        exportPanel.setLayout(new FlowLayout(1, 5, 5));

        exportAsImageButton = new JMenuItem("Export As Image");
        exportAsImageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mainPanel.SaveAsImage(PlayerTrackerDecoder.this);
            }
        });
        exportPanel.add(exportAsImageButton);

        exportPanel.add(new JLabel("   Upscaling"));
        exportUpscaleSlider = new JSlider(0, 1, 6, mainPanel.upscale);
        exportUpscaleSlider.setPaintTicks(true);
        exportUpscaleSlider.setMajorTickSpacing(1);
        exportUpscaleSlider.setMinorTickSpacing(0);
        exportUpscaleSlider.setPaintLabels(true);
        exportUpscaleSlider.setSnapToTicks(true);
        exportUpscaleLabel = new JLabel(Integer.toString(mainPanel.upscale));
        imageExportStatus = new JLabel("   No export in progress");
        mainPanel.imageExportStatus = imageExportStatus;
        exportUpscaleSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int upscale = source.getValue();
                mainPanel.upscale = upscale;

                exportUpscaleLabel.setText(Integer.toString(mainPanel.upscale));

                logger.Log("Changed export upscale to: " + upscale, Logger.MessageType.INFO);
            }
        });
        exportPanel.add(exportUpscaleSlider);
        exportPanel.add(exportUpscaleLabel);
        exportPanel.add(new JLabel("   WARNING (Don't increase too high!)"));
        exportPanel.add(imageExportStatus);

        tabbedPane.addTab("Export", null, exportPanel, "Export as an image");
        //endregion

        dataSettingsToolBar.add(tabbedPane);

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
        //startHour = 1;
        //endHour = 23;
        mainPanel.startDate = startDate;
        mainPanel.endDate = endDate;
        //mainPanel.selectedDate = selectedDate;
        mainPanel.setData(decoder);
        mainPanel.updatePoints();

        logger.Log("Decoding process finished successfully", Logger.MessageType.INFO);
    }
}