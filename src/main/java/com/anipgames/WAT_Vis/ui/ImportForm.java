package com.anipgames.WAT_Vis.ui;

import com.seedfinding.mccore.version.MCVersion;
import com.anipgames.WAT_Vis.io.filters.ImageFileFilter;
import com.anipgames.WAT_Vis.io.filters.TextFileFilter;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.config.Settings;
import com.anipgames.WAT_Vis.util.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
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

import com.seedfinding.mccore.state.Dimension;
import com.anipgames.WAT_Vis.util.Utils;

public class ImportForm extends JDialog {
    private final PlayerTrackerDecoder main;
    private final Settings settings;

    private final ArrayList<File> currentFiles = new ArrayList<>();
    private BufferedImage currentBackgroundImage;

    private JButton importButton;
    private JButton appendButton;
    private JButton cancelButton;

    private JSplitPane mainSplitPane;
    private JTabbedPane settingsTabs;

    private LabeledComponent<JSpinner> maxEntriesSpinner;
    private LabeledComponent<JRadioButton> convertChunksToggle;
    private LabeledComponent<JRadioButton> fancyRenderingToggle;

    private LabeledComponent<JComboBox<String>> backgroundTypeChooser;

    private LabeledComponent<JComboBox<String>> dimensionChooser;
    private LabeledComponent<HintedInputField> worldSeedInputField;
    private LabeledComponent<JComboBox<MCVersion>> worldVersionChooser;
    private LabeledComponent<JComboBox<Integer>> threadCountChooser;

    private JButton addWorldImageButton;
    private LabeledComponent<JSpinner> imageXOffsetSpinner;
    private LabeledComponent<JSpinner> imageZOffsetSpinner;

    private JList<File> selectedFileList;
    private JButton removeFileButton;
    private JButton addFileButton;

    public ImportForm(PlayerTrackerDecoder main, Settings settings, boolean alreadyImported) {
        super(main);

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

        setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
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
                    Logger.err("Error dragging and dropping files onto import panel:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
                }
            }
        });

        Logger.info("File import pane opened from drag and drop");
    }

    private void initComponents(boolean alreadyImported) {
        GridBagConstraints gbc;

        JLabel titleText = new JLabel();
        titleText.setEnabled(true);
        Font titleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (titleFont != null) titleText.setFont(titleFont);
        titleText.setHorizontalAlignment(SwingConstants.CENTER);
        titleText.setHorizontalTextPosition(SwingConstants.CENTER);
        titleText.setText("Import Files");
        add(titleText, BorderLayout.NORTH);

        //region Bottom button UI
        JPanel bottomButtonPanel = new JPanel(new GridBagLayout());
        add(bottomButtonPanel, BorderLayout.SOUTH);

        cancelButton = new JButton("Cancel");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 10, 5, 0);
        bottomButtonPanel.add(cancelButton, gbc);

        importButton = new JButton(alreadyImported ? "Overwrite" : "Import");
        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 5, alreadyImported ? 0 : 10);
        bottomButtonPanel.add(importButton, gbc);

        if (alreadyImported) {
            appendButton = new JButton("Append");
            gbc.gridx++;
            gbc.insets = new Insets(0, 0, 5, 10);
            bottomButtonPanel.add(appendButton, gbc);
        }
        //endregion

        //region Split pane UI
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        mainSplitPane.setDividerLocation(255);
        mainSplitPane.setDividerSize(9);
        mainSplitPane.setEnabled(false);
        add(mainSplitPane, BorderLayout.CENTER);
        //endregion

        //region Import settings UI
        JPanel importSettingsPanel = new JPanel(new GridBagLayout());
        mainSplitPane.setLeftComponent(importSettingsPanel);

        JLabel importSettingsTitle = new JLabel();
        Font importSettingstitleFont = Utils.getFont(null, Font.PLAIN, 16, importSettingsTitle.getFont());
        if (importSettingstitleFont != null) importSettingsTitle.setFont(importSettingstitleFont);
        importSettingsTitle.setHorizontalAlignment(SwingConstants.CENTER);
        importSettingsTitle.setText("Options");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 10, 0, 0);
        importSettingsPanel.add(importSettingsTitle, gbc);

        settingsTabs = new JTabbedPane();
        settingsTabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        settingsTabs.setTabPlacement(SwingConstants.TOP);
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 0, 0);
        importSettingsPanel.add(settingsTabs, gbc);

        //region Decoding settings UI
        JPanel decodingSettings = new JPanel(new GridBagLayout());
        settingsTabs.addTab("Decoding", decodingSettings);

        maxEntriesSpinner = new LabeledComponent<>("Max Entries to Decode", new JSpinner());
        maxEntriesSpinner.getComponent().setValue(settings.maxDataEntries);
        maxEntriesSpinner.setToolTipText("The maximum amount of data entries to decode. Set to 0 to disable.");
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        decodingSettings.add(maxEntriesSpinner, gbc);

        convertChunksToggle = new LabeledComponent<>("Convert Chunks", new JRadioButton());
        convertChunksToggle.getComponent().setSelected(settings.convertChunkPosToBlockPos);
        gbc.gridy++;
        decodingSettings.add(convertChunksToggle, gbc);

        fancyRenderingToggle = new LabeledComponent<>("Fancy Rendering", new JRadioButton());
        fancyRenderingToggle.getComponent().setSelected(settings.fancyRendering);
        gbc.gridy++;
        decodingSettings.add(fancyRenderingToggle, gbc);
        //endregion

        //region Background import settings UI
        JPanel mapSettings = new JPanel(new GridBagLayout());
        settingsTabs.addTab("Background", mapSettings);

        backgroundTypeChooser = new LabeledComponent<>("Background Type", new JComboBox<>(new String[]{"Seed", "Image"}));
        backgroundTypeChooser.getComponent().setSelectedItem("Seed");
        gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        mapSettings.add(backgroundTypeChooser, gbc);

        //region Seed map UI
        dimensionChooser = new LabeledComponent<>("Dimension", new JComboBox<>(new String[]{"Overworld", "Nether", "End"}));
        dimensionChooser.getComponent().setSelectedItem("Overworld");
        gbc.gridy++;
        mapSettings.add(dimensionChooser, gbc);

        worldSeedInputField = new LabeledComponent<>("World Seed", new HintedInputField("Enter seed..."));
        worldSeedInputField.setToolTipText("The seed to use for mapping. This should be the seed of the minecraft world that the tracking data is from.");
        gbc.gridy++;
        mapSettings.add(worldSeedInputField, gbc);

        worldVersionChooser = new LabeledComponent<>("World Version", new JComboBox<>(Arrays.stream(MCVersion.values()).filter(v -> v.isNewerOrEqualTo(MCVersion.vb1_8_1)).toList().toArray(new MCVersion[0])));
        worldVersionChooser.setToolTipText("The minecraft version of the world that the tracking data is from.");
        gbc.gridy++;
        mapSettings.add(worldVersionChooser, gbc);

        threadCountChooser = new LabeledComponent<>("Mapping Threads", new JComboBox<>(IntStream.rangeClosed(1, Runtime.getRuntime().availableProcessors()).boxed().toList().toArray(new Integer[0])));
        threadCountChooser.setToolTipText("The number of threads to allocate to seed mapping.");
        gbc.gridy++;
        mapSettings.add(threadCountChooser, gbc);
        //endregion

        //region World image UI
        addWorldImageButton = new JButton();
        addWorldImageButton.setText(alreadyImported ? "New World Image" : "Add World Image");
        addWorldImageButton.setEnabled(false);
        gbc.gridy++;
        mapSettings.add(addWorldImageButton, gbc);

        imageXOffsetSpinner = new LabeledComponent<>("Image X Offset", new JSpinner());
        imageXOffsetSpinner.setToolTipText("How much to offset the X of image to center it on the world's (0,0). Can be changed later in the toolbar, under the \"Background\" tab.");
        imageXOffsetSpinner.setEnabled(false);
        gbc.gridy++;
        mapSettings.add(imageXOffsetSpinner, gbc);

        imageZOffsetSpinner = new LabeledComponent<>("Image Y Offset", new JSpinner());
        imageZOffsetSpinner.setToolTipText("How much to offset the Y of image to center it on the world's (0,0). Can be changed later in the toolbar, under the \"Background\" tab.");
        imageZOffsetSpinner.setEnabled(false);
        gbc.gridy++;
        mapSettings.add(imageZOffsetSpinner, gbc);
        //endregion
        //endregion
        //endregion

        //region File selector UI
        JPanel fileSelectorPanel = new JPanel(new GridBagLayout());
        mainSplitPane.setRightComponent(fileSelectorPanel);

        JLabel fileSelectorTitle = new JLabel();
        Font fileSelectorTitleFont = Utils.getFont(null, Font.PLAIN, 16, fileSelectorTitle.getFont());
        if (fileSelectorTitleFont != null) fileSelectorTitle.setFont(fileSelectorTitleFont);
        fileSelectorTitle.setHorizontalAlignment(SwingConstants.CENTER);
        fileSelectorTitle.setText("Select Files");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 10);
        fileSelectorPanel.add(fileSelectorTitle, gbc);

        JScrollPane selectedFileListScrollPane = new JScrollPane();
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 0, 10);
        fileSelectorPanel.add(selectedFileListScrollPane, gbc);

        selectedFileList = new JList<>();
        if(currentFiles.size() !=0){
            selectedFileList.setListData(currentFiles.toArray(new File[0]));
        }
        selectedFileListScrollPane.setViewportView(selectedFileList);

        JPanel selectorButtons = new JPanel(new GridBagLayout());
        gbc.gridy++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 10);
        fileSelectorPanel.add(selectorButtons, gbc);

        removeFileButton = new JButton("Remove Selected Files");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        selectorButtons.add(removeFileButton, gbc);

        addFileButton = new JButton("Add Files");
        gbc.gridx++;
        selectorButtons.add(addFileButton, gbc);
        //endregion

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
            if (backgroundTypeChooser.getComponent().getSelectedItem() == "Seed") {
                main.ConfirmImport(currentFiles, (MCVersion) worldVersionChooser.getComponent().getSelectedItem(), dimFromString((String) dimensionChooser.getComponent().getSelectedItem()), (Integer) threadCountChooser.getComponent().getSelectedItem(), worldSeedInputField.getComponent().getText(), true);
            } else {
                main.ConfirmImport(currentFiles, currentBackgroundImage, true);
            }
            this.setVisible(false);
        });

        if (alreadyImported) {
            appendButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);

            appendButton.addActionListener(event -> {
                if (backgroundTypeChooser.getComponent().getSelectedItem() == "Seed") {
                    main.ConfirmImport(currentFiles, (MCVersion) worldVersionChooser.getComponent().getSelectedItem(), dimFromString((String) dimensionChooser.getComponent().getSelectedItem()), (Integer) threadCountChooser.getComponent().getSelectedItem(), worldSeedInputField.getComponent().getText(), true);
                } else {
                    main.ConfirmImport(currentFiles, currentBackgroundImage, true);
                }
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
                Logger.err("Error selecting input files");
            } else {
                Logger.warn("No input files selected");
            }
        });

        removeFileButton.addActionListener(event -> {
            currentFiles.removeAll(selectedFileList.getSelectedValuesList());
            selectedFileList.setListData(currentFiles.toArray(new File[0]));
            importButton.setEnabled(currentFiles.size() > 0);
            if (appendButton != null) appendButton.setEnabled(currentFiles.size() > 0);
        });

        maxEntriesSpinner.getComponent().addChangeListener(e -> {
            settings.maxDataEntries = (int) ((JSpinner) e.getSource()).getValue();
            settings.SaveSettings();
        });

        fancyRenderingToggle.getComponent().addItemListener(event -> {
            settings.fancyRendering = (event.getStateChange() == ItemEvent.SELECTED);
            settings.toggleRenderMode();
            settings.SaveSettings();
        });

        backgroundTypeChooser.getComponent().addActionListener(event -> {
            boolean isSeed = backgroundTypeChooser.getComponent().getSelectedItem() == "Seed";

            dimensionChooser.setEnabled(isSeed);
            worldSeedInputField.getComponent().setEnabled(isSeed);
            worldVersionChooser.setEnabled(isSeed);

            addWorldImageButton.setEnabled(!isSeed);
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
                Logger.err("Error selecting world background images");
            } else {
                Logger.warn("No world background images selected");
            }
        });

        imageXOffsetSpinner.getComponent().addChangeListener(e -> main.mainPanel.xBackgroundOffset = (int) ((JSpinner) e.getSource()).getValue());

        imageZOffsetSpinner.getComponent().addChangeListener(e -> main.mainPanel.zBackgroundOffset = (int) ((JSpinner) e.getSource()).getValue());
    }

    public void loadWorldImage(File imgFile) {
        Thread exec = new Thread(() -> {
            try {
                final long nowMs = System.currentTimeMillis();

                currentBackgroundImage = ImageIO.read(imgFile);

                final long durMs = System.currentTimeMillis() - nowMs;

                Logger.info("Successfully loaded world background image in " + durMs + "ms.");
            } catch (IOException e) {
                Logger.err("Error reading selected world background image:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
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

    private Dimension dimFromString(String input) {
        return switch (input) {
            case "Overworld" -> Dimension.OVERWORLD;
            case "Nether" -> Dimension.NETHER;
            case "End" -> Dimension.END;
            default -> null;
        };
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
        settingsTabs.setEnabled(value);

        importButton.setEnabled(value && selectedFileList.getLastVisibleIndex() > 0);
        if (appendButton != null) appendButton.setEnabled(value && selectedFileList.getLastVisibleIndex() > 0);

        boolean isSeed = backgroundTypeChooser.getComponent().getSelectedItem() == "Seed";
        dimensionChooser.setEnabled(value && isSeed);
        worldSeedInputField.setEnabled(value && isSeed);
        worldVersionChooser.setEnabled(value && isSeed);
        threadCountChooser.setEnabled(value && isSeed);
        addWorldImageButton.setEnabled(value && !isSeed);
        imageZOffsetSpinner.setEnabled(value && !isSeed);
        imageXOffsetSpinner.setEnabled(value && !isSeed);
    }
}