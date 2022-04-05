import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.time.LocalDateTime;
import RangedSlider.*;

public class Main extends JFrame {
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
  
  private JSlider sizeSlider;
  
  private JLabel sizeLabel;
  
  private JLabel sizeTypeLabel;
  
  private JSlider lineThresholdSlider;
  
  private JLabel lineThresholdLabel;
  
  private JLabel lineThresholdTitle;

  private JToggleButton fancyLinesToggle;
  
  private JToggleButton showHiddenLinesToggle;
  
  private File[] files;
  
  private ArrayList<LocalDateTime> logDates;
  
  private Logger logger;
  
  private Integer[] hours = new Integer[] { 
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 
      11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 
      21, 22, 23, 24 };
   
      private Integer[] minutes = new Integer[] { 
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 
      11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 
      21, 22, 23, 24 };
  
  public Main() {
      logger = new Logger();
    settings = new Settings(logger);
    decoder = new Decoder(settings, logger);
    initMainFrame();
    chooser = new JFileChooser();
    chooser.setCurrentDirectory(new File("."));
    chooser.setMultiSelectionEnabled(true);
    chooser.addChoosableFileFilter(new TextFileFilter());
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
            int returnVal = chooser.showOpenDialog(Main.this);
            files = chooser.getSelectedFiles();
            if (files.length != 0) {
              try {
                decodeAndDisplay();
              } catch (IOException e) {
                e.printStackTrace();
                print(e.getStackTrace());
              } 
              initDataSettingsToolBar(alreadyImported);
              dataSettingsToolBar.setVisible(true);
              mainPanel.SelectedEntryLabel.setVisible(true);
              print("Selected files: " + files.length);
              alreadyImported = true;
            } 
          }
        });
  }
  
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
          public void run() {
            Main myFrame = new Main();
            myFrame.setVisible(true);
          }
        });
  }
  
  private void initMainFrame() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException|InstantiationException|IllegalAccessException|javax.swing.UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      print(ex.getStackTrace());
    } 
    setTitle("Player Tracker Decoder App - v1.0.1");
    setDefaultCloseOperation(3);
    setSize(new Dimension(1280, 720));
    setLayout(new BorderLayout());
    setLocationRelativeTo((Component)null);
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
  }
  
  private void initDataSettingsToolBar(boolean remove) {
    dataSettingsToolBar.setLayout(new FlowLayout(1, 5, 5));
    if (remove)
      dataSettingsToolBar.removeAll(); 
    JTabbedPane tabbedPane = new JTabbedPane();
    dataSettingsToolBar.add(tabbedPane);
    JComponent dataPanel = new JPanel();
    dataPanel.add(new JLabel("Dates To Represent:   "));
    
    //LocalDateTime[] allDates = logDates.toArray(new LocalDateTime[0]);
    /*singleDayToggle = new JToggleButton("Single Day", mainPanel.singleDay);
    singleDayToggle.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            mainPanel.singleDay = (ev.getStateChange() == 1);
            dateRangeSlider.setVisible(mainPanel.singleDay);
            selectedSingleDayLabel.setVisible(mainPanel.singleDay);
            dateEndChooser.setVisible(!mainPanel.singleDay);
            dateStartChooser.setVisible(!mainPanel.singleDay);
            selectedDate = allDates[allDates.length / 2];
            mainPanel.startDate = startDate;
            mainPanel.endDate = endDate;
            mainPanel.selectedDate = selectedDate;
            mainPanel.updatePoints();
          }
        });
    dataPanel.add(singleDayToggle);*/
    
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
            RangeSlider source = (RangeSlider)e.getSource();
            int value1 = source.getValue();
            int value2 = source.getUpperValue();
            startDate = logDates.get(value1);
            endDate = logDates.get(value2);
            mainPanel.startDate = startDate;
            mainPanel.endDate = endDate;
            startDateLabel.setText(startDate.toString().replace("T", "; "));
            endDateLabel.setText(endDate.toString().replace("T", "; "));
            mainPanel.updatePoints();
          }
        });
    dataPanel.add(startDateLabel);
    dataPanel.add(dateRangeSlider);
    dataPanel.add(endDateLabel);
    
    /*dateRangeSlider.setVisible(mainPanel.singleDay);
    selectedSingleDayLabel.setVisible(mainPanel.singleDay);
    
    dateStartChooser = new JComboBox<>(allDates);
    dateStartChooser.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            startDate = (LocalDateTime)dateStartChooser.getSelectedItem();
            mainPanel.startDate = startDate;
            mainPanel.updatePoints();
          }
        });
    dataPanel.add(dateStartChooser);
    dateEndChooser = new JComboBox<>(allDates);
    dateEndChooser.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            endDate = (LocalDateTime)dateEndChooser.getSelectedItem();
            mainPanel.endDate = endDate;
            mainPanel.updatePoints();
          }
        });
    dateEndChooser.setSelectedIndex(logDates.size() - 1);
    dataPanel.add(dateEndChooser);
    dateEndChooser.setVisible(!mainPanel.singleDay);
    dateStartChooser.setVisible(!mainPanel.singleDay);
    
    //dataPanel.add(new JLabel("Times To Represent"));
    //singleTimeToggle = new JToggleButton("Single Time", mainPanel.singleTime);
    //singleTimeToggle.addItemListener(new ItemListener() {
          //public void itemStateChanged(ItemEvent ev) {
            //mainPanel.singleTime = (ev.getStateChange() == 1);
            //singleTimeSlider.setVisible(mainPanel.singleTime);
            //selectedSingleTimeLabel.setVisible(mainPanel.singleTime);
            //timeEndSpinner.setVisible(!mainPanel.singleTime);
            //timeStartSpinner.setVisible(!mainPanel.singleTime);
            //selectedHour = hours[12].intValue();
            //mainPanel.selectedHour = selectedHour;
            //mainPanel.updatePoints(startDate, endDate, startHour, endHour);
          //}
        //});
    //dataPanel.add(singleTimeToggle);
    //singleTimeSlider = new JSlider(0, 1, 24, 12);
    //dateRangeSlider.setPaintTicks(true);
    //dateRangeSlider.setMajorTickSpacing(allDates.length / 10);
    //dateRangeSlider.setMinorTickSpacing(0);
    //dateRangeSlider.setPaintLabels(false);
    
    //selectedHour = hours[12].intValue();
    //selectedSingleTimeLabel = new JLabel("" + selectedHour);
    //singleTimeSlider.addChangeListener(new ChangeListener() {
          //public void stateChanged(ChangeEvent e) {
            //selectedHour = hours[singleTimeSlider.getValue() - 1].intValue();
            //mainPanel.selectedHour = selectedHour;
            //selectedSingleTimeLabel.setText("" + selectedHour);
            //mainPanel.updatePoints(startDate, endDate, startHour, endHour);
          //}
        //});
    //dataPanel.add(singleTimeSlider);
    //dataPanel.add(selectedSingleTimeLabel);
    //singleTimeSlider.setVisible(mainPanel.singleTime);
    //selectedSingleTimeLabel.setVisible(mainPanel.singleTime);
    //timeStartSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
    //timeStartSpinner.addChangeListener(new ChangeListener() {
          //public void stateChanged(ChangeEvent e) {
            //startHour = hours[((Integer)timeStartSpinner.getValue()).intValue() - 1].intValue();
            //mainPanel.updatePoints(startDate, endDate, startHour, endHour);
          //}
        //});
    //dataPanel.add(timeStartSpinner);
    //timeEndSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 24, 1));
    //timeEndSpinner.addChangeListener(new ChangeListener() {
          //public void stateChanged(ChangeEvent e) {
            //endHour = hours[((Integer)timeEndSpinner.getValue()).intValue() - 1].intValue();
            //mainPanel.updatePoints(startDate, endDate, startHour, endHour);
          //}
        //});
    //dataPanel.add(timeEndSpinner);
    //timeStartSpinner.setVisible(!mainPanel.singleTime);
    //timeEndSpinner.setVisible(!mainPanel.singleTime);*/
    
    tabbedPane.addTab("Data", null, dataPanel, "Data Settings");
    
    JComponent playerPanel = new JPanel();
    playerPanel.add(new JLabel("Player Colors"));
    playerColorButtons = new ArrayList<>();
    for (String player : mainPanel.playerNameColorMap.keySet()) {
      final JMenuItem colButton = new JMenuItem(player);
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
    
    JComponent renderPanel = new JPanel();
    drawTypeChooser = new JComboBox<>(new Decoder.DrawType[] { Decoder.DrawType.PIXEL, Decoder.DrawType.DOT, Decoder.DrawType.LINE });
    drawTypeChooser.setSelectedItem(settings._drawType);
    drawTypeChooser.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            settings._drawType = (Decoder.DrawType)drawTypeChooser.getSelectedItem();
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
            sizeTypeLabel.setText((settings._drawType == Decoder.DrawType.DOT) ? "Dot Radius" : (
                (settings._drawType == Decoder.DrawType.PIXEL) ? "Square Size" : (
                (settings._drawType == Decoder.DrawType.LINE) ? "Line Thickness" : "   -")));
            settings.SaveSettings();
            mainPanel.update = true;
            mainPanel.repaint();
          }
        });
    renderPanel.add(drawTypeChooser);
    sizeTypeLabel = new JLabel((settings._drawType == Decoder.DrawType.DOT) ? "Dot Radius" : ((settings._drawType == Decoder.DrawType.PIXEL) ? "Square Size" : ((settings._drawType == Decoder.DrawType.LINE) ? "Line Thickness" : "   -")));
    renderPanel.add(sizeTypeLabel);
    sizeSlider = new JSlider(0, 0, settings.size > 50 ? (int)(settings.size + (settings.size * 0.1f)) : 50, settings.size);
    sizeSlider.setPaintTicks(true);
    sizeSlider.setMajorTickSpacing(settings.size / 5);
    sizeSlider.setMinorTickSpacing(0);
    sizeSlider.setPaintLabels(true);
    sizeLabel = new JLabel(Integer.toString(settings.size));
    sizeSlider.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            int radius = source.getValue();
            settings.size = radius;
            sizeLabel.setText(Integer.toString(radius));
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();
          }
        });
    renderPanel.add(sizeSlider);
    renderPanel.add(sizeLabel);
    lineThresholdTitle = new JLabel("   Line Threshold");
    renderPanel.add(lineThresholdTitle);
    lineThresholdSlider = new JSlider(0, 0, settings.lineThreshold > 200 ? (int)(settings.lineThreshold + (settings.lineThreshold * 0.1f)) : 200, settings.lineThreshold);
    lineThresholdSlider.setPaintTicks(true);
    lineThresholdSlider.setMajorTickSpacing(settings.lineThreshold / 4);
    lineThresholdSlider.setMinorTickSpacing(0);
    lineThresholdSlider.setPaintLabels(true);
    lineThresholdLabel = new JLabel(Integer.toString(settings.lineThreshold));
    lineThresholdSlider.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            JSlider source = (JSlider)e.getSource();
            int threshold = source.getValue();
            settings.lineThreshold = threshold;
            lineThresholdLabel.setText(Integer.toString(threshold));
            mainPanel.update = true;
            mainPanel.repaint();
            settings.SaveSettings();
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
          }
        });
    renderPanel.add(showHiddenLinesToggle);
    showHiddenLinesToggle.setVisible((settings._drawType == Decoder.DrawType.LINE));
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
    //startHour = 1;
    //endHour = 23;
    mainPanel.startDate = startDate;
    mainPanel.endDate = endDate;
    //mainPanel.selectedDate = selectedDate;
    mainPanel.setData(decoder);
    mainPanel.updatePoints();
  }
  
  private void print(Object input) {
      logger.Log(input);
  }
}