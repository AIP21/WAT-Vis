import java.awt.*;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.GraphicsDevice;
import java.awt.Dimension;
import java.util.Random;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

public class Panel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, Runnable {
    
    private final double ZOOM_INCREMENT = 1.05;
    private final int MAX_ZOOM = 50;
    private final double MIN_ZOOM = 0.02;
    
    private double zoomFactor = 1;
    private double prevZoomFactor = 1;
    private boolean zoomer;
    private boolean dragger;
    private boolean released;
    private double xOffset = 0;
    private double yOffset = 0;
    private int xDiff;
    private int yDiff;
    private Point startPoint;
    
    private int DEFAULT_WIDTH;
    private int DEFAULT_HEIGHT;
    
    private int minX, minY;
    private int maxX, maxY;
    private int xRange, yRange;
    
    private Settings settings;
    
    private boolean isRunning = true;
    public boolean update = false;
    
    public boolean singleDay = false;
    public LocalDate selectedDay;

    public ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();
    public Map<String, Color> playerNameColorMap = new LinkedHashMap<String, Color>();      
    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<String, Vector3>();
    public JLabel CoordinateLabel;
    public JLabel SelectedEntryLabel;
    public JLabel renderedPointsLabel;

    public Decoder decoder;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Map<LocalDateTime, ArrayList<LogEntry>> logEntriesGroupedByTime = new LinkedHashMap<LocalDateTime, ArrayList<LogEntry>>();
    
    private Dimension screenSize;
    
    private Point mousePosition;
    
    private LogEntry selectedEntry;
    
    private AffineTransform inverse, at;
    
    public Panel(Settings set) {
        initComponent();
        this.settings = set;
    }

    private void initComponent() {
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        setBackground(Color.LIGHT_GRAY);
        mousePosition = new Point();
    }
    
    public void SetInitialSize() {
        DEFAULT_WIDTH = getWidth();
        DEFAULT_HEIGHT = getHeight();
    }
    
    //int frame = 0;
    public void run() {
        //while(isRunning){
            //if(frame % 20 == 0){
                //iter++;
                //print("x: " + xOffset + ", y: " + yOffset);
            //}
            
            //frame++;
        //}
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        if (zoomer) {
            at = new AffineTransform();

            double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
            double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

            double zoomDiv = zoomFactor / prevZoomFactor;

            xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * xRel;
            yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * yRel;

            at.translate(xOffset, yOffset);
            at.scale(zoomFactor, zoomFactor);
            g2.transform(at);
            try {
                inverse = at.createInverse();
            } catch (java.awt.geom.NoninvertibleTransformException nte) {
                nte.printStackTrace();
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
            } catch (java.awt.geom.NoninvertibleTransformException nte) {
                nte.printStackTrace();
            }
            
            if (released) {
                xOffset += xDiff;
                yOffset += yDiff;
                dragger = false;
            }
        }
        
        if (update) {
            update = false;
            
            double zoomDiv = zoomFactor / prevZoomFactor;

            xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv);
            yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv);
            
            at = new AffineTransform();
            at.translate(xOffset + xDiff, yOffset + yDiff);
            at.scale(zoomFactor, zoomFactor);
            g2.transform(at);
            try {
                inverse = at.createInverse();
            } catch (java.awt.geom.NoninvertibleTransformException nte) {
                nte.printStackTrace();
            }
        }
        
        drawPoints(g2);
        g2.setColor(Color.black);
        float padding = 1.25f;
        g2.drawRect((int)(minX * padding), (int)(minY * padding), (int)(xRange * (padding)), (int)(yRange * (padding)));
    
        g2.setColor(Color.white);
        if(selectedEntry != null){
            g2.drawOval(selectedEntry.position.x - 2, selectedEntry.position.z - 2, 4, 4);
        }
        
        renderedPointsLabel.setText("" + renderedPoints);
    }
    
    public int renderedPoints = 0;
    private void drawPoints(Graphics2D g2d) {
        //screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point pt = new Point();
        Dimension screenSize = Toolkit. getDefaultToolkit(). getScreenSize();
        renderedPoints = 0;
        
        for(LocalDateTime time : logEntriesGroupedByTime.keySet()){
            ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);
            
            for(LogEntry entry : entries) {
                if(at != null)
                    at.transform(entry.position.toPoint(), pt);
                
                if(pt.x >= -2 && pt.x <= screenSize.width + 2 && pt.y >= -2 && pt.y <= screenSize.height + 2){ 
                    renderedPoints++;
                    g2d.setPaint(playerNameColorMap.get(entry.playerName));
                    if (settings._drawType == Decoder.DrawType.PIXEL){
                        g2d.fillRect(entry.position.x, entry.position.z, settings.size, settings.size);
                    } else if (settings._drawType == Decoder.DrawType.DOT){
                        g2d.fillOval(entry.position.x, entry.position.z, settings.size, settings.size);
                    } else if (settings._drawType == Decoder.DrawType.LINE){
                        Vector3 lastPos = playerLastPosMap.get(entry.playerName);
                        if (settings.hiddenLines || (Math.abs(entry.position.x - lastPos.x) < settings.lineThreshold && Math.abs(entry.position.z - lastPos.z) < settings.lineThreshold)) {
                            if(settings.hiddenLines && !(Math.abs(entry.position.x - lastPos.x) < settings.lineThreshold && Math.abs(entry.position.z - lastPos.z) < settings.lineThreshold)){
                                g2d.setStroke(new BasicStroke(settings.size / 2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{9}, 0));
                            } else {
                                g2d.setStroke(new BasicStroke(settings.size));
                            }
                            
                            if(settings.fancyLines) {
                                drawArrowLine(g2d, entry.position.x, entry.position.z, lastPos.x, lastPos.z, settings.size + 10, settings.size + 10);
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
    
    /**
     * Draw an arrow line between two points.
     * @param g the graphics component.
     * @param x1 x-position of first point.
     * @param y1 y-position of first point.
     * @param x2 x-position of second point.
     * @param y2 y-position of second point.
     * @param d  the width of the arrow.
     * @param h  the height of the arrow.
     */
    private void drawArrowLine(Graphics2D g, int x1, int y1, int x2, int y2, int d, int h) {
        int dx = x2 - x1, dy = y2 - y1;
        double D = Math.sqrt(dx*dx + dy*dy);
        double xm = D - d, xn = xm, ym = h, yn = -h, x;
        double sin = dy / D, cos = dx / D;
    
        x = xm*cos - ym*sin + x1;
        ym = xm*sin + ym*cos + y1;
        xm = x;
    
        x = xn*cos - yn*sin + x1;
        yn = xn*sin + yn*cos + y1;
        xn = x;
    
        g.drawLine(x1, y1, x2, y2);
        g.fillPolygon(new int[] {x2, (int) xm, (int) xn},new int[] {y2, (int) ym, (int) yn}, 3);
    }
    
    public void updatePoints(LocalDate _startDate, LocalDate _endDate) {    
        this.startDate = _startDate;
        this.endDate = _endDate;
        
        logEntriesGroupedByTime = new LinkedHashMap<LocalDateTime, ArrayList<LogEntry>>();
        
        for (LogEntry entry : logEntries) {
            if((singleDay && !entry.isChunk && entry.time.toLocalDate().equals(selectedDay)) || (!singleDay && ((entry.time.toLocalDate().isAfter(startDate) && entry.time.toLocalDate().isBefore(endDate)) || (entry.time.toLocalDate().equals(startDate) || entry.time.toLocalDate().equals(endDate))))) {
                logEntriesGroupedByTime.putIfAbsent(entry.time, new ArrayList<>());
                logEntriesGroupedByTime.get(entry.time).add(entry);
            }
        }
        
        update = true;
        repaint();
        print("Updated points: " + logEntriesGroupedByTime.size());
    }
    
    public void setData(Decoder dec){
        this.decoder = dec;
        this.logEntries = new ArrayList<LogEntry>();
        this.playerNameColorMap = new LinkedHashMap<String, Color>();
        this.playerLastPosMap = new LinkedHashMap<String, Vector3>();
        this.logEntries = decoder.logEntries;
        this.playerNameColorMap = decoder.playerNameColorMap;
        this.playerLastPosMap = decoder.playerLastPosMap;
        minX = decoder.minX;
        minY = decoder.minY;
        maxX = decoder.maxX;
        maxY = decoder.maxY;
        xRange = decoder.xRange;
        yRange = decoder.yRange;
        
        Collections.sort(this.logEntries);
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomer = true;

        //Zoom in
        if (e.getWheelRotation() < 0 && zoomFactor < MAX_ZOOM) {
            zoomFactor *= ZOOM_INCREMENT;
            repaint();
        }
        //Zoom out
        if (e.getWheelRotation() > 0 && zoomFactor > MIN_ZOOM) {
            zoomFactor /= ZOOM_INCREMENT;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point curPoint = e.getLocationOnScreen();
        xDiff = curPoint.x - startPoint.x;
        yDiff = curPoint.y - startPoint.y;

        dragger = true;
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(inverse != null)
            inverse.transform(e.getPoint(), mousePosition);
        
        CoordinateLabel.setText("   (" + mousePosition.x + ", " + mousePosition.y + ")   ");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        update = true;
        SelectedEntryLabel.setText("Nothing Selected");
        
        for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
            ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);
            for (LogEntry entry : entries) {
                if (Math.abs(entry.position.x - mousePosition.x) < 4 && Math.abs(entry.position.z - mousePosition.y) < 4) {
                    SelectedEntryLabel.setText(entry.toString());
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        released = false;
        //update = true;
        startPoint = MouseInfo.getPointerInfo().getLocation();
        xDiff = 0;
        yDiff = 0;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        released = true;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
    
    private void print(Object input) {
        System.out.println(input);
    }
}