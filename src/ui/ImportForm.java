package src.ui;

import src.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ImportForm extends JFrame {
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
    private JButton confirmButton;
    private JButton cancelButton;
    private JPanel confirmationButtonPanel;
    private JRadioButton convertChunksToggle;
    private JRadioButton antialiasingToggle;
    private JSplitPane mainSplitPane;
    private JSpinner maxEntriesSpinner;
    private JLabel maxEntriesSpinnerLabel;
    private JLabel convertChunksLabel;
    private JSpinner imageXOffsetSpinner;
    private JSpinner imageZOffsetSpinner;
    private JButton addWorldImageButton;
    private JLabel antialiasingLabel;
    private JLabel worldImageXOffset;
    private JLabel worldImageZOffset;
    private JLabel worldImageLabel;
    private JComboBox<String> dimensionChooser;
    private JLabel dimensionChooserLabel;

    private PlayerTrackerDecoder main;
    private Settings settings;
    private Logger logger;

    private final boolean USE_MY_DEFAULTS = true;

    private ArrayList<File> currentFiles = new ArrayList<>();

    public ImportForm(PlayerTrackerDecoder main, Settings settings, Logger logger) {
        this.main = main;
        this.setTitle("Import Files");
        this.settings = settings;
        this.logger = logger;
        setSize(new Dimension(720, 480));
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        confirmationButtonPanel = new JPanel();
        confirmationButtonPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        bottomPanel.add(confirmationButtonPanel, gbc);
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        confirmationButtonPanel.add(cancelButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        confirmButton = new JButton();
        confirmButton.setHideActionText(false);
        confirmButton.setOpaque(false);
        confirmButton.setRolloverEnabled(true);
        confirmButton.setSelected(true);
        confirmButton.setText("Confirm");
        confirmationButtonPanel.add(confirmButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
        selectedFileListScrollPane.setViewportView(selectedFileList);
        importSettingsPanel = new JPanel();
        importSettingsPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 2, new Insets(0, 0, 0, 0), -1, -1));
        mainSplitPane.setLeftComponent(importSettingsPanel);
        importSettingsTitle = new JLabel();
        Font ImportSettingsTitleFont = this.getFont(null, -1, 16, importSettingsTitle.getFont());
        if (ImportSettingsTitleFont != null) importSettingsTitle.setFont(ImportSettingsTitleFont);
        importSettingsTitle.setText("Import Settings");
        importSettingsPanel.add(importSettingsTitle, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        convertChunksToggle = new JRadioButton();
        convertChunksToggle.setHideActionText(false);
        convertChunksToggle.setText("");
        importSettingsPanel.add(convertChunksToggle, new com.intellij.uiDesigner.core.GridConstraints(3, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        antialiasingToggle = new JRadioButton();
        antialiasingToggle.setHideActionText(true);
        antialiasingToggle.setSelected(true);
        antialiasingToggle.setText("");
        importSettingsPanel.add(antialiasingToggle, new com.intellij.uiDesigner.core.GridConstraints(4, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        convertChunksLabel = new JLabel();
        convertChunksLabel.setText(" Convert Chunks");
        importSettingsPanel.add(convertChunksLabel, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        antialiasingLabel = new JLabel();
        antialiasingLabel.setText("Antialiasing");
        importSettingsPanel.add(antialiasingLabel, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        maxEntriesSpinner = new JSpinner();
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
        imageXOffsetSpinner.setToolTipText("How much to offset the y of image to center it on the world's (0,0). Can be changed later in the data settings.");
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

        setCallbacks();
    }

    private void setCallbacks() {
        confirmButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);

        confirmButton.addActionListener(event -> {
            main.ConfirmImport(currentFiles);
            this.setVisible(false);
        });

        cancelButton.addActionListener(event -> this.setVisible(false));

        addFileButton.addActionListener(event -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("inputs"));
            chooser.setMultiSelectionEnabled(true);
            chooser.addChoosableFileFilter(new TextFileFilter());
            chooser.setAcceptAllFileFilterUsed(false);

            int returnVal = chooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                currentFiles.addAll(Arrays.asList(chooser.getSelectedFiles()));
                selectedFileList.setListData(currentFiles.toArray(new File[0]));
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                logger.Log("Error selecting input files", Logger.MessageType.ERROR);
            } else {
                logger.Log("No input files selected", Logger.MessageType.WARNING);
            }

            confirmButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);
        });

        removeFileButton.addActionListener(event -> {
            currentFiles.removeAll(selectedFileList.getSelectedValuesList());
            selectedFileList.setListData(currentFiles.toArray(new File[0]));
            confirmButton.setEnabled(selectedFileList.getLastVisibleIndex() > 0);
        });

        if (USE_MY_DEFAULTS) {
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

        antialiasingToggle.addItemListener(event -> {
            settings.antialiasing = (event.getStateChange() == ItemEvent.SELECTED);
            settings.SaveSettings();
        });

        addWorldImageButton.addActionListener(event -> {
            logger.Log("Opening world background image dialog", Logger.MessageType.INFO);
            addWorldImageButton.setEnabled(false);

            JFileChooser imgChooser = new JFileChooser("worldImages");
            imgChooser.setMultiSelectionEnabled(false);
            imgChooser.addChoosableFileFilter(new ImageFileFilter());
            imgChooser.setAcceptAllFileFilterUsed(false);

            int returnVal = imgChooser.showOpenDialog(ImportForm.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File imgFile = imgChooser.getSelectedFile();
                logger.Log("Selected world background image: " + imgFile, Logger.MessageType.INFO);

                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                main.LoadWorldImage(imgFile);

                Toolkit.getDefaultToolkit().beep();
                worldImageLabel.setText(" World Image [Imported]");
                addWorldImageButton.setText("New World Image");
                setCursor(null);
            } else if (returnVal == JFileChooser.ERROR_OPTION) {
                logger.Log("Error selecting world background images", Logger.MessageType.ERROR);
            } else {
                logger.Log("No world background images selected", Logger.MessageType.WARNING);
            }

            addWorldImageButton.setEnabled(true);
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
}