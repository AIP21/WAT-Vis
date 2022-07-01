package src.main.ui;

import com.seedfinding.mccore.version.MCVersion;
import src.main.importing.filters.ImageFileFilter;
import src.main.importing.filters.TextFileFilter;
import src.main.PlayerTrackerDecoder;
import src.main.config.Settings;
import src.main.util.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
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

import com.seedfinding.mccore.state.Dimension;

import static src.main.util.Logger.LOGGER;

public class ImportForm extends JDialog {
    private JPanel bottomPanel;
    private JLabel titleText;
    private JScrollPane selectedFileListScrollPane;
    private JList<File> selectedFileList;
    private JPanel fileSelectorPanel;
    private JButton addFileButton;
    private JButton removeFileButton;
    private JLabel fileSelectorTitle;
    private JPanel selectorButtons;
    private JProgressBar loadingProgressBar;
    private JButton importButton;
    private JButton appendButton;
    private JButton cancelButton;
    private JPanel bottomButtonPanel;
    private JRadioButton convertChunksToggle;
    private JRadioButton fancyRenderingToggle;
    private JSplitPane mainSplitPane;
    private JSpinner maxEntriesSpinner;
    private JLabel maxEntriesSpinnerLabel;
    private JLabel convertChunksLabel;
    private JSpinner imageXOffsetSpinner;
    private JSpinner imageZOffsetSpinner;
    private JButton addWorldImageButton;
    private JLabel fancyRenderingLabel;
    private JLabel imageXOffsetLabel;
    private JLabel imageZOffsetLabel;
    private JLabel worldImageLabel;
    private JComboBox<String> dimensionChooser;
    private JLabel dimensionChooserLabel;
    public JPanel mainSettings;
    public JPanel mapSettings;
    public JTabbedPane settingsTabs;
    public JPanel importSettingsPanel;
    public JLabel importSettingsTitle;
    public JComboBox backgroundTypeChooser;
    public JLabel backgroundTypeLabel;
    public JComboBox worldVersionChooser;
    public JLabel worldVersionLabel;
    public JTextField worldSeedTextField;
    public JLabel worldSeedLabel;

    private PlayerTrackerDecoder main;
    private Settings settings;

    private ArrayList<File> currentFiles = new ArrayList<>();
    private BufferedImage currentBackgroundImage;

    public ImportForm(PlayerTrackerDecoder main, Settings settings, boolean alreadyImported) {
        super(main, "Import Files");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        this.main = main;
        this.settings = settings;

        setSize(new java.awt.Dimension(720, 480));
        setResizable(false);

        initComponents(alreadyImported);

        Logger.info("File import pane opened");
    }

    public ImportForm(PlayerTrackerDecoder main, Settings settings, DropTargetDropEvent evt, boolean alreadyImported) {
        super(main, "Import Files");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        this.main = main;
        this.settings = settings;

        setSize(new java.awt.Dimension(720, 480));
        setResizable(false);

        initComponents(alreadyImported);

        try {
            evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
            List<File> files = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

            for (File file : files) {
                handleFile(file);
            }
            selectedFileList.setListData(currentFiles.toArray(new File[0]));
            importButton.setEnabled(currentFiles.size() > 0);
            if (alreadyImported) appendButton.setEnabled(currentFiles.size() > 0);
        } catch (Exception e) {
            LOGGER.severe("Error dragging and dropping files onto import panel: " + Arrays.toString(e.getStackTrace()));
        }

        Logger.info("File import pane opened from drag and drop");
    }

    private void initComponents(boolean alreadyImported) {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        add(bottomPanel, BorderLayout.SOUTH);

        titleText = new JLabel();
        titleText.setEnabled(true);
        Font TitleFont = this.getFont(null, -1, 26, titleText.getFont());
        if (TitleFont != null) titleText.setFont(TitleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        titleText.setText("Import Files");
        add(titleText, BorderLayout.NORTH);

        bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        bottomPanel.add(bottomButtonPanel, gbc);

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bottomButtonPanel.add(cancelButton, gbc);

        importButton = new JButton();
        importButton.setText("Import");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bottomButtonPanel.add(importButton, gbc);

        if (alreadyImported) {
            appendButton = new JButton();
            appendButton.setText("Append");
            importButton.setText("Overwrite");
            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.weightx = 0.1;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            bottomButtonPanel.add(appendButton, gbc);
        }

        mainSplitPane = new JSplitPane();
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setDividerLocation(244);
        mainSplitPane.setDividerSize(9);
        mainSplitPane.setEnabled(true);
        add(mainSplitPane, BorderLayout.CENTER);

        fileSelectorPanel = new JPanel();
        fileSelectorPanel.setLayout(new GridBagLayout());
        mainSplitPane.setRightComponent(fileSelectorPanel);

        fileSelectorTitle = new JLabel();
        Font fileSelectorTitleFont = this.getFont(null, -1, 16, fileSelectorTitle.getFont());
        if (fileSelectorTitleFont != null) fileSelectorTitle.setFont(fileSelectorTitleFont);
        fileSelectorTitle.setText("Select Files");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        fileSelectorPanel.add(fileSelectorTitle, gbc);

        selectorButtons = new JPanel();
        selectorButtons.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        fileSelectorPanel.add(selectorButtons, gbc);

        addFileButton = new JButton();
        addFileButton.setText("Add");
        addFileButton.setToolTipText("Add Files");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        selectorButtons.add(addFileButton, gbc);

        removeFileButton = new JButton();
        removeFileButton.setText("Remove");
        removeFileButton.setToolTipText("Remove Files");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        selectorButtons.add(removeFileButton, gbc);

        selectedFileListScrollPane = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 3, 3);
        fileSelectorPanel.add(selectedFileListScrollPane, gbc);

        selectedFileList = new JList<File>();

        selectedFileList.setListData(currentFiles.toArray(new File[0]));
        selectedFileListScrollPane.setViewportView(selectedFileList);
        selectedFileList.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
                    List<File> files = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : files) {
                        handleFile(file);
                    }
                    selectedFileList.setListData(currentFiles.toArray(new File[0]));
                    importButton.setEnabled(currentFiles.size() > 0);
                } catch (Exception ex) {
                    LOGGER.severe("Error dragging and dropping files onto import panel: " + Arrays.toString(ex.getStackTrace()));
                }
            }
        });

        importSettingsPanel = new JPanel();
        importSettingsPanel.setLayout(new BorderLayout(0, 0));
        mainSplitPane.setLeftComponent(importSettingsPanel);

        importSettingsTitle = new JLabel();
        Font importSettingsTitleFont = this.getFont(null, -1, 16, importSettingsTitle.getFont());
        if (importSettingsTitleFont != null) importSettingsTitle.setFont(importSettingsTitleFont);
        importSettingsTitle.setHorizontalAlignment(0);
        importSettingsTitle.setText("Import Settings");
        importSettingsPanel.add(importSettingsTitle, BorderLayout.NORTH);

        settingsTabs = new JTabbedPane();
        settingsTabs.setTabLayoutPolicy(0);
        settingsTabs.setTabPlacement(1);
        importSettingsPanel.add(settingsTabs, BorderLayout.CENTER);

        mainSettings = new JPanel();
        mainSettings.setLayout(new GridBagLayout());
        settingsTabs.addTab("Main", mainSettings);

        convertChunksToggle = new JRadioButton();
        convertChunksToggle.setHideActionText(false);
        convertChunksToggle.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        convertChunksToggle.setSelected(settings.convertChunkPosToBlockPos);
        mainSettings.add(convertChunksToggle, gbc);

        convertChunksLabel = new JLabel();
        convertChunksLabel.setText("Convert Chunks");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        mainSettings.add(convertChunksLabel, gbc);

        fancyRenderingToggle = new JRadioButton();
        fancyRenderingToggle.setHideActionText(true);
        fancyRenderingToggle.setSelected(true);
        fancyRenderingToggle.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        fancyRenderingToggle.setSelected(settings.fancyRendering);
        mainSettings.add(fancyRenderingToggle, gbc);

        fancyRenderingLabel = new JLabel();
        fancyRenderingLabel.setText("Fancy Rendering");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainSettings.add(fancyRenderingLabel, gbc);

        maxEntriesSpinner = new JSpinner();
        maxEntriesSpinner.setValue(settings.maxDataEntries);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        maxEntriesSpinner.setToolTipText("The maximum amount of data entries to decode. Set to 0 to disable");
        mainSettings.add(maxEntriesSpinner, gbc);

        maxEntriesSpinnerLabel = new JLabel();
        maxEntriesSpinnerLabel.setText("Max Entries To Decode");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainSettings.add(maxEntriesSpinnerLabel, gbc);

        mapSettings = new JPanel();
        mapSettings.setLayout(new GridBagLayout());
        settingsTabs.addTab("Background", mapSettings);

        dimensionChooserLabel = new JLabel();
        dimensionChooserLabel.setText("Dimension");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mapSettings.add(dimensionChooserLabel, gbc);

        backgroundTypeChooser = new JComboBox<String>(new String[]{"Seed", "Image"});
        backgroundTypeChooser.setSelectedItem("Seed");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mapSettings.add(backgroundTypeChooser, gbc);

        backgroundTypeLabel = new JLabel();
        backgroundTypeLabel.setText("Background Type");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.ipadx = 20;
        gbc.insets = new Insets(0, 3, 0, 0);
        mapSettings.add(backgroundTypeLabel, gbc);

        dimensionChooser = new JComboBox<String>(new String[]{"Overworld", "Nether", "End"});
        dimensionChooser.setSelectedItem("Overworld");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mapSettings.add(dimensionChooser, gbc);

        worldSeedLabel = new JLabel();
        worldSeedLabel.setText("World Seed");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mapSettings.add(worldSeedLabel, gbc);

        worldSeedTextField = new JTextField();
        worldSeedTextField.setText("");
        worldSeedTextField.setToolTipText("Seed");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        mapSettings.add(worldSeedTextField, gbc);

        worldVersionLabel = new JLabel();
        worldVersionLabel.setText("World Version");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.ipadx = 20;
        gbc.insets = new Insets(0, 3, 0, 0);
        mapSettings.add(worldVersionLabel, gbc);

        worldVersionChooser = new JComboBox<MCVersion>(MCVersion.values());
        worldVersionChooser.setSelectedItem("Seed");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mapSettings.add(worldVersionChooser, gbc);

        worldImageLabel = new JLabel();
        worldImageLabel.setText("World Image");
        worldImageLabel.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mapSettings.add(worldImageLabel, gbc);

        addWorldImageButton = new JButton();
        addWorldImageButton.setText("Add World Image");
        addWorldImageButton.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mapSettings.add(addWorldImageButton, gbc);

        imageXOffsetLabel = new JLabel();
        imageXOffsetLabel.setText("Image X Offset");
        imageXOffsetLabel.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mapSettings.add(imageXOffsetLabel, gbc);

        imageXOffsetSpinner = new JSpinner();
        imageXOffsetSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        imageXOffsetSpinner.setToolTipText("How much to offset the x of image to center it on the world's (0,0). Can be changed later in the data settings.");
        mapSettings.add(imageXOffsetSpinner, gbc);

        imageZOffsetLabel = new JLabel();
        imageZOffsetLabel.setText("Image Y Offset");
        imageZOffsetLabel.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mapSettings.add(imageZOffsetLabel, gbc);

        imageZOffsetSpinner = new JSpinner();
        imageZOffsetSpinner.setEnabled(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        imageZOffsetSpinner.setToolTipText("How much to offset the y of image to center it on the world's (0,0). Can be changed later in the data settings.");
        mapSettings.add(imageZOffsetSpinner, gbc);

        maxEntriesSpinnerLabel.setLabelFor(maxEntriesSpinner);
        imageXOffsetLabel.setLabelFor(imageXOffsetSpinner);
        imageZOffsetLabel.setLabelFor(imageZOffsetSpinner);

        setCallbacks(alreadyImported);
    }

    private void handleFile(File file) {
        if (!file.isDirectory()) {
            if (!currentFiles.contains(file) && file.getName().contains(".txt")) {
                currentFiles.add(file);
            }
        } else {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                handleFile(f);
            }
        }
    }

    private void setCallbacks(boolean alreadyImported) {
        cancelButton.addActionListener(event -> {
            main.mainPanel.shouldDraw = true;
            main.mainPanel.isPlaying = false;
            this.setVisible(false);
        });

        importButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);

        importButton.addActionListener(event -> {
            if (backgroundTypeChooser.getSelectedItem() == "Seed") {
                main.ConfirmImport(currentFiles, (MCVersion) worldVersionChooser.getSelectedItem(), Dimension.fromId(dimensionChooser.getSelectedIndex()), worldSeedTextField.getText(), true);
            } else {
                main.ConfirmImport(currentFiles, currentBackgroundImage, true);
            }
            this.setVisible(false);
        });

        if (alreadyImported) {
            appendButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);

            appendButton.addActionListener(event -> {
                main.ConfirmImport(currentFiles, (MCVersion) worldVersionChooser.getSelectedItem(), Dimension.fromId(dimensionChooser.getSelectedIndex()), worldSeedTextField.getText(), false);
                this.setVisible(false);
            });
        }

        addFileButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(PlayerTrackerDecoder.DIR_INPUTS));
            chooser.setMultiSelectionEnabled(true);
            chooser.addChoosableFileFilter(new TextFileFilter());
            chooser.setAcceptAllFileFilterUsed(false);

            int returnVal = chooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                for (File file : files) {
                    handleFile(file);
                }
                selectedFileList.setListData(currentFiles.toArray(new File[0]));
                importButton.setEnabled(currentFiles.size() > 0);
                if (appendButton != null) appendButton.setEnabled(currentFiles.size() > 0);
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                LOGGER.severe("Error selecting input files");
            } else {
                LOGGER.warning("No input files selected");
            }
        });

        removeFileButton.addActionListener(event -> {
            currentFiles.removeAll(selectedFileList.getSelectedValuesList());
            selectedFileList.setListData(currentFiles.toArray(new File[0]));
            importButton.setEnabled(currentFiles.size() > 0);
            if (appendButton != null) appendButton.setEnabled(currentFiles.size() > 0);
        });

        if (PlayerTrackerDecoder.DEBUG) {
            dimensionChooser.addItemListener(event -> {
                String value = (String) dimensionChooser.getSelectedItem();
                assert value != null;
                if (value.equals("Overworld")) {
                    main.mainPanel.xBackgroundOffset = -6384;
                    imageXOffsetSpinner.setValue(main.mainPanel.xBackgroundOffset);
                    main.mainPanel.zBackgroundOffset = -5376;
                    imageZOffsetSpinner.setValue(main.mainPanel.zBackgroundOffset);
                } else if (value.equals("Nether")) {
                    main.mainPanel.xBackgroundOffset = -1008;
                    imageXOffsetSpinner.setValue(main.mainPanel.xBackgroundOffset);
                    main.mainPanel.zBackgroundOffset = -1969;
                    imageZOffsetSpinner.setValue(main.mainPanel.zBackgroundOffset);
                }
            });

            String value = (String) dimensionChooser.getSelectedItem();
            assert value != null;
            if (value.equals("Overworld")) {
                main.mainPanel.xBackgroundOffset = -6384;
                imageXOffsetSpinner.setValue(main.mainPanel.xBackgroundOffset);
                main.mainPanel.zBackgroundOffset = -5376;
                imageZOffsetSpinner.setValue(main.mainPanel.zBackgroundOffset);
            } else if (value.equals("Nether")) {
                main.mainPanel.xBackgroundOffset = -1008;
                imageXOffsetSpinner.setValue(main.mainPanel.xBackgroundOffset);
                main.mainPanel.zBackgroundOffset = -1969;
                imageZOffsetSpinner.setValue(main.mainPanel.zBackgroundOffset);
            }
        }

        maxEntriesSpinner.addChangeListener(e -> {
            settings.maxDataEntries = (int) ((JSpinner) e.getSource()).getValue();
            settings.SaveSettings();
        });

        fancyRenderingToggle.addItemListener(event -> {
            settings.fancyRendering = (event.getStateChange() == ItemEvent.SELECTED);
            settings.toggleRenderMode();
            settings.SaveSettings();
        });

        backgroundTypeChooser.addActionListener(event -> {
            boolean isSeed = backgroundTypeChooser.getSelectedItem() == "Seed";

            dimensionChooserLabel.setEnabled(isSeed);
            dimensionChooser.setEnabled(isSeed);
            worldSeedLabel.setEnabled(isSeed);
            worldSeedTextField.setEnabled(isSeed);
            worldVersionChooser.setEnabled(isSeed);

            worldImageLabel.setEnabled(!isSeed);
            addWorldImageButton.setEnabled(!isSeed);
            imageZOffsetLabel.setEnabled(!isSeed);
            imageXOffsetLabel.setEnabled(!isSeed);
            imageZOffsetSpinner.setEnabled(!isSeed);
            imageXOffsetSpinner.setEnabled(!isSeed);
        });

        addWorldImageButton.addActionListener(event -> {
            Logger.info("Opening world background image dialog");

            JFileChooser imgChooser = new JFileChooser(PlayerTrackerDecoder.DIR_WORLDIMAGES);
            imgChooser.setMultiSelectionEnabled(false);
            imgChooser.addChoosableFileFilter(new ImageFileFilter());
            imgChooser.setAcceptAllFileFilterUsed(false);

            int returnVal = imgChooser.showOpenDialog(ImportForm.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                addWorldImageButton.setText("Loading...");

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                toggleComponents(false);

                File imgFile = imgChooser.getSelectedFile();
                Logger.info("Selected world background image");

                loadWorldImage(imgFile);
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                LOGGER.severe("Error selecting world background images");
            } else {
                LOGGER.warning("No world background images selected");
            }
        });

        imageXOffsetSpinner.addChangeListener(e -> main.mainPanel.xBackgroundOffset = (int) ((JSpinner) e.getSource()).getValue());

        imageZOffsetSpinner.addChangeListener(e -> main.mainPanel.zBackgroundOffset = (int) ((JSpinner) e.getSource()).getValue());
    }

    public void loadWorldImage(File imgFile) {
        Thread exec = new Thread(() -> {
            try {
                final long nowMs = System.currentTimeMillis();

                currentBackgroundImage = ImageIO.read(imgFile);

                final long durMs = System.currentTimeMillis() - nowMs;

                Logger.info("Successfully loaded world background image in " + durMs + "ms.");
            } catch (IOException e) {
                LOGGER.severe("Error reading selected world background image:\n   " + Arrays.toString(e.getStackTrace()));
            }

            Toolkit.getDefaultToolkit().beep();
            addWorldImageButton.setText("New World Image");
            revalidate();
            repaint();
            main.setCursor(null);
            setCursor(null);

            toggleComponents(true);
            revalidate();
            repaint();
        });

        exec.start();
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

    public void toggleComponents(boolean value) {
        cancelButton.setEnabled(value);
        addWorldImageButton.setEnabled(value);
        selectedFileList.setEnabled(value);
        maxEntriesSpinner.setEnabled(value);
        imageXOffsetSpinner.setEnabled(value);
        imageZOffsetSpinner.setEnabled(value);
        fancyRenderingToggle.setEnabled(value);
        convertChunksToggle.setEnabled(value);
        mainSplitPane.setEnabled(value);
        addFileButton.setEnabled(value);
        removeFileButton.setEnabled(value);
        backgroundTypeChooser.setEnabled(value);
        backgroundTypeLabel.setEnabled(value);
        settingsTabs.setEnabled(value);

        importButton.setEnabled(value && selectedFileList.getLastVisibleIndex() > 0);
        if (appendButton != null) appendButton.setEnabled(value && selectedFileList.getLastVisibleIndex() > 0);
        dimensionChooserLabel.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Seed");
        dimensionChooser.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Seed");
        worldSeedLabel.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Seed");
        worldSeedTextField.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Seed");
        worldVersionLabel.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Seed");
        worldVersionChooser.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Seed");
        worldImageLabel.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Image");
        addWorldImageButton.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Image");
        imageZOffsetLabel.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Image");
        imageXOffsetLabel.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Image");
        imageZOffsetSpinner.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Image");
        imageXOffsetSpinner.setEnabled(value && backgroundTypeChooser.getSelectedItem() == "Image");
    }
}