package main;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.version.MCVersion;
import config.Settings;
import config.mapping.Configs;
import IO.filters.TextFileFilter;
import util.Assets;
import ui.HelpForm;
import ui.RangedSlider.RangeSlider;
import ui.ImportForm;
import ui.SettingsForm;
import util.Logger;
import util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlayerTrackerDecoder extends JFrame {
    public static PlayerTrackerDecoder INSTANCE;

    public static final String VERSION = "1.3.2-FR";
    public static String[] BUILD_INFO = new String[]{"NULL", "NULL", "NULL", "NULL", "NULL"};

    public static final String DIR_ROOT = System.getProperty("user.dir");
    public final static String DIR_LOGS = DIR_ROOT + File.separatorChar + "logs";
    public final static String DIR_INPUTS = DIR_ROOT + File.separatorChar + "inputs";
    public final static String DIR_OUTPUTS = DIR_ROOT + File.separatorChar + "outputs";
    public final static String DIR_WORLDIMAGES = DIR_ROOT + File.separatorChar + "worldImages";
    public final static String DIR_CONFIG = DIR_ROOT + File.separatorChar + "configs";
    public final static String DIR_DL = DIR_ROOT + File.separatorChar + ".downloads";

    public static boolean DEBUG = true;

    public final Settings settings;

    public MainPanel mainPanel;

    private JMenuBar menuBar;
    private JMenuItem dataFileImportButton;
    private JMenuItem settingsButton;
    private JMenuItem helpButton;
    private final JToolBar toolbar;
    private JTabbedPane tabbedPane;

    private boolean alreadyImported = false;
    private boolean hasBackgroundImage = false;

    //region UI elements
    public RangeSlider timeRangeSlider;
    public JLabel startTimeLabel;
    public JLabel endTimeLabel;
    public JToggleButton animatePlayPause;

    private JComboBox<DrawType> drawTypeChooser;
    private JComboBox<HeatDrawType> heatDrawTypeChooser;

    private JPanel drawSizeComponent;
    private JLabel drawSizeTitle;
    private JSlider drawSizeSlider;
    private JLabel drawSizeLabel;

    private JPanel lineThresholdComponent;
    private JLabel lineThresholdTitle;
    private JLabel lineThresholdLabel;
    private JSlider lineThresholdSlider;

    private JPanel ageFadeComponent;
    private JToggleButton ageFadeToggle;
    private JLabel ageFadeStrengthTitle;
    private JSlider ageFadeStrengthSlider;
    private JLabel ageFadeStrengthLabel;

    private JPanel heatMapComponent;
    private JLabel heatMapStrengthTitle;
    private JSlider heatMapStrengthSlider;
    private JLabel heatMapStrengthLabel;

    private JToggleButton fancyLinesToggle;
    private JToggleButton terminusPointsToggle;
    private JToggleButton showHiddenLinesToggle;

    private JButton exportAsImageButton;
    private JButton screenshotButton;
    private JLabel exportUpscaleLabel;
    private JLabel xLabel;
    private JLabel zLabel;
    private JLabel backgroundOpacityLabel;
    private JPanel backgroundImagePanel;

    //region Player pages
    private JLabel playerPageLabel;
    private int currentPlayerPageIndex = 0;
    private int PLAYER_PAGE_CAPACITY = 12;
    //endregion
    //endregion

    private ImportForm importForm;
    private SettingsForm settingsForm;
    private HelpForm helpForm;

    //region Icons
    public static ImageIcon playIcon_L;
    public static ImageIcon playIcon_D;
    public static ImageIcon replayIcon_L;
    public static ImageIcon replayIcon_D;
    public static ImageIcon speedIcon_L;
    public static ImageIcon speedIcon_D;
    public static ImageIcon pauseIcon_L;
    public static ImageIcon pauseIcon_D;
    public static ImageIcon importIcon_L;
    public static ImageIcon importIcon_D;
    public static ImageIcon exportIcon_L;
    public static ImageIcon exportIcon_D;
    public static ImageIcon screenshotIcon_L;
    public static ImageIcon screenshotIcon_D;
    public static ImageIcon toggleIconON_L;
    public static ImageIcon toggleIconON_D;
    public static ImageIcon toggleIconOFF_L;
    public static ImageIcon toggleIconOFF_D;
    public static ImageIcon settingsIcon_L;
    public static ImageIcon settingsIcon_D;
    public static ImageIcon helpIcon_L;
    public static ImageIcon helpIcon_D;
    public static ImageIcon lightThemeIcon;
    public static ImageIcon darkThemeIcon;
    //endregion

    public PlayerTrackerDecoder(boolean debug) {
        DEBUG = debug;
        String[] buildInfo = Assets.getCurrentBuildInfo();
        if (buildInfo != null)
            BUILD_INFO = buildInfo;

        Logger.info("Initializing primary subsystems");
        settings = new Settings();

        loadResources();

        initMainFrame();

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(PlayerTrackerDecoder.DIR_INPUTS));
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(new TextFileFilter());
        chooser.setAcceptAllFileFilterUsed(false);

        menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);

        JPanel menuButtons = new JPanel();
        menuBar.add(menuButtons, BorderLayout.WEST);

        dataFileImportButton = new JMenuItem("");
        dataFileImportButton.setToolTipText("Import Data");
        dataFileImportButton.setIcon(importIcon_L);
        dataFileImportButton.setPreferredSize(new Dimension(48, 48));
        dataFileImportButton.setMinimumSize(new Dimension(47, 47));
        menuButtons.add(dataFileImportButton);
        dataFileImportButton.addActionListener((event) -> {
            hasBackgroundImage = false;
            mainPanel.isPlaying = false;
            mainPanel.shouldDraw = false;
            mainPanel.backgroundImage = null;
            if (importForm != null) {
                importForm.setVisible(false);
                importForm = null;
            }

            importForm = new ImportForm(this, settings, alreadyImported);
            importForm.setLocationRelativeTo(this);
            importForm.setVisible(true);
        });

        settingsButton = new JMenuItem("");
        settingsButton.setToolTipText("Open Settings Pane");
        settingsButton.setIcon(settingsIcon_L);
        settingsButton.setPreferredSize(new Dimension(48, 48));
        settingsButton.setMinimumSize(new Dimension(47, 47));
        menuButtons.add(settingsButton);
        settingsButton.addActionListener((event) -> {
            mainPanel.isPlaying = false;
            if (animatePlayPause != null) animatePlayPause.setEnabled(false);

            if (settingsForm != null) {
                settingsForm.setVisible(false);
                settingsForm = null;
            }

            settingsForm = new SettingsForm(this, settings);
            settingsForm.setLocationRelativeTo(this);
            settingsForm.setVisible(true);
        });

        helpButton = new JMenuItem(helpIcon_L);
        helpButton.setToolTipText("Open Help Pane");
        helpButton.setPreferredSize(new Dimension(48, 48));
        helpButton.setMinimumSize(new Dimension(47, 47));
        menuButtons.add(helpButton);
        helpButton.addActionListener((event) -> {
            mainPanel.isPlaying = false;
            if (animatePlayPause != null) animatePlayPause.setEnabled(false);

            if (helpForm != null) {
                helpForm.setVisible(false);
                helpForm = null;
            }

            helpForm = new HelpForm(this);
            helpForm.setLocationRelativeTo(this);
            helpForm.setVisible(true);
        });

        toolbar = new JToolBar("Toolbar");
        toolbar.setVisible(false);
        toolbar.setCursor(null);

        menuBar.add(toolbar, BorderLayout.CENTER);
        JMenuBar bottomMenuBar = new JMenuBar();
        add(bottomMenuBar, BorderLayout.SOUTH);

        JLabel renderInfoLabel = new JLabel();
        mainPanel.renderedPointsLabel = renderInfoLabel;
        bottomMenuBar.add(renderInfoLabel);
        bottomMenuBar.add(mainPanel.coordinateLabel);
        bottomMenuBar.add(mainPanel.selectedEntryLabel);

        ChangeTheme(settings.uiTheme);
        Logger.info("Successfully initialized all subsystems");
    }

    // TODO:
    //  SHOW THE LATEST POSITION, JUST TERMINUS POINTS BUT AS A STANDALONE MODE
    //  GET RID OF INTELLIJ GRIDLAYOUT AND REPLACE THEM WITH GRIDBAGLAYOUT (thus allowing me to use maven)
    //  ADD ABILITY TO SELECT A SINGLE ENTRY AND SEE ITS DATA IN A POPUP OVER THE POINT
    //  ADD CREDITS TO THE ABOUT PAGE
    //  ADD ACTIVITY GRAPHS
    //  FIX ISSUE WHERE THE HYPIXEL DATA IS READ BUT NOT ACTUALLY USED, PROB DUPLICATE DATA?
    //  ADD PLAYER NAME LABELS OVER MOST RECENT PLAYER POINT

    public static void main(String[] args) {
        createDirectories();
        boolean debug = args.length > 0 && args[0].contains("-debug");
        Logger.registerLogger();

        HashMap<String, Pair<Pair<String, String>, String>> updateInfo = Assets.shouldUpdate();
        boolean noUpdate = Arrays.asList(args).contains("-no-update");
        boolean update = Arrays.asList(args).contains("-update");
        if (updateInfo != null && !noUpdate) {
            updateApplication(updateInfo, !update);
        }

        Configs.registerConfigs();

        if (debug) {
            Logger.warn("DEBUG MODE IS ON, PERFORMANCE MAY BE AFFECTED");
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                INSTANCE = new PlayerTrackerDecoder(debug);
                INSTANCE.setVisible(true);
            }
        });
    }

    private void loadResources(){
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            Logger.info("Loading resources");
            final long nowMs = System.currentTimeMillis();

            playIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/play.png"))).getScaledInstance(24, 24, 4), "Play");
            playIcon_D = new ImageIcon(Utils.invertImage(playIcon_L.getImage()), "Play");
            pauseIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/pause.png"))).getScaledInstance(24, 24, 4), "Pause");
            pauseIcon_D = new ImageIcon(Utils.invertImage(pauseIcon_L.getImage()), "Pause");
            replayIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/replay.png"))).getScaledInstance(24, 24, 4), "Replay");
            replayIcon_D = new ImageIcon(Utils.invertImage(replayIcon_L.getImage()), "Replay");
            speedIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/fastForward.png"))).getScaledInstance(24, 24, 4), "Fast Forward");
            speedIcon_D = new ImageIcon(Utils.invertImage(speedIcon_L.getImage()), "Fast Forward");
            importIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/import.png"))).getScaledInstance(24, 24, 4), "Import");
            importIcon_D = new ImageIcon(Utils.invertImage(importIcon_L.getImage()), "Import");
            exportIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/export.png"))).getScaledInstance(24, 24, 4), "Export Image");
            exportIcon_D = new ImageIcon(Utils.invertImage(exportIcon_L.getImage()), "Export Image");
            screenshotIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/screenshot.png"))).getScaledInstance(24, 24, 4), "Take Screenshot");
            screenshotIcon_D = new ImageIcon(Utils.invertImage(screenshotIcon_L.getImage()), "Take Screenshot");
            toggleIconON_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/toggle-true.png"))).getScaledInstance(24, 24, 4), "Disable");
            toggleIconON_D = new ImageIcon(Utils.invertImage(toggleIconON_L.getImage()), "Disable");
            toggleIconOFF_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/toggle-false.png"))).getScaledInstance(24, 24, 4), "Enable");
            toggleIconOFF_D = new ImageIcon(Utils.invertImage(toggleIconOFF_L.getImage()), "Enable");
            settingsIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/settings.png"))).getScaledInstance(24, 24, 4), "Off");
            settingsIcon_D = new ImageIcon(Utils.invertImage(settingsIcon_L.getImage()), "Off");
            helpIcon_L = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/help.png"))).getScaledInstance(24, 24, 4), "Help");
            helpIcon_D = new ImageIcon(Utils.invertImage(helpIcon_L.getImage()), "Help");
            lightThemeIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/lightThemeIcon.png"))).getScaledInstance(177, 118, 4), "Off");
            darkThemeIcon = new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("icons/darkThemeIcon.png"))).getScaledInstance(177, 118, 4), "Off");

            final long durMs = System.currentTimeMillis() - nowMs;

            Logger.info("Successfully loaded resources in " + durMs + "ms");
        } catch (Exception e) {
            Logger.err("Error loading icon resources:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void createDirectories() {
        try {
            String[] dirs = {DIR_CONFIG, DIR_LOGS};
            for (String dir : dirs) {
                Files.createDirectories(Paths.get(dir));
            }
            Assets.createDirs();
        } catch (IOException e) {
            Logger.err("Failed to create a necessary directory:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }
    }

    private static void updateApplication(HashMap<String, Pair<Pair<String, String>, String>> updateInfo, boolean prompt) {
        Pair<Pair<String, String>, String> release = updateInfo.get("jar");
        if (release == null) {
            Logger.err("Missing jar Entry");
            return;
        }
        String OS = System.getProperty("os.name").toLowerCase();
        boolean isWindows = (OS.contains("win"));
        boolean isMax = (OS.contains("mac"));
        boolean isUnix = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
        boolean isSolaris = (OS.contains("sunos"));

        boolean shouldUpdate = false;
        Pair<Pair<String, String>, String> exeRelease = updateInfo.get("exe");
        if (exeRelease != null && isWindows) {
            release = exeRelease;
            shouldUpdate = true;
        }

        if (prompt) {
            int dialogResult = JOptionPane.showConfirmDialog(null, String.format("Update to the newest version (%s)?", release.getSecond()), "Update available!", JOptionPane.YES_NO_OPTION);
            if (dialogResult != 0) {
                return;
            }
        }

        JDialog downloadPopup = new JDialog();
        JPanel p1 = new JPanel(new GridBagLayout());
        p1.add(new JLabel("<html><div style='text-align: center;'>Downloading new version<br>Please wait...</div></html>"));
        downloadPopup.setUndecorated(true);
        downloadPopup.getContentPane().add(p1);
        downloadPopup.pack();
        downloadPopup.setLocationRelativeTo(null);
        downloadPopup.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        downloadPopup.setModal(true);

        downloadPopup.setSize(new Dimension(300, 50));
        downloadPopup.setShape(new RoundRectangle2D.Double(0, 0, 300, 50, 50, 50));

        SwingWorker<String, Void> downloadWorker = getDownloadWorker(downloadPopup, release.getFirst());
        downloadWorker.execute();
        downloadPopup.setVisible(true);

        String newVersion = null;
        try {
            newVersion = downloadWorker.get(); // blocking wait (intended)
        } catch (Exception e) {
            Logger.err("Failed to use the download worker:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }
        downloadPopup.setVisible(false);
        downloadPopup.dispose();

        if (newVersion != null) {
            Process ps;
            try {
//                if (!shouldUpdate) {
                ps = Runtime.getRuntime().exec(new String[]{"java", "-jar", newVersion, "-no-update"});
//                } else {
//                    ps = Runtime.getRuntime().exec(new String[]{"./" + newVersion, "-no-update"});
//                }

                Logger.info(String.format("Process exited with %s", ps.waitFor()));
            } catch (Exception e) {
                Logger.err("Failed to start the new process, error:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
                return;
            }

            int exitValue = ps.exitValue();
            if (exitValue != 0) {
                Logger.err("Failed to execute jar:\n " + Arrays.toString(new BufferedReader(new InputStreamReader(ps.getErrorStream())).lines().toArray()));
            } else {
                Logger.warn(String.format("Switching to newer version! %s", newVersion));
                System.exit(0);
            }
        }
    }

    private static SwingWorker<String, Void> getDownloadWorker(JDialog parent, Pair<String, String> newVersion) {
        return new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return Assets.downloadLatestVersion(newVersion.getFirst(), newVersion.getSecond());
            }

            @Override
            protected void done() {
                super.done();
                parent.dispose();
            }
        };
    }

    private void initMainFrame() {
        Logger.info("Initializing primary frame subsystem");

        try {
            if (settings.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            }
        } catch (UnsupportedLookAndFeelException e) {
            Logger.err("Error setting system look and feel for UI:\n " + Arrays.toString(e.getStackTrace()));
        }

        setTitle("Player Tracker Decoder App - v" + VERSION);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(new Dimension(1280, 720));
        setMinimumSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.DARK_GRAY);

        mainPanel = new MainPanel(settings);
        mainPanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    hasBackgroundImage = false;
                    mainPanel.isPlaying = false;
                    mainPanel.shouldDraw = false;
                    if (importForm != null) {
                        importForm.setVisible(false);
                        importForm = null;
                    }

                    importForm = new ImportForm(PlayerTrackerDecoder.this, settings, evt, alreadyImported);
                    importForm.setLocationRelativeTo(PlayerTrackerDecoder.this);
                    importForm.setVisible(true);
                } catch (Exception e) {
                    Logger.err("Error doing drag and drop on main frame:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
                }
            }
        });
        mainPanel.setDoubleBuffered(true);
        (new Thread(mainPanel)).start();

        mainPanel.coordinateLabel = new JLabel();
        mainPanel.coordinateLabel.setText("");
        mainPanel.selectedEntryLabel = new JLabel("Nothing Selected");
        mainPanel.selectedEntryLabel.setVisible(false);
        add(mainPanel, BorderLayout.CENTER);
        mainPanel.setVisible(true);
        mainPanel.repaint();
        revalidate();
        Logger.info("Successfully initialized primary frame subsystem");
    }

    public void ConfirmImport(ArrayList<File> files, MCVersion worldVersion, com.seedfinding.mccore.state.Dimension dimension, int threadCount, String worldSeed, boolean overwrite) {
        try {
            if (alreadyImported && overwrite) {
                mainPanel.Reset();
            }

            decodeAndDisplay(files);
        } catch (IOException e) {
            Logger.err("Error decoding the selected input log files:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }

        if (!worldSeed.isBlank()) {
            long fixedSeed;
            try {
                fixedSeed = Long.parseLong(worldSeed);
            } catch (NumberFormatException e) {
                fixedSeed = worldSeed.hashCode();
            }

            mainPanel.setSeedMapInfo(worldVersion, dimension, threadCount, fixedSeed);
        }
    }

    public void ConfirmImport(ArrayList<File> files, BufferedImage worldImg, boolean overwrite) {
        initializeWorldImage(worldImg);

        try {
            if (alreadyImported && overwrite) {
                mainPanel.Reset();
            }

            decodeAndDisplay(files);
        } catch (IOException e) {
            Logger.err("Error decoding the selected input log files:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }

        mainPanel.resetSeedMapInfo();
    }

    public void initializeWorldImage(BufferedImage image) {
        hasBackgroundImage = true;

        mainPanel.backgroundImage = image;

        mainPanel.backgroundImage.setAccelerationPriority(1);

        backgroundImagePanel = new JPanel();
        backgroundImagePanel.setLayout(new GridLayout(3, 5));

        backgroundImagePanel.add(new JLabel("World Background Image Offset:   "));

        int width = mainPanel.backgroundImage.getWidth();
        int height = mainPanel.backgroundImage.getHeight();
        int defaultX = Utils.clamp(mainPanel.xBackgroundOffset, -width, width);
        int defaultZ = Utils.clamp(mainPanel.zBackgroundOffset, -height, height);

        Logger.info(String.format("World Image: width: %d, height: %d", width, height));
        Logger.info(String.format("World Image: defaultX: %d, defaultZ: %d", defaultX, defaultZ));

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
            mainPanel.repaint();

            Logger.info("Changed the world background image X offset to: " + x);
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
            mainPanel.repaint();

            Logger.info("Changed the world background image Y offset to: " + z);
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
            mainPanel.repaint();

            Logger.info("Changed the world background image opacity to: " + opacity);
        });
        backgroundImagePanel.add(backgroundOpacitySlider);
        backgroundImagePanel.add(backgroundOpacityLabel);

        Logger.info("Successfully initialized world background image");
    }

    public void ChangeTheme(PlayerTrackerDecoder.UITheme newTheme) {
        settings.uiTheme = newTheme;
        settings.SaveSettings();

        dataFileImportButton.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? importIcon_L : importIcon_D);
        settingsButton.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? settingsIcon_L : settingsIcon_D);
        helpButton.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? helpIcon_L : helpIcon_D);

        if (exportAsImageButton != null) {
            exportAsImageButton.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? exportIcon_L : exportIcon_D);
        }

        if (screenshotButton != null) {
            screenshotButton.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? screenshotIcon_L : screenshotIcon_D);
        }

        if (mainPanel.isPlaying && animatePlayPause != null) {
            animatePlayPause.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? pauseIcon_L : pauseIcon_D);
        } else if (animatePlayPause != null) {
            animatePlayPause.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? playIcon_L : playIcon_D);
        }

        if (settings.terminusPoints && terminusPointsToggle != null) {
            terminusPointsToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconON_L : toggleIconON_D);
        } else if (terminusPointsToggle != null) {
            terminusPointsToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconOFF_L : toggleIconOFF_D);
        }

        if (settings.hiddenLines && terminusPointsToggle != null) {
            showHiddenLinesToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconON_L : toggleIconON_D);
        } else if (showHiddenLinesToggle != null) {
            showHiddenLinesToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconOFF_L : toggleIconOFF_D);
        }

        if (settings.fancyLines && fancyLinesToggle != null) {
            fancyLinesToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconON_L : toggleIconON_D);
        } else if (fancyLinesToggle != null) {
            fancyLinesToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconOFF_L : toggleIconOFF_D);
        }

        if (settings.ageFade && terminusPointsToggle != null) {
            ageFadeToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconON_L : toggleIconON_D);
        } else if (ageFadeToggle != null) {
            ageFadeToggle.setIcon(newTheme == PlayerTrackerDecoder.UITheme.Light ? toggleIconOFF_L : toggleIconOFF_D);
        }

        try {
            if (newTheme == PlayerTrackerDecoder.UITheme.Light) {
                mainPanel.setBackground(Color.lightGray);
                UIManager.setLookAndFeel(new FlatIntelliJLaf());
            } else {
                mainPanel.setBackground(Color.darkGray);
                UIManager.setLookAndFeel(new FlatDarculaLaf());
            }
        } catch (UnsupportedLookAndFeelException e) {
            Logger.err("Error setting system look and feel for UI:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void initDataSettingsToolBar(boolean remove) {
        Logger.info("Initializing toolbar subsystem");

        toolbar.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        if (remove) toolbar.removeAll();
        toolbar.setDoubleBuffered(true);

        tabbedPane = new JTabbedPane();
        toolbar.add(tabbedPane);

        GridBagConstraints gbc;

        //region Data
        JPanel dataPanel = new JPanel();
        dataPanel.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        dataPanel.add(new JLabel("Times:   "), gbc);

        System.out.println(mainPanel.logTimes.length - 1);
        timeRangeSlider = new RangeSlider(0, mainPanel.logTimes.length - 1);
        timeRangeSlider.setPreferredSize(new Dimension(600, 48));
        timeRangeSlider.setValue(0);
        timeRangeSlider.setUpperValue(mainPanel.logTimes.length - 1);
        timeRangeSlider.setPaintTicks(false);
        timeRangeSlider.setMajorTickSpacing(0);
        timeRangeSlider.setMinorTickSpacing(0);
        timeRangeSlider.setPaintLabels(false);
        timeRangeSlider.setSnapToTicks(false);

        startTimeLabel = new JLabel(mainPanel.startTime.toString().replace("T", "; "));
        endTimeLabel = new JLabel(mainPanel.endTime.toString().replace("T", "; "));
        timeRangeSlider.addChangeListener(e -> {
            RangeSlider source = (RangeSlider) e.getSource();
            int value1 = source.getValue();
            int value2 = source.getUpperValue();
            mainPanel.startTime = mainPanel.logTimes[value1];
            mainPanel.endTime = mainPanel.logTimes[value2];
            startTimeLabel.setText(mainPanel.startTime.toString().replace("T", "; "));
            endTimeLabel.setText(mainPanel.endTime.toString().replace("T", "; "));

            if (!mainPanel.isPlaying) {
                mainPanel.queuePointUpdate(true);
                Logger.info("Changed date range slider: From " + startTimeLabel.getText() + " to " + endTimeLabel.getText());
            }
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        dataPanel.add(startTimeLabel, gbc);
        gbc.gridx = 2;
        gbc.weightx = 1.0;
        dataPanel.add(timeRangeSlider, gbc);
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 1, 0, 0);
        dataPanel.add(endTimeLabel, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        dataPanel.add(new JLabel("Animate:", JLabel.RIGHT), gbc);

        animatePlayPause = new JToggleButton("", false);
        animatePlayPause.setIcon(mainPanel.isPlaying ? pauseIcon_L : playIcon_L);
        animatePlayPause.setPreferredSize(new Dimension(24, 24));
        animatePlayPause.setMargin(new Insets(2, 2, 2, 2));
        animatePlayPause.setBorder(BorderFactory.createEmptyBorder());
        animatePlayPause.setBackground(new Color(0, 0, 0, 0));

        animatePlayPause.addItemListener(ev -> {
            mainPanel.isPlaying = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.dateTimeIndex = timeRangeSlider.getUpperValue();

            if (settings.uiTheme == UITheme.Light) {
                animatePlayPause.setIcon(mainPanel.isPlaying ? pauseIcon_L : playIcon_L);
            } else {
                animatePlayPause.setIcon(mainPanel.isPlaying ? pauseIcon_D : playIcon_D);
            }
            Logger.info(mainPanel.isPlaying ? "Started playing animation" : "Stopped playing animation");
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        dataPanel.add(animatePlayPause, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        dataPanel.add(new JLabel("Animation Speed:", JLabel.RIGHT), gbc);

        JSpinner animSpeedSpinner = new JSpinner();
        animSpeedSpinner.setValue(mainPanel != null ? mainPanel.animationSpeed : 1);
        animSpeedSpinner.addChangeListener(e -> {
            int value = (int) ((JSpinner) e.getSource()).getValue();
            if (value < 1) {
                animSpeedSpinner.setValue(1);
                value = 1;
            } else if (value > 100) {
                animSpeedSpinner.setValue(100);
                value = 100;
            }
            mainPanel.animationSpeed = value;

            Logger.info("Changed animation speed to: " + mainPanel.animationSpeed);
        });

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        dataPanel.add(animSpeedSpinner, gbc);

        tabbedPane.addTab("Data", null, dataPanel, "Data range settings");
        //endregion

        //region Players
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BorderLayout());

        String[] nameSet = mainPanel.playerNameColorMap.keySet().toArray(new String[0]);

        JPanel[] playerPages = new JPanel[(int) Math.ceil((double) nameSet.length / (double) PLAYER_PAGE_CAPACITY)];
        for (int i = 0; i < playerPages.length; i++) {
            playerPages[i] = new JPanel();
            playerPages[i].setLayout(new GridLayout(3, 4));
        }

        for (int i = 0; i < nameSet.length; i++) {
            String player = nameSet[i];
            JMenuItem colButton = new JMenuItem(player);
            colButton.setForeground(mainPanel.playerNameColorMap.get(player));

            JToggleButton toggle = new JToggleButton("", mainPanel.playerNameEnabledMap.get(player));
            toggle.setPreferredSize(new Dimension(24, 24));

            JPanel panel = new JPanel();
            panel.add(colButton);
            panel.add(toggle);
            JLabel countLabel = new JLabel(mainPanel.playerMarkerCount.get(player) + "x", SwingConstants.LEFT);
            panel.add(countLabel, CENTER_ALIGNMENT);

            int index = (i / PLAYER_PAGE_CAPACITY);
            playerPages[index].add(panel);

            colButton.addActionListener(event -> {
                Color selectedColor = JColorChooser.showDialog(PlayerTrackerDecoder.this, "Select player color", mainPanel.playerNameColorMap.get(player));
                colButton.setForeground(selectedColor);
                mainPanel.playerNameColorMap.put(player, selectedColor);
                mainPanel.repaint();

                Logger.info("Changed " + player + "'s color");
            });

            toggle.addItemListener(ev -> {
                boolean value = (ev.getStateChange() == ItemEvent.SELECTED);
                mainPanel.playerNameEnabledMap.put(player, value);
                mainPanel.queuePointUpdate(true);

                Logger.info((value ? "Showed " : "Hid ") + player + "'s data");
            });
        }

        playerPageLabel = new JLabel("Page " + (currentPlayerPageIndex + 1) + "/" + playerPages.length, SwingConstants.CENTER);
        playerPanel.add(playerPageLabel, BorderLayout.NORTH);

        JButton leftButton = new JButton("<");
        playerPanel.add(leftButton, BorderLayout.WEST);
        leftButton.addActionListener(event -> {
            if (currentPlayerPageIndex > 0) {
                currentPlayerPageIndex--;
            } else {
                currentPlayerPageIndex = playerPages.length - 1;
            }

            playerPageLabel.setText("Page " + (currentPlayerPageIndex + 1) + "/" + playerPages.length);
            for (JPanel panel : playerPages) {
                if (panel != playerPages[currentPlayerPageIndex]) {
                    playerPanel.remove(panel);
                }
            }
            playerPanel.add(playerPages[currentPlayerPageIndex]);
            playerPanel.repaint();
            playerPanel.revalidate();
        });

        JButton rightButton = new JButton(">");
        playerPanel.add(rightButton, BorderLayout.EAST);
        rightButton.addActionListener(event -> {
            if (currentPlayerPageIndex < playerPages.length - 1) {
                currentPlayerPageIndex++;
            } else {
                currentPlayerPageIndex = 0;
            }

            playerPageLabel.setText("Page " + (currentPlayerPageIndex + 1) + "/" + playerPages.length);
            for (JPanel panel : playerPages) {
                if (panel != playerPages[currentPlayerPageIndex]) {
                    playerPanel.remove(panel);
                }
            }
            playerPanel.add(playerPages[currentPlayerPageIndex]);
            playerPanel.repaint();
            playerPanel.revalidate();
        });

        playerPanel.add(playerPages[0]);

        tabbedPane.addTab("Player", null, playerPanel, "Player display settings");
        //endregion

        //region Render
        JPanel renderPanel = new JPanel();
        renderPanel.setLayout(new GridBagLayout());

        drawTypeChooser = new JComboBox<>(new DrawType[]{DrawType.Pixel, DrawType.Dot, DrawType.Line, DrawType.Heat, DrawType.Fast});
//        drawTypeChooser.setPreferredSize(new Dimension(85, 24));
        drawTypeChooser.setSelectedItem(settings._drawType);
        drawTypeChooser.addActionListener(event -> {
            settings._drawType = (DrawType) drawTypeChooser.getSelectedItem();
//            if (drawSizeComponent != null) {
//                drawSizeTitle.setEnabled(settings._drawType == DrawType.Line || settings._drawType == DrawType.Heat);
//                drawSizeSlider.setEnabled(settings._drawType == DrawType.Line || settings._drawType == DrawType.Heat);
//                drawSizeLabel.setEnabled(settings._drawType == DrawType.Line || settings._drawType == DrawType.Heat);
//            }
            if (lineThresholdComponent != null) {
                lineThresholdTitle.setEnabled(settings._drawType == DrawType.Line);
                lineThresholdSlider.setEnabled(settings._drawType == DrawType.Line);
                lineThresholdLabel.setEnabled(settings._drawType == DrawType.Line);
            }
            if (terminusPointsToggle != null) {
                terminusPointsToggle.setEnabled(settings._drawType == DrawType.Line);
            }
            if (fancyLinesToggle != null) {
                fancyLinesToggle.setEnabled(settings._drawType == DrawType.Line);
            }
            if (showHiddenLinesToggle != null) {
                showHiddenLinesToggle.setEnabled(settings._drawType == DrawType.Line);
            }

            if (ageFadeToggle != null) {
                ageFadeToggle.setEnabled(settings._drawType != DrawType.Heat && settings._drawType != DrawType.Fast);
            }
            if (ageFadeComponent != null) {
                ageFadeStrengthTitle.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat && settings._drawType != DrawType.Fast);
                ageFadeStrengthSlider.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat && settings._drawType != DrawType.Fast);
                ageFadeStrengthLabel.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat && settings._drawType != DrawType.Fast);
            }

            if (heatDrawTypeChooser != null) {
                heatDrawTypeChooser.setEnabled(settings._drawType == DrawType.Heat);
            }
            if (heatMapComponent != null) {
                heatMapStrengthTitle.setEnabled(settings._drawType == DrawType.Heat);
                heatMapStrengthSlider.setEnabled(settings._drawType == DrawType.Heat);
                heatMapStrengthLabel.setEnabled(settings._drawType == DrawType.Heat);
            }

            if (drawSizeSlider != null) {
                drawSizeSlider.setEnabled(settings._drawType != DrawType.Fast);
            }
            if (drawSizeComponent != null) {
                drawSizeComponent.setEnabled(settings._drawType != DrawType.Fast);
            }

//            mainPanel.updatePoints(true);
            drawSizeTitle.setText((settings._drawType == DrawType.Dot) ? "Dot Radius" : ((settings._drawType == DrawType.Pixel || settings._drawType == DrawType.Heat) ? "Pixel Size" : ((settings._drawType == DrawType.Line) ? "Line Thickness" : "   ---")));
            settings.SaveSettings();
            mainPanel.repaint();

            toolbar.validate();
            toolbar.repaint();

            Logger.info("Changed draw type to: " + settings._drawType);
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(drawTypeChooser, gbc);

        heatDrawTypeChooser = new JComboBox<>(new HeatDrawType[]{HeatDrawType.Size, HeatDrawType.Color});
//        heatDrawTypeChooser.setPreferredSize(new Dimension(85, 24));
        heatDrawTypeChooser.setSelectedItem(settings._heatDrawType);
        heatDrawTypeChooser.addActionListener(event -> {
            settings._heatDrawType = (HeatDrawType) heatDrawTypeChooser.getSelectedItem();

            settings.SaveSettings();
            mainPanel.repaint();

            Logger.info("Changed heat draw type to: " + settings._heatDrawType);
        });
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(heatDrawTypeChooser, gbc);
        heatDrawTypeChooser.setEnabled(settings._drawType == DrawType.Heat);

        drawSizeComponent = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(drawSizeComponent, gbc);

        drawSizeTitle = new JLabel((settings._drawType == DrawType.Dot) ? "Dot Radius" : ((settings._drawType == DrawType.Pixel || settings._drawType == DrawType.Heat) ? "Pixel Size" : ((settings._drawType == DrawType.Line) ? "Line Thickness" : "-")));
        drawSizeComponent.add(drawSizeTitle);

        drawSizeSlider = new JSlider(0, 0, 75, Utils.clamp((int) settings.size * 10, 0, 75)); //  Math.max(1, settings.size > 50 ? (int) (settings.size + (settings.size * 0.1f)) : 50)
//        sizeSlider.setPreferredSize(new Dimension(200, 24));
        drawSizeSlider.setPaintTicks(true);
        drawSizeSlider.setMajorTickSpacing(0);
        drawSizeSlider.setMinorTickSpacing(0);
        drawSizeSlider.setPaintLabels(true);
        drawSizeLabel = new JLabel(Float.toString(settings.size));
        drawSizeSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            settings.size = (float) source.getValue() / 10.0f;
            drawSizeLabel.setText(Float.toString(settings.size));
            mainPanel.repaint();
            settings.SaveSettings();

            Logger.info("Changed draw size to: " + settings.size);
        });
        drawSizeComponent.add(drawSizeTitle);
        drawSizeComponent.add(drawSizeSlider);
        drawSizeComponent.add(drawSizeLabel);

        lineThresholdComponent = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(lineThresholdComponent, gbc);

        lineThresholdTitle = new JLabel("Line Threshold");
        lineThresholdComponent.add(lineThresholdTitle);

        lineThresholdSlider = new JSlider(0, 0, 200, Utils.clamp(settings.lineThreshold * 100, 0, 200));
//        lineThresholdSlider.setPreferredSize(new Dimension(200, 24));
        lineThresholdSlider.setPaintTicks(true);
        lineThresholdSlider.setMajorTickSpacing(50);
        lineThresholdSlider.setMinorTickSpacing(25);
        lineThresholdSlider.setPaintLabels(false);
        lineThresholdLabel = new JLabel(Integer.toString(settings.lineThreshold));
        lineThresholdSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int threshold = source.getValue();
            settings.lineThreshold = threshold;
            lineThresholdLabel.setText(Integer.toString(threshold));
            mainPanel.repaint();
            settings.SaveSettings();

            Logger.info("Changed line threshold to: " + settings.lineThreshold);
        });
        lineThresholdTitle.setEnabled(settings._drawType == DrawType.Line);
        lineThresholdSlider.setEnabled(settings._drawType == DrawType.Line);
        lineThresholdLabel.setEnabled(settings._drawType == DrawType.Line);
        lineThresholdComponent.add(lineThresholdSlider);
        lineThresholdComponent.add(lineThresholdLabel);

        heatMapComponent = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(heatMapComponent, gbc);

        heatMapStrengthTitle = new JLabel("Activity Strength");
        heatMapStrengthSlider = new JSlider(0, -100, 100, Utils.clamp((int) (settings.heatMapStrength * 100), -100, 100));
//        heatMapStrengthSlider.setPreferredSize(new Dimension(150, 24));
        heatMapStrengthSlider.setPaintTicks(true);
        heatMapStrengthSlider.setMajorTickSpacing(50);
        heatMapStrengthSlider.setMinorTickSpacing(25);
        heatMapStrengthSlider.setPaintLabels(true);
        heatMapStrengthLabel = new JLabel((settings.heatMapStrength * 100) + "%");
        heatMapStrengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int strength = source.getValue();
            settings.heatMapStrength = (float) strength / 100.0f;
            heatMapStrengthLabel.setText(strength + "%");

            mainPanel.repaint();
            settings.SaveSettings();

            Logger.info("Changed activity strength to: " + settings.heatMapStrength);
        });
        heatMapStrengthTitle.setEnabled(settings._drawType == DrawType.Heat);
        heatMapStrengthSlider.setEnabled(settings._drawType == DrawType.Heat);
        heatMapStrengthLabel.setEnabled(settings._drawType == DrawType.Heat);
        heatMapComponent.add(heatMapStrengthTitle);
        heatMapComponent.add(heatMapStrengthSlider);
        heatMapComponent.add(heatMapStrengthLabel);

        ageFadeComponent = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(ageFadeComponent, gbc);

        ageFadeStrengthTitle = new JLabel("Age Fade Strength");
        ageFadeStrengthSlider = new JSlider(0, 0, 100, Utils.clamp((int) settings.ageFadeStrength * 100, 0, 100));
//        ageFadeStrengthSlider.setPreferredSize(new Dimension(150, 24));
        ageFadeStrengthSlider.setPaintTicks(true);
        ageFadeStrengthSlider.setMajorTickSpacing(50);
        ageFadeStrengthSlider.setMinorTickSpacing(25);
        ageFadeStrengthSlider.setPaintLabels(true);
        ageFadeStrengthLabel = new JLabel((settings.ageFadeStrength * 100f) + "%");

        ageFadeStrengthSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            int strength = source.getValue();
            settings.ageFadeStrength = (float) strength / 100f;
            ageFadeStrengthLabel.setText(strength + "%");
            mainPanel.repaint();
            settings.SaveSettings();

            Logger.info("Changed age fade strength to: " + settings.ageFadeStrength);
        });
        ageFadeStrengthSlider.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat);
        ageFadeStrengthTitle.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat);
        ageFadeStrengthLabel.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat);
        ageFadeComponent.add(ageFadeStrengthTitle);
        ageFadeComponent.add(ageFadeStrengthSlider);
        ageFadeComponent.add(ageFadeStrengthLabel);

        ageFadeToggle = new JToggleButton("Age Fade", settings.ageFade);
        ageFadeToggle.setIcon(settings.ageFade ? toggleIconON_L : toggleIconOFF_L);
//        ageFadeToggle.setPreferredSize(new Dimension(24, 24));
        ageFadeToggle.setMargin(new Insets(2, 2, 2, 2));
        ageFadeToggle.setBorder(BorderFactory.createEmptyBorder());
        ageFadeToggle.setBackground(new Color(0, 0, 0, 0));
        ageFadeToggle.setEnabled(settings._drawType != DrawType.Heat);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(ageFadeToggle, gbc);

        ageFadeToggle.addItemListener(ev -> {
            settings.ageFade = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.repaint();
            settings.SaveSettings();

            if (settings.uiTheme == UITheme.Light) {
                ageFadeToggle.setIcon(settings.ageFade ? toggleIconON_L : toggleIconOFF_L);
            } else {
                ageFadeToggle.setIcon(settings.ageFade ? toggleIconON_D : toggleIconOFF_D);
            }
            ageFadeStrengthSlider.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat);
            ageFadeStrengthTitle.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat);
            ageFadeStrengthLabel.setEnabled(settings.ageFade && settings._drawType != DrawType.Heat);

            Logger.info("Toggled age fade to: " + settings.ageFade);
        });

        fancyLinesToggle = new JToggleButton("Fancy Lines", settings.fancyLines);
        fancyLinesToggle.setIcon(settings.fancyLines ? toggleIconON_L : toggleIconOFF_L);
//        fancyLinesToggle.setPreferredSize(new Dimension(24, 24));
        fancyLinesToggle.setMargin(new Insets(2, 2, 2, 2));
        fancyLinesToggle.setBorder(BorderFactory.createEmptyBorder());
        fancyLinesToggle.setBackground(new Color(0, 0, 0, 0));

        fancyLinesToggle.addItemListener(ev -> {
            settings.fancyLines = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.repaint();
            settings.SaveSettings();

            if (settings.uiTheme == UITheme.Light) {
                fancyLinesToggle.setIcon(settings.fancyLines ? toggleIconON_L : toggleIconOFF_L);
            } else {
                fancyLinesToggle.setIcon(settings.fancyLines ? toggleIconON_D : toggleIconOFF_D);
            }

            Logger.info("Toggled fancy lines to: " + settings.fancyLines);
        });
        fancyLinesToggle.setEnabled(settings._drawType == DrawType.Line);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(fancyLinesToggle, gbc);

        terminusPointsToggle = new JToggleButton("Terminus Points", settings.terminusPoints);
        terminusPointsToggle.setIcon(settings.terminusPoints ? toggleIconON_L : toggleIconOFF_L);
//        terminusPointsToggle.setPreferredSize(new Dimension(24, 24));
        terminusPointsToggle.setMargin(new Insets(2, 2, 2, 2));
        terminusPointsToggle.setBorder(BorderFactory.createEmptyBorder());
        terminusPointsToggle.setBackground(new Color(0, 0, 0, 0));

        terminusPointsToggle.addItemListener(ev -> {
            settings.terminusPoints = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.repaint();
            settings.SaveSettings();

            if (settings.uiTheme == UITheme.Light) {
                terminusPointsToggle.setIcon(settings.terminusPoints ? toggleIconON_L : toggleIconOFF_L);
            } else {
                terminusPointsToggle.setIcon(settings.terminusPoints ? toggleIconON_D : toggleIconOFF_D);
            }

            Logger.info("Toggled terminus points to: " + settings.terminusPoints);
        });
        terminusPointsToggle.setEnabled(settings._drawType == DrawType.Line);
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(terminusPointsToggle, gbc);

        showHiddenLinesToggle = new JToggleButton("Show Hidden Lines", settings.hiddenLines);
        showHiddenLinesToggle.setIcon(settings.hiddenLines ? toggleIconON_L : toggleIconOFF_L);
//        showHiddenLinesToggle.setPreferredSize(new Dimension(24, 24));
        showHiddenLinesToggle.setMargin(new Insets(2, 2, 2, 2));
        showHiddenLinesToggle.setBorder(BorderFactory.createEmptyBorder());
        showHiddenLinesToggle.setBackground(new Color(0, 0, 0, 0));

        showHiddenLinesToggle.addItemListener(ev -> {
            settings.hiddenLines = (ev.getStateChange() == ItemEvent.SELECTED);
            mainPanel.repaint();
            settings.SaveSettings();

            if (settings.uiTheme == UITheme.Light) {
                showHiddenLinesToggle.setIcon(settings.hiddenLines ? toggleIconON_L : toggleIconOFF_L);
            } else {
                showHiddenLinesToggle.setIcon(settings.hiddenLines ? toggleIconON_D : toggleIconOFF_D);
            }

            Logger.info("Toggled hidden lines to: " + settings.hiddenLines);
        });
        showHiddenLinesToggle.setEnabled((settings._drawType == DrawType.Line));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        renderPanel.add(showHiddenLinesToggle, gbc);

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
        JPanel exportPanel = new JPanel();
        exportPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        screenshotButton = new JButton();
        screenshotButton.setIcon(screenshotIcon_L);
        screenshotButton.setBackground(new Color(0, 0, 0, 0));
        screenshotButton.setPreferredSize(new Dimension(48, 48));
        screenshotButton.setMargin(new Insets(2, 2, 2, 2));
        screenshotButton.setBorder(BorderFactory.createEmptyBorder());

        screenshotButton.addActionListener(event -> mainPanel.SaveAsImage(true));
        exportPanel.add(screenshotButton);

        exportAsImageButton = new JButton();
        exportAsImageButton.setIcon(exportIcon_L);
        exportAsImageButton.setBackground(new Color(0, 0, 0, 0));
        exportAsImageButton.setPreferredSize(new Dimension(48, 48));
        exportAsImageButton.setMargin(new Insets(2, 2, 2, 2));
        exportAsImageButton.setBorder(BorderFactory.createEmptyBorder());

        exportAsImageButton.addActionListener(event -> mainPanel.SaveAsImage(false));
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

            Logger.info("Changed export upscale to: " + upscale);
        });
        exportPanel.add(exportUpscaleSlider);
        exportPanel.add(exportUpscaleLabel);
        exportPanel.add(new JLabel("   WARNING (Don't increase too high! Uses lots of memory!)"));
        exportPanel.add(imageExportStatus);

        tabbedPane.addTab("Export", null, exportPanel, "Export data as an image");
        //endregion

        toolbar.add(tabbedPane);

        revalidate();

        ChangeTheme(settings.uiTheme);

        Logger.info("Successfully initialized toolbar subsystem");
    }

    private void decodeAndDisplay(ArrayList<File> inputFiles) throws IOException {
        ArrayList<File> files = (ArrayList<File>) inputFiles.stream().distinct().collect(Collectors.toList());

        Logger.info("Selected files: " + files.size());

        Thread exec = new Thread(() -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            mainPanel.setData(Objects.requireNonNull(Decoder.Decode(files, settings.maxDataEntries, settings.convertChunkPosToBlockPos)));

            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

            initDataSettingsToolBar(alreadyImported);
            toolbar.setVisible(true);
            mainPanel.selectedEntryLabel.setVisible(true);

            mainPanel.shouldDraw = true;

            alreadyImported = true;

            Logger.info("Decoding process finished successfully");
        });

        exec.start();
    }

    public enum UITheme {
        Light, Dark
    }

    public enum DrawType {
        Pixel, Dot, Line, Heat, Fast
    }

    public enum HeatDrawType {
        Size, Color
    }
}