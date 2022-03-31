import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.time.LocalDateTime;
import javax.swing.JToolBar;
import java.util.ArrayList;
import java.time.LocalDate;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;
import java.awt.event.*;

public class Main extends JFrame {

    public Panel mainPanel;
    public JScrollPane scrollPane;

    private JFileChooser chooser;
    private JMenuBar menuBar;
    private JMenuBar bottomMenuBar;
    
    private JMenu dataFileMenu;
    private JMenuItem dataFileImportButton;
    
    private JToolBar dataSettingsToolBar;
    private boolean alreadyImported = false;

    private JToggleButton singleDayToggle;
    private JSlider singleDaySlider;
    private JLabel selectedSingleDayLabel;
    private LocalDate selectedDay;
    private JComboBox<LocalDate> dateStartChooser;
    private JComboBox<LocalDate> dateEndChooser;
    public LocalDate startDate;
    public LocalDate endDate;
    
    private JComboBox<Decoder.DrawType> drawTypeChooser;
    
    private ArrayList<JMenuItem> playerColorButtons;
    
    private JSlider sizeSlider;
    private JLabel sizeLabel;
    private JLabel sizeTypeLabel;
    
    private JSlider lineThresholdSlider;
    private JLabel lineThresholdLabel;
    
    private JToggleButton fancyLinesToggle;
    
    private JToggleButton showHiddenLinesToggle;
    
    private File[] files;
    
    private Decoder decoder;
    
    private Settings settings;
    
    private ArrayList<LocalDate> logDates;
    
    public static void main() {
        // Create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main myFrame = new Main();
                myFrame.setVisible(true);
            }
        });
    }

    public Main() {
        settings = new Settings();
        decoder = new Decoder(settings);
        initMainFrame();

        //Set up the file chooser
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setMultiSelectionEnabled(true);
        chooser.addChoosableFileFilter(new TextFileFilter());
        
        // Set up the menubar
        menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setJMenuBar(menuBar);

        // Create file selector
        dataFileMenu = new JMenu("Data");
        menuBar.add(dataFileMenu);
        
        dataFileImportButton = new JMenuItem("Import");
        dataFileMenu.add(dataFileImportButton);
        
        dataSettingsToolBar = new JToolBar("Data Settings");
        dataSettingsToolBar.setVisible(false);
        menuBar.add(dataSettingsToolBar);

        bottomMenuBar = new JMenuBar();
        add(bottomMenuBar, BorderLayout.SOUTH);

        JLabel label = new JLabel();
        mainPanel.renderedPointsLabel = label;
        bottomMenuBar.add(label);
        bottomMenuBar.add(mainPanel.CoordinateLabel);
        bottomMenuBar.add(mainPanel.SelectedEntryLabel);
        
        dataFileImportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                //show file chooser dialog
                int returnVal = chooser.showOpenDialog(Main.this);
                
                files = chooser.getSelectedFiles();
                
                if(files.length != 0) {
                    try {
                        decodeAndDisplay();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    mainPanel.SetInitialSize();
                    
                    initDataSettingsToolBar(alreadyImported);
                    dataSettingsToolBar.setVisible(true);
                    mainPanel.SelectedEntryLabel.setVisible(true);

                    //initListeners();
                    print("Selected files: " + files.length);
                    alreadyImported = true;
                }
            }
        });
    }
    
    private void initMainFrame(){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        
        setTitle("Player Tracker Decoder App - v0.0.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.DARK_GRAY);
        
        // Create the display panel and initialize the render thread
        mainPanel = new Panel(settings);
        (new Thread(mainPanel)).start();
        mainPanel.setDoubleBuffered(true);
        
        scrollPane = new JScrollPane(mainPanel);
        mainPanel.CoordinateLabel = new JLabel();
        mainPanel.CoordinateLabel.setText("   (0, 0) ");
        mainPanel.SelectedEntryLabel = new JLabel();
        mainPanel.SelectedEntryLabel.setVisible(false);
        scrollPane.setDoubleBuffered(true);
        add(scrollPane);
        add(mainPanel);
        mainPanel.setVisible(true);
        revalidate();
    }
    
    private void initDataSettingsToolBar(boolean remove) {
        dataSettingsToolBar.setLayout(new GridBagLayout());
        
        if(remove){
            dataSettingsToolBar.removeAll();
        }
        
        JTabbedPane tabbedPane = new JTabbedPane();
        dataSettingsToolBar.add(tabbedPane);
        
        // Create data range menu
        JComponent dataPanel = new JPanel();
        dataPanel.add(new JLabel("Dates To Represent"));
        LocalDate[] allDates = logDates.toArray(new LocalDate[0]);

        // Create single day toggle
        singleDayToggle = new JToggleButton("   Single Day", mainPanel.singleDay);
        singleDayToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                mainPanel.singleDay = ev.getStateChange() == ItemEvent.SELECTED;
                
                singleDaySlider.setVisible(mainPanel.singleDay);
                selectedSingleDayLabel.setVisible(mainPanel.singleDay);
                
                dateEndChooser.setVisible(!mainPanel.singleDay);
                dateStartChooser.setVisible(!mainPanel.singleDay);
                
                selectedDay = allDates[allDates.length / 2];
                mainPanel.selectedDay = selectedDay;
                mainPanel.updatePoints(startDate, endDate);
            }
        });
        dataPanel.add(singleDayToggle);

        // Create single day slider
        singleDaySlider = new JSlider(JSlider.HORIZONTAL, 0, allDates.length - 1, allDates.length / 2);
        singleDaySlider.setPaintTicks(true);
        singleDaySlider.setMajorTickSpacing(allDates.length / 10);
        singleDaySlider.setMinorTickSpacing(0);
        singleDaySlider.setPaintLabels(false);
        selectedDay = allDates[allDates.length / 2];
        selectedSingleDayLabel = new JLabel(selectedDay.toString());
        singleDaySlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int value = (int)source.getValue();
                selectedDay = allDates[value];
                mainPanel.selectedDay = selectedDay;
                selectedSingleDayLabel.setText(selectedDay.toString());
                
                mainPanel.updatePoints(startDate, endDate);
            }
        });
        dataPanel.add(singleDaySlider);
        dataPanel.add(selectedSingleDayLabel);
        singleDaySlider.setVisible(mainPanel.singleDay);
        selectedSingleDayLabel.setVisible(mainPanel.singleDay);

        dateStartChooser = new JComboBox<LocalDate>(allDates);
        dateStartChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                startDate = (LocalDate) dateStartChooser.getSelectedItem();
                mainPanel.updatePoints(startDate, endDate);
            }
        });
        dataPanel.add(dateStartChooser);
        
        dateEndChooser = new JComboBox<LocalDate>(allDates);
        dateEndChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                endDate = (LocalDate) dateEndChooser.getSelectedItem();
                mainPanel.updatePoints(startDate, endDate);
            }
        });
        dateEndChooser.setSelectedIndex(logDates.size() - 1);
        dataPanel.add(dateEndChooser);
        
        dateEndChooser.setVisible(!mainPanel.singleDay);
        dateStartChooser.setVisible(!mainPanel.singleDay);

        tabbedPane.addTab("Data", null, dataPanel, "Data Settings");

        // Create player color selector
        JComponent playerPanel = new JPanel();
        playerPanel.add(new JLabel("Player Colors"));
        
        playerColorButtons = new ArrayList<JMenuItem>();
        for(String player : mainPanel.playerNameColorMap.keySet()) {
            JMenuItem colButton = new JMenuItem(player);
            colButton.setForeground(mainPanel.playerNameColorMap.get(player));
            playerColorButtons.add(colButton);
            playerPanel.add(colButton);
            
            colButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Color selectedColor = JColorChooser.showDialog(Main.this, "Select player color", mainPanel.playerNameColorMap.get(player));
                    colButton.setForeground(selectedColor);
                    mainPanel.playerNameColorMap.put(player, selectedColor);
                    mainPanel.update = true;
                    mainPanel.repaint();
                }
            });
        }
        
        tabbedPane.addTab("Player", null, playerPanel, "Player Settings");
        
        // Create render type chooser
        JComponent renderPanel = new JPanel();
        drawTypeChooser = new JComboBox<Decoder.DrawType>(new Decoder.DrawType[] {Decoder.DrawType.PIXEL, Decoder.DrawType.DOT, Decoder.DrawType.LINE});
        drawTypeChooser.setSelectedItem(settings._drawType);
        drawTypeChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                settings._drawType = (Decoder.DrawType) drawTypeChooser.getSelectedItem();
                if(fancyLinesToggle != null)
                    fancyLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);
                if(showHiddenLinesToggle != null)
                    showHiddenLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);
                if(lineThresholdSlider != null)
                    lineThresholdSlider.setVisible(settings._drawType == Decoder.DrawType.LINE);
                if(lineThresholdLabel != null)
                    lineThresholdLabel.setVisible(settings._drawType == Decoder.DrawType.LINE);
        
                mainPanel.updatePoints(startDate, endDate);
                // FIX NOT CHANGING LABEL!! ALSO MAKE THE OTHER LABEL CODE LOOK NICE LIKE THIS!!!
                sizeTypeLabel.setText(settings._drawType == Decoder.DrawType.DOT   ? "   Dot Radius" :
                       (settings._drawType == Decoder.DrawType.PIXEL ? "   Square Size" :
                       (settings._drawType == Decoder.DrawType.LINE  ? "   Line Thickness" : "   -")));
                settings.SaveSettings();
                mainPanel.update = true;
                mainPanel.repaint();
            }
        });
        renderPanel.add(drawTypeChooser);

        // Create size slider
        sizeTypeLabel = new JLabel(settings._drawType == Decoder.DrawType.DOT   ? "   Dot Radius" :
                       (settings._drawType == Decoder.DrawType.PIXEL ? "   Square Size" :
                       (settings._drawType == Decoder.DrawType.LINE  ? "   Line Thickness" : "   -")));
        renderPanel.add(sizeTypeLabel);
        sizeSlider = new JSlider(JSlider.HORIZONTAL, 0, 50, settings.size);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setMajorTickSpacing(10);
        sizeSlider.setMinorTickSpacing(5);
        sizeSlider.setPaintLabels(true);
        sizeLabel = new JLabel(Integer.toString(settings.size));
        sizeSlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int radius = (int)source.getValue();
                settings.size = radius;
                sizeLabel.setText(Integer.toString(radius));
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();
            }
        });
        renderPanel.add(sizeSlider);
        renderPanel.add(sizeLabel);
        
        // Create line threshold slider
        renderPanel.add(new JLabel("   Line Threshold"));
        lineThresholdSlider = new JSlider(JSlider.HORIZONTAL, 0, 200, settings.lineThreshold);
        lineThresholdSlider.setPaintTicks(true);
        lineThresholdSlider.setMajorTickSpacing(50);
        lineThresholdSlider.setMinorTickSpacing(25);
        lineThresholdSlider.setPaintLabels(true);
        lineThresholdLabel = new JLabel(Integer.toString(settings.lineThreshold));
        lineThresholdSlider.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                int threshold = (int)source.getValue();
                settings.lineThreshold = threshold;
                lineThresholdLabel.setText(Integer.toString(threshold));
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();
            }
        });
        renderPanel.add(lineThresholdSlider);
        renderPanel.add(lineThresholdLabel);
        lineThresholdSlider.setVisible(settings._drawType == Decoder.DrawType.LINE);
        lineThresholdLabel.setVisible(settings._drawType == Decoder.DrawType.LINE);

        // Create fancy lines toggle
        fancyLinesToggle = new JToggleButton("   Fancy Lines", settings.fancyLines);
        fancyLinesToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                settings.fancyLines = ev.getStateChange() == ItemEvent.SELECTED;
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();
            }
        });
        renderPanel.add(fancyLinesToggle);
        fancyLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);
        
        // Create show hidden lines toggle
        showHiddenLinesToggle = new JToggleButton("   Show Hidden Lines", settings.hiddenLines);
        showHiddenLinesToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ev) {
                settings.hiddenLines = ev.getStateChange() == ItemEvent.SELECTED;
                mainPanel.update = true;
                mainPanel.repaint();
                settings.SaveSettings();
            }
        });
        renderPanel.add(showHiddenLinesToggle);
        //showHiddenLinesToggle.setVisible(settings._drawType == Decoder.DrawType.LINE);
        
        dataSettingsToolBar.add(tabbedPane);
        tabbedPane.addTab("Render", null, renderPanel, "Render Settings");
        
        dataSettingsToolBar.add(tabbedPane);
        
        revalidate();
    }
    
    private void decodeAndDisplay() throws IOException {
        decoder.Decode(files);
        mainPanel.setSize(new Dimension(decoder.xRange, decoder.yRange));
                    
        logDates = decoder.logDates;
        startDate = logDates.get(0);
        endDate = logDates.get(logDates.size() - 1);

        mainPanel.setData(decoder);
        mainPanel.updatePoints(startDate, endDate);
    }

    private void print(Object input) {
        System.out.println(input);
    }
}