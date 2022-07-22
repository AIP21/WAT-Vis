package com.anipgames.WAT_Vis.ui;

import com.anipgames.WAT_Vis.io.Icons;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder.UITheme;
import com.anipgames.WAT_Vis.config.Settings;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class SettingsForm extends JDialog {
    public JLabel settingsTitleText;
    public JToggleButton lightThemeButton;
    public JToggleButton darkThemeButton;
    public LabeledComponent<JRadioButton> fancyRendering;
    public LabeledComponent<JRadioButton> debugMode;
    public LabeledComponent<JSlider> fpsLimit;
    public LabeledComponent<JSlider> mouseSensitivity;
    private Font settingsTitleFont;

    private final PlayerTrackerDecoder main;
    private final Settings settings;

    public SettingsForm(PlayerTrackerDecoder main, Settings settings) {
        super(main, "Settings");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        this.main = main;
        this.settings = settings;

        setSize(new Dimension(720, 480));
        setResizable(false);

        initComponents();

        Logger.info("Settings pane opened");
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        settingsTitleText = new JLabel("Settings");
        settingsTitleFont = Utils.getFont(null, Font.BOLD, 26, settingsTitleText.getFont());
        if (settingsTitleFont != null) {
            settingsTitleText.setFont(settingsTitleFont);
        }
        settingsTitleText.setHorizontalAlignment(SwingConstants.CENTER);
        settingsTitleText.setHorizontalTextPosition(SwingConstants.CENTER);
        add(settingsTitleText, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());
        add(settingsPanel, BorderLayout.CENTER);

        GridBagConstraints gbc;

        JLabel themeTitle = new JLabel("Theme");
        themeTitle.setHorizontalAlignment(SwingConstants.CENTER);
        themeTitle.setHorizontalTextPosition(SwingConstants.CENTER);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        settingsPanel.add(themeTitle, gbc);

        //region Theme buttons
        JPanel themeButtonContainer = new JPanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);
        themeButtonContainer.setLayout(new GridBagLayout());
        settingsPanel.add(themeButtonContainer, gbc);

        lightThemeButton = new JToggleButton("", settings.uiTheme == UITheme.Light);
        lightThemeButton.setIcon(Icons.getIcon("lightThemePreview"));
        lightThemeButton.setToolTipText("Use a light ui theme");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        themeButtonContainer.add(lightThemeButton, gbc);

        darkThemeButton = new JToggleButton("", settings.uiTheme == UITheme.Dark);
        darkThemeButton.setIcon(Icons.getIcon("darkThemePreview"));
        darkThemeButton.setToolTipText("Use a dark ui theme");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.EAST;
        themeButtonContainer.add(darkThemeButton, gbc);
        //endregion

        fancyRendering = new LabeledComponent<>("Fancy Rendering", new JRadioButton("", settings.fancyRendering));
        fancyRendering.setToolTipText("Use fancy rendering to improve visual fidelity at the cost of performance");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 0, 10);
        settingsPanel.add(fancyRendering, gbc);

        debugMode = new LabeledComponent<>("Debug Mode", new JRadioButton("", PlayerTrackerDecoder.DEBUG));
        debugMode.setToolTipText("Used for debugging, obviously. Debug mode makes logging more verbose, adds more info to the bottom toolbar, and does some other stuff.");
        gbc.gridy++;
        settingsPanel.add(debugMode, gbc);

        fpsLimit = new LabeledComponent<>(String.format("Target Framerate: %d FPS", settings.fpsLimit), new JSlider(0, 1, 255, settings.fpsLimit), LabeledComponent.LEFT, 0.5, 1);
        fpsLimit.setToolTipText("The maximum framerate to render at");
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;

        JSlider fpsLimitSlider = fpsLimit.getComponent();
        fpsLimitSlider.setSnapToTicks(true);

        settingsPanel.add(fpsLimit, gbc);

        mouseSensitivity = new LabeledComponent<>(String.format("Mouse Sensitivity: %d%%", settings.mouseSensitivity), new JSlider(0, 0, 200, settings.mouseSensitivity), LabeledComponent.LEFT, 0.5, 1);
        mouseSensitivity.setToolTipText("How sensitive mouse inputs should be");
        gbc.gridy++;

        JSlider sensitivitySlider = mouseSensitivity.getComponent();
        sensitivitySlider.setSnapToTicks(true);

        settingsPanel.add(mouseSensitivity, gbc);

        setCallbacks();
    }

    private void setCallbacks() {
        lightThemeButton.addActionListener((event) -> {
            main.ChangeTheme(UITheme.Light);
            SwingUtilities.updateComponentTreeUI(this);
            lightThemeButton.setSelected(true);
            darkThemeButton.setSelected(false);
            settingsTitleText.setFont(settingsTitleFont);
            revalidate();

            Logger.info("Set the theme to light");
        });

        darkThemeButton.addActionListener((event) -> {
            main.ChangeTheme(UITheme.Dark);
            SwingUtilities.updateComponentTreeUI(this);
            lightThemeButton.setSelected(false);
            darkThemeButton.setSelected(true);
            settingsTitleText.setFont(settingsTitleFont);
            revalidate();

            Logger.info("Set the theme to dark");
        });

        fancyRendering.getComponent().addItemListener((event) -> {
            settings.fancyRendering = event.getStateChange() == ItemEvent.SELECTED;
            settings.toggleRenderMode();
            settings.SaveSettings();

            Logger.info("Toggled fancy rendering to: " + settings.fancyRendering);
        });

        debugMode.getComponent().addItemListener((event) -> {
            PlayerTrackerDecoder.DEBUG = event.getStateChange() == ItemEvent.SELECTED;
            settings.SaveSettings();

            Logger.info("Toggled the not-so-secret DEBUG MODE (oooooh) to: " + PlayerTrackerDecoder.DEBUG);
        });

        fpsLimit.getComponent().addChangeListener((e) -> {
            settings.fpsLimit = ((JSlider) e.getSource()).getValue();
            fpsLimit.setLabelText(String.format("Target Framerate: %d FPS", settings.fpsLimit));
            settings.SaveSettings();

            Logger.info("Changed framerate limit to: " + settings.fpsLimit);
        });

        fpsLimit.getComponent().addChangeListener((e) -> {
            settings.mouseSensitivity = ((JSlider) e.getSource()).getValue();
            fpsLimit.setLabelText(String.format("Mouse Sensitivity: %d%%", settings.mouseSensitivity));
            settings.SaveSettings();
            main.mainPanel.sensitivity = (float) settings.mouseSensitivity / 100.0F;

            Logger.info("Changed mouse sensitivity to: " + settings.mouseSensitivity);
        });
    }
}