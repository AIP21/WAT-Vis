package TrackerDecoderApp.ui;

import TrackerDecoderApp.PlayerTrackerDecoder;
import TrackerDecoderApp.Settings;
import TrackerDecoderApp.util.Logger;
//import com.intellij.uiDesigner.core.GridConstraints;
//import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Locale;

public class SettingsForm extends JDialog {
    public JLabel settingsTitle;
    public JPanel settingsPanel;
    public JToggleButton lightThemeButton;
    public JToggleButton darkThemeButton;
    public JPanel themeButtonContainer;
    public JRadioButton fancyRenderingToggle;
    public JRadioButton debugModeToggle;
    public JSlider fpsLimitSlider;
    public JPanel fpsLimitContainer;
    public JLabel fpsLimitLabel;
    public JLabel fpsLimitValue;
    public JPanel mouseSensitivityContainer;
    public JLabel sensitivityLabel;
    public JLabel sensitivityValue;
    public JSlider sensitivitySlider;
    public JPanel aboutPanel;
    public JTextPane aboutText;
    private PlayerTrackerDecoder main;
    private Settings settings;
    private Logger logger;

    public SettingsForm(PlayerTrackerDecoder main, Settings settings, Logger logger) {
        super(main, "TrackerDecoderApp.Settings");

        setModal(true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        pack();

        this.main = main;
        this.settings = settings;
        this.logger = logger;

        setSize(new Dimension(720, 480));
        setResizable(false);

        initComponents();

        logger.info("TrackerDecoderApp.Settings pane opened", 0);
    }

    private void initComponents() {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());//(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingsTitle = new JLabel("App TrackerDecoderApp.Settings");
        Font settingsTitleFont = getFont(null, -1, 26, settingsTitle.getFont());
        if (settingsTitleFont != null) {
            settingsTitle.setFont(settingsTitleFont);
        }
        panel1.add(settingsTitle, new GridBagConstraints(0, 0, 1, 1, 0, 0, 0, 0, null, null, null, 0, false));

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayoutManager(6, 1, new Insets(0, 0, 0, 0), -1, -1));
        settingsPanel.setBorder(BorderFactory.createTitledBorder(null, "", 0, 0, (Font) null, (Color) null));
        panel1.add(settingsPanel, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));

        JLabel label1 = new JLabel("Theme");
        settingsPanel.add(label1, new GridConstraints(0, 0, 1, 1, 0, 0, 0, 0, null, null, null, 0, false));

        themeButtonContainer = new JPanel();
        themeButtonContainer.setLayout(new GridLayoutManager(1, 2, new Insets(0, 25, 0, 25), -1, -1));
        settingsPanel.add(themeButtonContainer, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));

        lightThemeButton = new JToggleButton("", settings.uiTheme == PlayerTrackerDecoder.UITheme.Light);
        lightThemeButton.setIcon(main.lightThemeIcon);
        lightThemeButton.setToolTipText("Use a light TrackerDecoderApp.ui theme");
        themeButtonContainer.add(lightThemeButton, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));
        darkThemeButton = new JToggleButton("", settings.uiTheme == PlayerTrackerDecoder.UITheme.Dark);
        darkThemeButton.setIcon(main.darkThemeIcon);
        darkThemeButton.setToolTipText("Use a dark TrackerDecoderApp.ui theme");
        themeButtonContainer.add(darkThemeButton, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, null, null, null, 0, false));

        fancyRenderingToggle = new JRadioButton();
        fancyRenderingToggle.setText("Fancy Rendering");
        fancyRenderingToggle.setSelected(settings.fancyRendering);
        fancyRenderingToggle.setToolTipText("Use fancy rendering to improve visual fidelity at the cost of performance");
        settingsPanel.add(fancyRenderingToggle, new GridConstraints(2, 0, 1, 1, 0, 0, 3, 0, null, null, null, 0, false));

        debugModeToggle = new JRadioButton();
        debugModeToggle.setText("Debug Mode");
        debugModeToggle.setSelected(PlayerTrackerDecoder.debugMode);
        debugModeToggle.setToolTipText("Used for debugging, obviously. Debug mode makes logging more verbose, adds more info to the bottom TrackerDecoderApp.ui.toolbar, and does some other stuff.");
        settingsPanel.add(debugModeToggle, new GridConstraints(3, 0, 1, 1, 0, 0, 3, 0, null, null, null, 0, false));

        fpsLimitContainer = new JPanel();
        fpsLimitContainer.setLayout(new GridLayoutManager(1, 3, new Insets(0, 25, 0, 25), -1, -1, true, false));
        settingsPanel.add(fpsLimitContainer, new GridConstraints(4, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));

        fpsLimitLabel = new JLabel("Target Framerate");
        fpsLimitContainer.add(fpsLimitLabel, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null, 0, false));
        fpsLimitSlider = new JSlider(0, 1, 255, settings.fpsLimit);
        fpsLimitSlider.setMajorTickSpacing(30);
        fpsLimitSlider.setMinorTickSpacing(10);
        fpsLimitSlider.setPaintLabels(false);
        fpsLimitSlider.setPaintTicks(true);
        fpsLimitSlider.setSnapToTicks(true);
        fpsLimitSlider.setToolTipText("The maximum framerate to render at");
        fpsLimitContainer.add(fpsLimitSlider, new GridConstraints(0, 1, 1, 1, 0, 1, 4, 0, null, null, null, 0, false));
        fpsLimitValue = new JLabel(settings.fpsLimit + " FPS");
        fpsLimitContainer.add(fpsLimitValue, new GridConstraints(0, 2, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        mouseSensitivityContainer = new JPanel();
        mouseSensitivityContainer.setLayout(new GridLayoutManager(1, 3, new Insets(0, 25, 0, 25), -1, -1, true, false));
        settingsPanel.add(mouseSensitivityContainer, new GridConstraints(5, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));

        sensitivityLabel = new JLabel("Mouse Sensitivity");
        mouseSensitivityContainer.add(sensitivityLabel, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null, 0, false));
        sensitivitySlider = new JSlider(0, 0, 200, settings.mouseSensitivity);
        sensitivitySlider.setMajorTickSpacing(50);
        sensitivitySlider.setMinorTickSpacing(25);
        sensitivitySlider.setPaintLabels(false);
        sensitivitySlider.setPaintTicks(true);
        sensitivitySlider.setSnapToTicks(true);
        sensitivitySlider.setToolTipText("The maximum framerate to render at");
        mouseSensitivityContainer.add(sensitivitySlider, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, null, null, null, 0, false));
        sensitivityValue = new JLabel(settings.mouseSensitivity + "%");
        mouseSensitivityContainer.add(sensitivityValue, new GridConstraints(0, 2, 1, 1, 8, 0, 0, 0, null, null, null, 0, false));

        aboutPanel = new JPanel();
        aboutPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 25, 0, 25), -1, -1));
        panel1.add(aboutPanel, new GridConstraints(2, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0, false));
        aboutPanel.setBorder(BorderFactory.createTitledBorder((Border) null, "", 0, 0, (Font) null, (Color) null));
        aboutText = new JTextPane();
        aboutText.setContentType("text/html");
        aboutText.setEditable(false);
        aboutText.setText("<html>\n  <head>\n    \n  </head>\n  <body>\n    <center>\n      <h2>\n        <font face=\"Segoe UI\">ABOUT </font>\n      </h2>\n      <font face=\"Segoe UI\"><b>Player Tracker TrackerDecoderApp.Decoder</b> is a tool to decode \n      the data logged in the format used by the Minecraft mod WAT (Where are \n      they?)<br><a href=\"https://github.com/AIP21/TrackerDecoderApp\">Github \n      Page</a><br><a href=\"https://github.com/AIP21/WAT-mod\">WAT mod Github \n      Page</a><br><a href=\"https://github.com/AIP21/WAT-mod\">WAT mod Modrinth \n      Page</a><br></font>\n    </center>\n  </body>\n</html>\n");
        aboutPanel.add(aboutText, new GridConstraints(0, 0, 1, 1, 0, 3, 4, 4, null, new Dimension(150, 50), null, 0, false));
        add(panel1);

        setCallbacks();
    }

    private void setCallbacks() {
        lightThemeButton.addActionListener((event) -> {
            main.ChangeTheme(PlayerTrackerDecoder.UITheme.Light);
            SwingUtilities.updateComponentTreeUI(this);
            lightThemeButton.setSelected(true);
            darkThemeButton.setSelected(false);
            Font settingsTitleFont = getFont(null, -1, 26, settingsTitle.getFont());
            if (settingsTitleFont != null) {
                settingsTitle.setFont(settingsTitleFont);
            }
            revalidate();

            logger.info("Set the theme to light", 0);
        });

        darkThemeButton.addActionListener((event) -> {
            main.ChangeTheme(PlayerTrackerDecoder.UITheme.Dark);
            SwingUtilities.updateComponentTreeUI(this);
            lightThemeButton.setSelected(false);
            darkThemeButton.setSelected(true);
            Font settingsTitleFont = getFont(null, -1, 26, settingsTitle.getFont());
            if (settingsTitleFont != null) {
                settingsTitle.setFont(settingsTitleFont);
            }
            revalidate();

            logger.info("Set the theme to dark", 0);
        });

        fancyRenderingToggle.addItemListener((event) -> {
            settings.fancyRendering = event.getStateChange() == ItemEvent.SELECTED;
            settings.toggleRenderMode();
            settings.SaveSettings();

            logger.info("Toggled fancyRendering to: " + settings.fancyRendering, 0);
        });

        debugModeToggle.addItemListener((event) -> {
            PlayerTrackerDecoder.debugMode = event.getStateChange() == ItemEvent.SELECTED;
            settings.SaveSettings();

            logger.info("Toggled the not-so-secret DEBUG MODE (oooooh) to: " + PlayerTrackerDecoder.debugMode, 0);
        });

        fpsLimitSlider.addChangeListener((e) -> {
            settings.fpsLimit = ((JSlider) e.getSource()).getValue();
            fpsLimitValue.setText(settings.fpsLimit + " FPS");
            settings.SaveSettings();

            logger.info("Changed framerate limit to: " + settings.fpsLimit, 0);
        });

        sensitivitySlider.addChangeListener((e) -> {
            settings.mouseSensitivity = ((JSlider) e.getSource()).getValue();
            sensitivityValue.setText(settings.mouseSensitivity + "%");
            settings.SaveSettings();
            main.mainPanel.sensitivity = (float) settings.mouseSensitivity / 100.0F;

            logger.info("Changed mouse sensitivity to: " + settings.mouseSensitivity, 0);
        });
    }

    private Font getFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) {
            return null;
        } else {
            String resultName;
            Font font;
            if (fontName == null) {
                resultName = currentFont.getName();
            } else {
                font = new Font(fontName, Font.PLAIN, 10);
                if (font.canDisplay('a') && font.canDisplay('1')) {
                    resultName = fontName;
                } else {
                    resultName = currentFont.getName();
                }
            }

            font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
            boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
            Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : (new StyleContext()).getFont(font.getFamily(), font.getStyle(), font.getSize());
            return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
        }
    }
}