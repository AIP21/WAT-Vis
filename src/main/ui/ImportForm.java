package src.main.ui;

import src.main.importing.filters.ImageFileFilter;
import src.main.importing.filters.TextFileFilter;
import src.main.PlayerTrackerDecoder;
import src.main.Settings;
import src.main.util.Logger;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.List;
import java.util.*;

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
    private JPanel importSettingsPanel;
    private JLabel importSettingsTitle;
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
    private JLabel worldImageXOffset;
    private JLabel worldImageZOffset;
    private JLabel worldImageLabel;
    private JComboBox<String> dimensionChooser;
    private JLabel dimensionChooserLabel;

    private PlayerTrackerDecoder main;
    private Settings settings;
    private Logger logger;

    private ArrayList<File> currentFiles = new ArrayList<>();

    public ImportForm(PlayerTrackerDecoder main, Settings settings, Logger logger, boolean alreadyImported) {
        super(main, "Import Files");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        this.main = main;
        this.settings = settings;
        this.logger = logger;

        setSize(new Dimension(720, 480));
        setResizable(false);

        initComponents(alreadyImported);

        logger.info("File import pane opened", 0);
    }

    public ImportForm(PlayerTrackerDecoder main, Settings settings, Logger logger, DropTargetDropEvent evt, boolean alreadyImported) {
        super(main, "Import Files");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        this.main = main;
        this.settings = settings;
        this.logger = logger;

        setSize(new Dimension(720, 480));
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
        } catch (Exception e) {
            logger.error("Error dragging and dropping files onto import panel: " + Arrays.toString(e.getStackTrace()));
        }

        logger.info("File import pane opened from drag and drop", 0);
    }

    private void initComponents(boolean alreadyImported) {
//        setLayout(new BorderLayout(0, 0));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        bottomPanel.add(bottomButtonPanel, gbc);

        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        bottomButtonPanel.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        importButton = new JButton();
        importButton.setRolloverEnabled(true);
        importButton.setEnabled(false);
        importButton.setText("Import");
        bottomButtonPanel.add(importButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        if (alreadyImported) {
            appendButton = new JButton();
            appendButton.setRolloverEnabled(true);
            appendButton.setEnabled(false);
            appendButton.setText("Append");
            importButton.setText("Overwrite");
            bottomButtonPanel.add(appendButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        }

        loadingProgressBar = new JProgressBar();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(loadingProgressBar, gbc);

        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(spacer1, gbc);

        titleText = new JLabel();
        Font TitleFont = this.getFont(null, -1, 26, titleText.getFont());
        if (TitleFont != null) titleText.setFont(TitleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        titleText.setText("Import Files");
        add(titleText, BorderLayout.NORTH);

        mainSplitPane = new JSplitPane();
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setDividerLocation(275);
        mainSplitPane.setDividerSize(9);
        mainSplitPane.setEnabled(true);
        add(mainSplitPane, BorderLayout.CENTER);

        fileSelectorPanel = new JPanel();
        fileSelectorPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainSplitPane.setRightComponent(fileSelectorPanel);

        selectorButtons = new JPanel();
        selectorButtons.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        fileSelectorPanel.add(selectorButtons, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));

        addFileButton = new JButton();
        addFileButton.setText("Add");
        addFileButton.setToolTipText("Add Files");
        selectorButtons.add(addFileButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        removeFileButton = new JButton();
        removeFileButton.setText("Remove");
        removeFileButton.setToolTipText("Remove Files");
        selectorButtons.add(removeFileButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        fileSelectorTitle = new JLabel();
        Font FileSelectorTitleFont = this.getFont(null, -1, 16, fileSelectorTitle.getFont());
        if (FileSelectorTitleFont != null) fileSelectorTitle.setFont(FileSelectorTitleFont);
        fileSelectorTitle.setText("Select Files");
        fileSelectorPanel.add(fileSelectorTitle, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        selectedFileListScrollPane = new JScrollPane();
        fileSelectorPanel.add(selectedFileListScrollPane, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

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
                    logger.error("Error dragging and dropping files onto import panel: " + Arrays.toString(ex.getStackTrace()));
                }
            }
        });

        importSettingsPanel = new JPanel();
        importSettingsPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainSplitPane.setLeftComponent(importSettingsPanel);

        importSettingsTitle = new JLabel();
        Font ImportSettingsTitleFont = this.getFont(null, -1, 16, importSettingsTitle.getFont());
        if (ImportSettingsTitleFont != null) importSettingsTitle.setFont(ImportSettingsTitleFont);
        importSettingsTitle.setText("Import Settings");
        importSettingsPanel.add(importSettingsTitle, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        convertChunksToggle = new JRadioButton();
        convertChunksToggle.setSelected(settings.convertChunkPosToBlockPos);
        convertChunksToggle.setHideActionText(false);
        convertChunksToggle.setText("");
        importSettingsPanel.add(convertChunksToggle, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        fancyRenderingToggle = new JRadioButton();
        fancyRenderingToggle.setHideActionText(true);
        fancyRenderingToggle.setSelected(settings.fancyRendering);
        fancyRenderingToggle.setText("");
        importSettingsPanel.add(fancyRenderingToggle, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        convertChunksLabel = new JLabel();
        convertChunksLabel.setText(" Convert Chunks");
        importSettingsPanel.add(convertChunksLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        fancyRenderingLabel = new JLabel();
        fancyRenderingLabel.setText(" Fancy Rendering");
        importSettingsPanel.add(fancyRenderingLabel, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        maxEntriesSpinner = new JSpinner();
        maxEntriesSpinner.setValue(settings.maxDataEntries);
        maxEntriesSpinner.setToolTipText("The maximum amount of data entries to decode. Set to 0 to disable");
        importSettingsPanel.add(maxEntriesSpinner, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        maxEntriesSpinnerLabel = new JLabel();
        maxEntriesSpinnerLabel.setText("Max Entries");
        importSettingsPanel.add(maxEntriesSpinnerLabel, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        worldImageXOffset = new JLabel();
        worldImageXOffset.setText("Image X Offset");
        importSettingsPanel.add(worldImageXOffset, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        imageXOffsetSpinner = new JSpinner();
        imageXOffsetSpinner.setToolTipText("How much to offset the x of image to center it on the world's (0,0). Can be changed later in the data settings.");
        importSettingsPanel.add(imageXOffsetSpinner, new com.intellij.uiDesigner.core.GridConstraints(6, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        imageZOffsetSpinner = new JSpinner();
        imageZOffsetSpinner.setToolTipText("How much to offset the y of image to center it on the world's (0,0). Can be changed later in the data settings.");
        importSettingsPanel.add(imageZOffsetSpinner, new com.intellij.uiDesigner.core.GridConstraints(7, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        addWorldImageButton = new JButton();
        addWorldImageButton.setText("Add World Image");
        importSettingsPanel.add(addWorldImageButton, new com.intellij.uiDesigner.core.GridConstraints(5, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        worldImageZOffset = new JLabel();
        worldImageZOffset.setText("Image Y Offset");
        importSettingsPanel.add(worldImageZOffset, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        worldImageLabel = new JLabel();
        worldImageLabel.setText(" World Image");
        importSettingsPanel.add(worldImageLabel, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        dimensionChooserLabel = new JLabel();
        dimensionChooserLabel.setText("Dimension");
        importSettingsPanel.add(dimensionChooserLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        dimensionChooser = new JComboBox<String>(new String[]{"Overworld", "Nether", "End"});
        dimensionChooser.setSelectedItem("Overworld");
        importSettingsPanel.add(dimensionChooser, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxEntriesSpinnerLabel.setLabelFor(maxEntriesSpinner);
        worldImageXOffset.setLabelFor(imageXOffsetSpinner);
        worldImageZOffset.setLabelFor(imageZOffsetSpinner);

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
            main.ConfirmImport(currentFiles, true);
            this.setVisible(false);
        });

        if(alreadyImported){
            appendButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);

            appendButton.addActionListener(event -> {
                main.ConfirmImport(currentFiles, false);
                this.setVisible(false);
            });
        }

        addFileButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("inputs"));
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
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                logger.error("Error selecting input files");
            } else {
                logger.warn("No input files selected");
            }
        });

        removeFileButton.addActionListener(event -> {
            currentFiles.removeAll(selectedFileList.getSelectedValuesList());
            selectedFileList.setListData(currentFiles.toArray(new File[0]));
            importButton.setEnabled(currentFiles.size() > 0);
        });

        if (PlayerTrackerDecoder.debugMode) {
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

        addWorldImageButton.addActionListener(event -> {
            logger.info("Opening world background image dialog", 0);

            JFileChooser imgChooser = new JFileChooser("worldImages");
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
                logger.info("Selected world background image: " + imgFile, 0);

                main.LoadWorldImage(imgFile, worldImageLabel, addWorldImageButton, this);
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                logger.error("Error selecting world background images");
            } else {
                logger.warn("No world background images selected");
            }
        });

        imageXOffsetSpinner.addChangeListener(e -> main.mainPanel.xBackgroundOffset = (int) ((JSpinner) e.getSource()).getValue());

        imageZOffsetSpinner.addChangeListener(e -> main.mainPanel.zBackgroundOffset = (int) ((JSpinner) e.getSource()).getValue());
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
        importButton.setEnabled(value);
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
        dimensionChooser.setEnabled(value);
    }
}