package src.ui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import src.Logger;
import src.PlayerTrackerDecoder;
import src.Settings;
import src.PlayerTrackerDecoder.UITheme;

public class SettingsForm extends JFrame {
    public JLabel settingsTitle;
    public JPanel settingsPanel;
    public JToggleButton lightThemeButton;
    public JToggleButton darkThemeButton;
    public JPanel themeButtonContainer;
    public JRadioButton antialiasingToggle;
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
        this.main = main;
        this.setTitle("Settings");
        this.settings = settings;
        this.logger = logger;
        this.setSize(new Dimension(720, 480));
        this.initComponents();
    }

    private void initComponents() {
        JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        this.settingsTitle = new JLabel();
        Font settingsTitleFont = this.getFont((String)null, -1, 26, this.settingsTitle.getFont());
        if (settingsTitleFont != null) {
            this.settingsTitle.setFont(settingsTitleFont);
        }

        this.settingsTitle.setText("App Settings");
        panel1.add(this.settingsTitle, new GridConstraints(0, 0, 1, 1, 0, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.settingsPanel = new JPanel();
        this.settingsPanel.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(this.settingsPanel, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.settingsPanel.setBorder(BorderFactory.createTitledBorder((Border)null, "", 0, 0, (Font)null, (Color)null));
        this.antialiasingToggle = new JRadioButton();
        this.antialiasingToggle.setText("Antialiasing");
        this.antialiasingToggle.setSelected(this.settings.antialiasing);
        this.antialiasingToggle.setToolTipText("Use antialiasing to smooth out jagged edges on the screen");
        this.settingsPanel.add(this.antialiasingToggle, new GridConstraints(2, 0, 1, 1, 0, 0, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.fpsLimitContainer = new JPanel();
        this.fpsLimitContainer.setLayout(new GridLayoutManager(1, 3, new Insets(0, 25, 0, 25), -1, -1, true, false));
        this.settingsPanel.add(this.fpsLimitContainer, new GridConstraints(3, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.fpsLimitLabel = new JLabel();
        this.fpsLimitLabel.setText("Target Framerate");
        this.fpsLimitContainer.add(this.fpsLimitLabel, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.fpsLimitSlider = new JSlider(0, 10, 255, this.settings.fpsLimit);
        this.fpsLimitSlider.setMajorTickSpacing(30);
        this.fpsLimitSlider.setMinorTickSpacing(10);
        this.fpsLimitSlider.setPaintLabels(false);
        this.fpsLimitSlider.setPaintTicks(true);
        this.fpsLimitSlider.setSnapToTicks(true);
        this.fpsLimitSlider.setToolTipText("The maximum framerate to render at");
        this.fpsLimitContainer.add(this.fpsLimitSlider, new GridConstraints(0, 1, 1, 1, 0, 1, 4, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.fpsLimitValue = new JLabel();
        int var10001 = this.settings.fpsLimit;
        this.fpsLimitValue.setText(var10001 + " FPS");
        this.fpsLimitContainer.add(this.fpsLimitValue, new GridConstraints(0, 2, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        JLabel label1 = new JLabel();
        label1.setText("Theme");
        this.settingsPanel.add(label1, new GridConstraints(0, 0, 1, 1, 0, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.themeButtonContainer = new JPanel();
        this.themeButtonContainer.setLayout(new GridLayoutManager(1, 2, new Insets(0, 25, 0, 25), -1, -1));
        this.settingsPanel.add(this.themeButtonContainer, new GridConstraints(1, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.lightThemeButton = new JToggleButton("", this.settings.uiTheme == UITheme.Light);
        this.lightThemeButton.setIcon(this.main.lightThemeIcon);
        this.lightThemeButton.setToolTipText("Use a light ui theme");
        this.themeButtonContainer.add(this.lightThemeButton, new GridConstraints(0, 0, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.darkThemeButton = new JToggleButton("", this.settings.uiTheme == UITheme.Dark);
        this.darkThemeButton.setIcon(this.main.darkThemeIcon);
        this.darkThemeButton.setToolTipText("Use a dark ui theme");
        this.themeButtonContainer.add(this.darkThemeButton, new GridConstraints(0, 1, 1, 1, 0, 1, 3, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.mouseSensitivityContainer = new JPanel();
        this.mouseSensitivityContainer.setLayout(new GridLayoutManager(1, 3, new Insets(0, 25, 0, 25), -1, -1, true, false));
        this.settingsPanel.add(this.mouseSensitivityContainer, new GridConstraints(4, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.sensitivityLabel = new JLabel();
        this.sensitivityLabel.setText("Mouse Sensitivity");
        this.mouseSensitivityContainer.add(this.sensitivityLabel, new GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.sensitivitySlider = new JSlider(0, 1, 200, this.settings.mouseSensitivity);
        this.sensitivitySlider.setMajorTickSpacing(30);
        this.sensitivitySlider.setMinorTickSpacing(10);
        this.sensitivitySlider.setPaintLabels(false);
        this.sensitivitySlider.setPaintTicks(true);
        this.sensitivitySlider.setSnapToTicks(true);
        this.sensitivitySlider.setToolTipText("The maximum framerate to render at");
        this.mouseSensitivityContainer.add(this.sensitivitySlider, new GridConstraints(0, 1, 1, 1, 8, 1, 4, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.sensitivityValue = new JLabel();
        this.sensitivityValue.setText(this.settings.mouseSensitivity + "%");
        this.mouseSensitivityContainer.add(this.sensitivityValue, new GridConstraints(0, 2, 1, 1, 8, 0, 0, 0, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.aboutPanel = new JPanel();
        this.aboutPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 25, 0, 25), -1, -1));
        panel1.add(this.aboutPanel, new GridConstraints(2, 0, 1, 1, 0, 3, 3, 3, (Dimension)null, (Dimension)null, (Dimension)null, 0, false));
        this.aboutPanel.setBorder(BorderFactory.createTitledBorder((Border)null, "", 0, 0, (Font)null, (Color)null));
        this.aboutText = new JTextPane();
        this.aboutText.setContentType("text/html");
        this.aboutText.setEditable(false);
        this.aboutText.setText("<html>\n  <head>\n    \n  </head>\n  <body>\n    <center>\n      <h2>\n        <font face=\"Segoe UI\">ABOUT </font>\n      </h2>\n      <font face=\"Segoe UI\"><b>Player Tracker Decoder</b> is a tool to decode \n      the data logged in the format used by the Minecraft mod WAT (Where are \n      they?)<br><a href=\"https://github.com/AIP21/TrackerDecoderApp\">Github \n      Page</a><br><a href=\"https://github.com/AIP21/WAT-mod\">WAT mod Github \n      Page</a><br><a href=\"https://github.com/AIP21/WAT-mod\">WAT mod Modrinth \n      Page</a><br></font>\n    </center>\n  </body>\n</html>\n");
        this.aboutPanel.add(this.aboutText, new GridConstraints(0, 0, 1, 1, 0, 3, 4, 4, (Dimension)null, new Dimension(150, 50), (Dimension)null, 0, false));
        this.add(panel1);
        this.setCallbacks();
    }

    private void setCallbacks() {
        this.lightThemeButton.addActionListener((event) -> {
            this.main.ChangeTheme(UITheme.Light);
            SwingUtilities.updateComponentTreeUI(this);
            this.lightThemeButton.setSelected(true);
            this.darkThemeButton.setSelected(false);
        });
        this.darkThemeButton.addActionListener((event) -> {
            this.main.ChangeTheme(UITheme.Dark);
            SwingUtilities.updateComponentTreeUI(this);
            this.lightThemeButton.setSelected(false);
            this.darkThemeButton.setSelected(true);
        });
        this.antialiasingToggle.addItemListener((event) -> {
            this.settings.antialiasing = event.getStateChange() == ItemEvent.SELECTED;
            this.settings.SaveSettings();
        });
        this.fpsLimitSlider.addChangeListener((e) -> {
            this.settings.fpsLimit = ((JSlider)e.getSource()).getValue();
            this.fpsLimitValue.setText(this.settings.fpsLimit + " FPS");
            this.settings.SaveSettings();
        });
        this.sensitivitySlider.addChangeListener((e) -> {
            this.settings.mouseSensitivity = ((JSlider)e.getSource()).getValue();
            this.sensitivityValue.setText(this.settings.mouseSensitivity + "%");
            this.settings.SaveSettings();
            this.main.mainPanel.sensitivity = (float)this.settings.mouseSensitivity / 100.0F;
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