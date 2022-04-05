import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Panel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, Runnable {
  private final float ZOOM_INCREMENT = 1.05F;
  
  private final int MAX_ZOOM = 50;
  
  private final float MIN_ZOOM = 0.02F;
  
  private final Settings settings;
  
  private final boolean isRunning = true;
  
  public boolean update = false;
  
  public ArrayList<LogEntry> logEntries = new ArrayList<>();
  
  public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
  
  public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();
  
  public JLabel CoordinateLabel;
  
  public JLabel SelectedEntryLabel;
  
  public JLabel RenderedPointsLabel;
  
  public Decoder _Decoder;
  
  public int renderedPoints = 0;
  
  private float zoomFactor = 1.0F;
  
  private float prevZoomFactor = 1.0F;
  
  private boolean zoomer;
  
  private boolean dragger;
  
  private boolean released;
  
  private float xOffset = 0.0F;
  
  private float yOffset = 0.0F;
  
  private int xDiff;
  
  private int yDiff;
  
  private Point startPoint;
  
  private int minX;
  
  private int minY;
  
  private int maxX;
  
  private int maxY;
  
  private int xRange;
  
  private int yRange;
  
  //public boolean singleDate = false;
  
  //public LocalDateTime selectedDate;
  
  public LocalDateTime startDate;
  
  public LocalDateTime endDate;
  
  //public int selectedHour;
  
  //private int startHour;
  
  //private int endHour;
  
  private Map<LocalDateTime, ArrayList<LogEntry>> logEntriesGroupedByTime = new LinkedHashMap<>();
  
  private Point mousePosition;
  
  private LogEntry selectedEntry;
  
  private AffineTransform inverse;
  
  private AffineTransform at;
  
  private int totalData;
  
  private Logger logger;
  
  public Panel(Settings set, Logger log) {
    super(true);
    initComponent();
    logger = log;
    settings = set;
  }
  
  private void initComponent() {
    addMouseWheelListener(this);
    addMouseMotionListener(this);
    addMouseListener(this);
    setBackground(Color.LIGHT_GRAY);
    mousePosition = new Point();
  }
  
  public void run() {}
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    Graphics2D g2 = (Graphics2D)g;
    
    if(settings.antialiasing){
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
    
    if (zoomer) {
      at = new AffineTransform();
      float xRel = (float)MouseInfo.getPointerInfo().getLocation().getX() - (float)getLocationOnScreen().getX();
      float yRel = (float)MouseInfo.getPointerInfo().getLocation().getY() - (float)getLocationOnScreen().getY();
      float zoomDiv = zoomFactor / prevZoomFactor;
      xOffset = zoomDiv * xOffset + (1.0F - zoomDiv) * xRel;
      yOffset = zoomDiv * yOffset + (1.0F - zoomDiv) * yRel;
      at.translate(xOffset, yOffset);
      at.scale(zoomFactor, zoomFactor);
      g2.transform(at);
      try {
        inverse = at.createInverse();
      } catch (NoninvertibleTransformException nte) {
        nte.printStackTrace();
        print(nte.getStackTrace());
      } 
      prevZoomFactor = zoomFactor;
      zoomer = false;
    } 
    
    if (dragger) {
      at = new AffineTransform();
      at.translate(xOffset + xDiff, yOffset + yDiff);
      at.scale(zoomFactor, zoomFactor);
      g2.transform(at);
      try {
        inverse = at.createInverse();
      } catch (NoninvertibleTransformException nte) {
        nte.printStackTrace();
        print(nte.getStackTrace());
      } 
      if (released) {
        xOffset += xDiff;
        yOffset += yDiff;
        dragger = false;
      } 
    } 
    
    if (update) {
      update = false;
      float zoomDiv = zoomFactor / prevZoomFactor;
      xOffset = zoomDiv * xOffset + 1.0F - zoomDiv;
      yOffset = zoomDiv * yOffset + 1.0F - zoomDiv;
      at = new AffineTransform();
      at.translate(xOffset + xDiff, yOffset + yDiff);
      at.scale(zoomFactor, zoomFactor);
      g2.transform(at);
      try {
        inverse = at.createInverse();
      } catch (NoninvertibleTransformException nte) {
        nte.printStackTrace();
        print(nte.getStackTrace());
      } 
    }
    
    drawPoints(g2);
    g2.setColor(Color.black);
    float padding = 1.25F;
    g2.drawRect((int)(minX * padding), (int)(minY * padding), (int)(xRange * padding), (int)(yRange * padding));
    g2.setColor(Color.white);
    if (selectedEntry != null){
      g2.setStroke(new BasicStroke(0.25f));
      g2.drawRect(selectedEntry.position.x - 1, selectedEntry.position.z - 1, 3, 3); 
    }
    
    RenderedPointsLabel.setText("   Loaded: " + totalData + " | Rendered: " + renderedPoints + " | ");
  }
  
  private void drawPoints(Graphics2D g2d) {
    Point pt = new Point();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    renderedPoints = 0;
    for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
      ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);
      for (LogEntry entry : entries) {
        if (at != null)
          at.transform(entry.position.toPoint(), pt); 
          
        if (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50) {
          renderedPoints++;
          g2d.setPaint(playerNameColorMap.get(entry.playerName));
          if (settings._drawType == Decoder.DrawType.PIXEL) {
            g2d.fillRect(entry.position.x, entry.position.z, settings.size, settings.size);
            //continue;
          } else if (settings._drawType == Decoder.DrawType.DOT) {
            g2d.fillOval(entry.position.x, entry.position.z, settings.size, settings.size);
            //continue;
          } else if (settings._drawType == Decoder.DrawType.LINE) {
            Vector3 lastPos = playerLastPosMap.get(entry.playerName);
            
            if (settings.hiddenLines || (Math.abs(entry.position.x - lastPos.x) <= settings.lineThreshold && Math.abs(entry.position.z - lastPos.z) <= settings.lineThreshold)) {
              if (settings.hiddenLines && (Math.abs(entry.position.x - lastPos.x) > settings.lineThreshold || Math.abs(entry.position.z - lastPos.z) > settings.lineThreshold)) {
                g2d.setStroke(new BasicStroke((settings.size / 2), 0, 0, 10.0F, new float[] { 9.0F }, 0.0F));
              } else {
                g2d.setStroke(new BasicStroke(settings.size));
              } 
              
              if (settings.fancyLines) {
                drawArrowLine(g2d, entry.position.x, entry.position.z, lastPos.x, lastPos.z, settings.size * 2, settings.size * 2);
              } else {
                g2d.drawLine(entry.position.x, entry.position.z, lastPos.x, lastPos.z);
              } 
            } 
            
            playerLastPosMap.put(entry.playerName, entry.position);
          } 
        } 
      } 
    } 
  }
  
  private void drawArrowLine(Graphics2D g, int x1, int y1, int x2, int y2, int d, int h) {
    int dx = x2 - x1, dy = y2 - y1;
    float D = (float)Math.sqrt((dx * dx + dy * dy));
    float xm = D - d, xn = xm, ym = h, yn = -h;
    float sin = dy / D, cos = dx / D;
    float x = xm * cos - ym * sin + x1;
    ym = xm * sin + ym * cos + y1;
    xm = x;
    x = xn * cos - yn * sin + x1;
    yn = xn * sin + yn * cos + y1;
    xn = x;
    g.drawLine(x1, y1, x2, y2);
    g.fillPolygon(new int[] { x2, (int)xm, (int)xn }, new int[] { y2, (int)ym, (int)yn }, 3);
  }
  
  public void updatePoints() {
    logEntriesGroupedByTime = new LinkedHashMap<>();
    for (LogEntry entry : logEntries) {
      //if (((singleDate && !entry.isChunk && entry.time.toLocalDate().equals(selectedDate)) || (!singleDate && ((entry.time.toLocalDate().isAfter(startDate) && entry.time.toLocalDate().isBefore(endDate)) || entry.time.toLocalDate().equals(startDate) || entry.time.toLocalDate().equals(endDate)))) && ((
      //  singleTime && entry.time.toLocalTime().getHour() == selectedHour) || (!singleTime && entry.time.toLocalTime().getHour() >= startHour && entry.time.toLocalTime().getHour() <= endHour))) {
      if (!entry.isChunk && ((entry.time.isAfter(startDate) && entry.time.isBefore(endDate)) || entry.time.equals(startDate) || entry.time.equals(endDate))) {
        logEntriesGroupedByTime.putIfAbsent(entry.time, new ArrayList<>());
        logEntriesGroupedByTime.get(entry.time).add(entry);
      }
    } 
    update = true;
    repaint();
    print("Updated points: " + logEntriesGroupedByTime.size());
  }
  
  public void setData(Decoder dec) {
    _Decoder = dec;
    logEntries = new ArrayList<>();
    playerNameColorMap = new LinkedHashMap<>();
    playerLastPosMap = new LinkedHashMap<>();
    logEntries = _Decoder.logEntries;
    playerNameColorMap = _Decoder.playerNameColorMap;
    playerLastPosMap = _Decoder.playerLastPosMap;
    minX = _Decoder.minX;
    minY = _Decoder.minY;
    maxX = _Decoder.maxX;
    maxY = _Decoder.maxY;
    xRange = _Decoder.xRange;
    yRange = _Decoder.yRange;
    totalData = logEntries.size();
    Collections.sort(logEntries);
    SelectedEntryLabel.setText("Nothing Selected");
    selectedEntry = null;
  }
  
  public void mouseWheelMoved(MouseWheelEvent e) {
    zoomer = true;
    if (e.getWheelRotation() < 0 && zoomFactor < 50.0F) {
      zoomFactor *= 1.05F;
      repaint();
    }
    
    if (e.getWheelRotation() > 0 && zoomFactor > 0.02F) {
      zoomFactor /= 1.05F;
      repaint();
    } 
  }
  
  public void mouseDragged(MouseEvent e) {
    Point curPoint = e.getLocationOnScreen();
    xDiff = curPoint.x - startPoint.x;
    yDiff = curPoint.y - startPoint.y;
    dragger = true;
    repaint();
  }
  
  public void mouseMoved(MouseEvent e) {
    if (inverse != null)
      inverse.transform(e.getPoint(), mousePosition); 
    CoordinateLabel.setText("(" + mousePosition.x + ", " + mousePosition.y + ") | ");
  }
  
  public void mouseClicked(MouseEvent e) {
    update = true;
    SelectedEntryLabel.setText("Nothing Selected");
    selectedEntry = null;
    for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
      ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);
      for (LogEntry entry : entries) {
        if (Math.abs(entry.position.x - mousePosition.x) < 1 && Math.abs(entry.position.z - mousePosition.y) < 1) {
          selectedEntry = entry;
          SelectedEntryLabel.setText(entry.toString());
        } 
      } 
    } 
  }
  
  public void mousePressed(MouseEvent e) {
    released = false;
    startPoint = MouseInfo.getPointerInfo().getLocation();
    xDiff = 0;
    yDiff = 0;
  }
  
  public void mouseReleased(MouseEvent e) {
    released = true;
    repaint();
  }
  
  public void mouseEntered(MouseEvent e) {}
  
  public void mouseExited(MouseEvent e) {}
  
  private void print(Object input) {
    logger.Log(input);
  }
}
