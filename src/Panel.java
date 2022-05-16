package src;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class Panel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, Runnable {

    private final Settings settings;

    private boolean isRunning = true;

    public boolean update = false;

    public ArrayList<LogEntry> logEntries = new ArrayList<>();
    public ArrayList<LocalDateTime> logDates = new ArrayList<>();

    public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();
    public Map<String, Integer> playerMarkerCount = new LinkedHashMap<>();
    public Map<Vector3, Integer> posActivityMap = new LinkedHashMap<>();
    public int maxActivity;

    public Map<String, Boolean> playerNameEnabledMap = new LinkedHashMap<>();

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

    public int minX;

    public int minY;

    public int maxX;

    public int maxY;

    private int xRange;

    private int yRange;

    public BufferedImage backgroundImage;

    public int xBackgroundOffset, zBackgroundOffset;

    public LocalDateTime startDate;

    public LocalDateTime endDate;

    public int upscale = 1;
    public JLabel imageExportStatus;

    private Map<LocalDateTime, ArrayList<LogEntry>> logEntriesGroupedByTime = new LinkedHashMap<>();

    private Point mousePosition;

    private LogEntry selectedEntry;

    private AffineTransform inverse;

    private AffineTransform at;

    private int totalData;

    private final Logger logger;

    public boolean ShouldDraw;

    public float backgroundOpacity = 0.5f;

    public boolean isPlaying = false;
    public int timesCount;

    public int dateTimeIndex = 0;

    private final int TARGET_FPS = 30;
    private final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    private final PlayerTrackerDecoder main;

    public Panel(Settings set, Logger log, PlayerTrackerDecoder main) {
        super(true);

        this.main = main;
        logger = log;

        logger.Log("Initializing main display subsystem", Logger.MessageType.INFO);

        initComponent();
        settings = set;

        if (settings.antialiasing) {
            logger.Log("Antialiasing enabled on main display panel", Logger.MessageType.INFO);
        }

        logger.Log("Successfully initialized main display subsystem", Logger.MessageType.INFO);
    }

    private void initComponent() {
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        setBackground(Color.LIGHT_GRAY);
        mousePosition = new Point();
    }

    public BufferedImage LoadBackgroundImage(File imageFile) throws IOException {
        final long nowMs = System.currentTimeMillis();

        BufferedImage image = ImageIO.read(imageFile);

        final long durMs = System.currentTimeMillis() - nowMs;

        logger.Log("Loading world background image took " + durMs + "ms.", Logger.MessageType.INFO);

        return image;
    }

    /**
     * Loop of the app. It uses a fixed time step.
     * Optimal time represents time needed to update one frame.
     * Update time represents time actually taken to update one frame.
     * <p>
     * By calculating the difference between optimal and actual time,
     * we can let Thread to sleep for the exact time we are aiming for, which is 60 FPS.
     */
    public void run() {
        long now;
        long updateTime;
        long wait;

        while (isRunning) {
            now = System.nanoTime();

            if (isPlaying) {
                if (dateTimeIndex < timesCount) {
                    endDate = logDates.get(dateTimeIndex);
                    updatePoints(false);
                    dateTimeIndex++;
                    logger.Log("Playing animated", Logger.MessageType.INFO);
                } else {
                    logger.Log("Finished playing", Logger.MessageType.INFO);
                    isPlaying = false;
                }

                main.dateRangeSlider.setUpperValue(dateTimeIndex);
                main.startDateLabel.setText(startDate.toString().replace("T", "; "));
                main.endDateLabel.setText(endDate.toString().replace("T", "; "));
            }

            updateTime = System.nanoTime() - now;
            wait = (OPTIMAL_TIME - updateTime) / 1000000;

            try {
                Thread.sleep(wait);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!ShouldDraw)
            return;

        Graphics2D g2 = (Graphics2D) g;

        if (settings.antialiasing) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }

        if (zoomer) {
            at = new AffineTransform();
            float xRel = (float) MouseInfo.getPointerInfo().getLocation().getX() - (float) getLocationOnScreen().getX();
            float yRel = (float) MouseInfo.getPointerInfo().getLocation().getY() - (float) getLocationOnScreen().getY();
            float zoomDiv = zoomFactor / prevZoomFactor;
            xOffset = zoomDiv * xOffset + (1.0F - zoomDiv) * xRel;
            yOffset = zoomDiv * yOffset + (1.0F - zoomDiv) * yRel;
            at.translate(xOffset, yOffset);
            at.scale(zoomFactor, zoomFactor);
            g2.transform(at);

            try {
                inverse = at.createInverse();
            } catch (NoninvertibleTransformException nte) {
                logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
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
                logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
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
                logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
            }
        }


        if (backgroundImage != null) {
            // (-6384, -5376), (8959, 2767)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));

            g2.drawImage(backgroundImage, xBackgroundOffset, zBackgroundOffset, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        }

        drawPoints(g2, true, 0, 1);

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(1));
        float padding = 1.25F;
        g2.drawRect((int) (minX * padding), (int) (minY * padding), (int) (xRange * padding), (int) (yRange * padding));
        g2.setColor(Color.white);
        if (selectedEntry != null) {
            g2.setStroke(new BasicStroke(0.25f));
            drawRectangle(g2, selectedEntry.position.x, selectedEntry.position.z, 3, false);
        }

        RenderedPointsLabel.setText("   Loaded: " + totalData + " | Rendered: " + renderedPoints + " |");
    }

    private void drawPoints(Graphics2D g2d, boolean useCulling, int offset, int upscale) {
        Point pt = new Point();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        renderedPoints = 0;
        playerLastPosMap.clear();
        ArrayList<String> playerFirst = new ArrayList<>();
        Map<String, Integer> playerOccurences = new LinkedHashMap<>();

        if (settings._drawType == Decoder.DrawType.Heat) {
            int newMax = maxActivity;

            Vector3[] positions = posActivityMap.keySet().toArray(new Vector3[0]);
            for (Vector3 vec : positions) {
                if (at != null) at.transform(vec.toPoint(), pt);

                int x = (vec.x + (offset != 0 ? -minX : 0) + offset) * upscale;
                int y = (vec.z + (offset != 0 ? -minY : 0) + offset) * upscale;

                if (!useCulling || (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50)) {
//                    logger.Log(posActivityMap.get(vec) + " / " + maxActivity, Logger.MessageType.INFO);
                    float value = Math.min((float) posActivityMap.get(vec) * ((float) settings.heatMapThreshold / 100.0f), newMax);

                    if (settings._heatDrawType == PlayerTrackerDecoder.HeatDrawType.Change_Color) {
                        g2d.setColor(Utils.lerp(Color.darkGray, Color.getHSBColor(0.93f, 0.68f, 0.55f), value / newMax));
                        drawRectangle(g2d, x, y, settings.size, true);
                    } else {
                        drawRectangle(g2d, x, y, settings.size + value, true);
                    }
                    renderedPoints++;
                }
            }
        } else {
            g2d.setColor(Color.black);
            for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
                ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);

                for (LogEntry entry : entries) {
                    if (playerNameEnabledMap.get(entry.playerName)) {
                        if (settings.ageFade) {
                            if (!playerOccurences.containsKey(entry.playerName)) {
                                playerOccurences.put(entry.playerName, 1);
                            } else {
                                playerOccurences.put(entry.playerName, playerOccurences.get(entry.playerName) + 1);
                            }
                        }

                        if (at != null) at.transform(entry.position.toPoint(), pt);

                        int x = (entry.position.x + (offset != 0 ? -minX : 0) + offset) * upscale;
                        int y = (entry.position.z + (offset != 0 ? -minY : 0) + offset) * upscale;

                        if (settings.terminusPoints && settings._drawType == Decoder.DrawType.Line && !playerFirst.contains(entry.playerName)) {
                            playerFirst.add(entry.playerName);
                            g2d.setColor(playerNameColorMap.get(entry.playerName));
                            drawDot(g2d, x, y, settings.size + 7, false);
                        }

                        if (!useCulling || (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50)) {
                            Color col = playerNameColorMap.get(entry.playerName);
                            int val = settings.ageFade ? Utils.lerp(0, 255, (float) playerOccurences.get(entry.playerName) / (float) (settings.ageFadeThreshold == 0 ? playerMarkerCount.get(entry.playerName) : settings.ageFadeThreshold)) : 255;
                            g2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), Math.max(0, Math.min(255, val))));

                            if (settings._drawType == Decoder.DrawType.Pixel) {
                                drawRectangle(g2d, x, y, settings.size, true);
                                renderedPoints++;
                            } else if (settings._drawType == Decoder.DrawType.Dot) {
                                drawDot(g2d, x, y, settings.size, true);
                                renderedPoints++;
                            } else if (settings._drawType == Decoder.DrawType.Line) {
                                Vector3 lastPos = playerLastPosMap.get(entry.playerName);

                                if (lastPos != null && (settings.hiddenLines || (Math.abs(entry.position.x - lastPos.x) <= settings.lineThreshold && Math.abs(entry.position.z - lastPos.z) <= settings.lineThreshold))) {
                                    if (settings.hiddenLines && (Math.abs(entry.position.x - lastPos.x) > settings.lineThreshold || Math.abs(entry.position.z - lastPos.z) > settings.lineThreshold)) {
                                        g2d.setStroke(new BasicStroke((settings.size / 2.0F), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, new float[]{9.0F}, 0.0F));
                                    } else {
                                        g2d.setStroke(new BasicStroke(settings.size));
                                    }

                                    if (settings.fancyLines) {
                                        drawArrowLine(g2d, x, y, (lastPos.x + (offset != 0 ? -minX : 0) + offset) * upscale, (lastPos.z + (offset != 0 ? -minY : 0) + offset) * upscale, settings.size * 2, settings.size * 2);
                                    } else {
                                        drawLine(g2d, x, y, (lastPos.x + (offset != 0 ? -minX : 0) + offset) * upscale, (lastPos.z + (offset != 0 ? -minY : 0) + offset) * upscale, settings.size);
                                    }
                                    renderedPoints++;
                                }

                                playerLastPosMap.put(entry.playerName, entry.position);
                            }
                        }
                    }
                }
            }

            if (settings.terminusPoints && settings._drawType == Decoder.DrawType.Line) {
                for (String name : playerLastPosMap.keySet()) {
                    if (playerNameEnabledMap.get(name)) {
                        Vector3 pos = playerLastPosMap.get(name);
                        if (at != null) at.transform(pos.toPoint(), pt);

                        int x = (pos.x + (offset != 0 ? -minX : 0) + offset) * upscale;
                        int y = (pos.z + (offset != 0 ? -minY : 0) + offset) * upscale;

                        if (!useCulling || (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50)) {
                            g2d.setColor(playerNameColorMap.get(name));
                            drawDot(g2d, x, y, settings.size + 5, false);
                        }
                    }
                }
            }
        }
    }

    private void drawRectangle(Graphics2D g2d, float x, float y, float size, boolean filled) {
        if (filled) g2d.fill(new Rectangle2D.Double(x - size / 2, y - size / 2, size, size));
        else g2d.draw(new Rectangle2D.Double(x - size / 2, y - size / 2, size, size));
    }

    private void drawDot(Graphics2D g2d, float x, float y, float size, boolean filled) {
        if (filled) g2d.fill(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size));
        else g2d.draw(new Ellipse2D.Double(x - size / 2, y - size / 2, size, size));
    }

    private void drawLine(Graphics2D g2d, float x1, float y1, float x2, float y2, float thickness) {
        g2d.draw(new Line2D.Double(x1 - thickness / 2, y1 - thickness / 2, x2 - thickness / 2, y2 - thickness / 2));
    }

    private void drawArrowLine(Graphics2D g2d, float x1, float y1, float x2, float y2, float d, float h) {
        float dx = x2 - x1, dy = y2 - y1;
        float D = (float) Math.sqrt((dx * dx + dy * dy));
        float xm = D - d, xn = xm, ym = h, yn = -h;
        float sin = dy / D, cos = dx / D;
        float x = xm * cos - ym * sin + x1;
        ym = xm * sin + ym * cos + y1;
        xm = x;
        x = xn * cos - yn * sin + x1;
        yn = xn * sin + yn * cos + y1;
        xn = x;
        drawLine(g2d, x1, y1, x2, y2, settings.size);
        g2d.fillPolygon(new int[]{(int) x2, (int) xm, (int) xn}, new int[]{(int) y2, (int) ym, (int) yn}, 3);
    }

    public void updatePoints(boolean log) {
        if (log) logger.Log("Updating points", Logger.MessageType.INFO);

        logEntriesGroupedByTime.clear();
        playerMarkerCount.clear();
        posActivityMap.clear();
        maxActivity = 0;

        for (LogEntry entry : logEntries) {
            //if (((singleDate && !entry.isChunk && entry.time.toLocalDate().equals(selectedDate)) || (!singleDate && ((entry.time.toLocalDate().isAfter(startDate) && entry.time.toLocalDate().isBefore(endDate)) || entry.time.toLocalDate().equals(startDate) || entry.time.toLocalDate().equals(endDate)))) && ((
            //  singleTime && entry.time.toLocalTime().getHour() == selectedHour) || (!singleTime && entry.time.toLocalTime().getHour() >= startHour && entry.time.toLocalTime().getHour() <= endHour))) {
            if (!entry.isChunk && ((entry.time.isAfter(startDate) && entry.time.isBefore(endDate)) || entry.time.equals(startDate) || entry.time.equals(endDate))) {
                logEntriesGroupedByTime.putIfAbsent(entry.time, new ArrayList<>());
                logEntriesGroupedByTime.get(entry.time).add(entry);

                if (!playerMarkerCount.containsKey(entry.playerName)) playerMarkerCount.put(entry.playerName, 1);
                else {
                    playerMarkerCount.put(entry.playerName, playerMarkerCount.get(entry.playerName) + 1);
                }

                if (!posActivityMap.containsKey(entry.position)) {
                    posActivityMap.put(entry.position, 1);
                } else {
                    int val = posActivityMap.get(entry.position) + 1;
                    posActivityMap.put(entry.position, val);

                    if (maxActivity < val) {
                        maxActivity = val;
                    }
                }
            }
        }

        update = true;

        repaint();

        if (log) logger.Log("Updated points: " + logEntriesGroupedByTime.size(), Logger.MessageType.INFO);
    }

    public void setData(Decoder dec) {
        logger.Log("Setting data to display", Logger.MessageType.INFO);

        _Decoder = dec;
        logEntries = _Decoder.logEntries;
        logDates = _Decoder.logDates;

        for (LogEntry entry : logEntries) {
            logEntriesGroupedByTime.putIfAbsent(entry.time, new ArrayList<>());
            logEntriesGroupedByTime.get(entry.time).add(entry);
        }
        timesCount = logEntriesGroupedByTime.size();
        logEntriesGroupedByTime.clear();

        playerNameColorMap = _Decoder.playerNameColorMap;
        playerNameEnabledMap = _Decoder.playerNameEnabledMap;

        playerLastPosMap = _Decoder.playerLastPosMap;

        minX = _Decoder.minX;
        minY = _Decoder.minY;
        maxX = _Decoder.maxX;
        maxY = _Decoder.maxY;
        xRange = _Decoder.xRange;
        yRange = _Decoder.yRange;
        setPreferredSize(new Dimension(xRange, yRange));
        totalData = logEntries.size();
        Collections.sort(logEntries);
        SelectedEntryLabel.setText("Nothing Selected");
        selectedEntry = null;

        logger.Log("Successfully set data to display", Logger.MessageType.INFO);
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
        if (inverse != null) inverse.transform(e.getPoint(), mousePosition);
        CoordinateLabel.setText("(" + mousePosition.x + ", " + mousePosition.y + ") | ");
    }

    public void mouseClicked(MouseEvent e) {
        update = true;
        SelectedEntryLabel.setText("Nothing Selected");
        selectedEntry = null;
        for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
            ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);
            for (LogEntry entry : entries) {
                if (Math.abs(entry.position.x - mousePosition.x) < 2 && Math.abs(entry.position.z - mousePosition.y) < 2) {
                    selectedEntry = entry;
                    SelectedEntryLabel.setText(entry.toString());

                    logger.Log("Selected a log entry:\n" + entry, Logger.MessageType.INFO);
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

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void SaveAsImage(PlayerTrackerDecoder playerTrackerDecoder) {
        logger.Log("Started saving current screen as an image. Currently preparing the image", Logger.MessageType.INFO);
        imageExportStatus.setText("  Processing...");
        playerTrackerDecoder.repaint();

        System.gc();

        int imagePadding = 100;
        BufferedImage image = null;

        try {
            image = new BufferedImage((xRange + imagePadding) * upscale, (yRange + imagePadding) * upscale, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, (xRange + imagePadding) * upscale, (yRange + imagePadding) * upscale);

            //g2d.transform(at); // use to capture current view only (aka. screenshot)

            drawPoints(g2d, false, imagePadding / 2, upscale);

            g2d.setColor(Color.white);
            float padding = 1.25F;
            g2d.drawRect((int) (minX * padding), (int) (minY * padding), (int) (xRange * padding), (int) (yRange * padding));

            g2d.dispose();
        } catch (OutOfMemoryError ex) {
            logger.Log("Error preparing the image to export (Out of memory):\n   " + Arrays.toString(ex.getStackTrace()), Logger.MessageType.ERROR);
        }

        logger.Log("Starting to save the exported image file", Logger.MessageType.INFO);

        if (!new File("outputs").exists()) {
            boolean val = new File("outputs").mkdir();
            logger.Log("Outputs folder to save exported image didn't exist so it was just created with result: " + val, Logger.MessageType.WARNING);
        }

        String name = settings._drawType + "-pointMap-" + _Decoder.dataWorld + "-" + _Decoder.dataDate;
        File[] logFiles = new File("outputs/").listFiles();
        int count = 0;

        if (logFiles != null) {
            for (File file : logFiles) {
                if (file.getName().contains(name)) {
                    count++;
                }
            }

            name += (count != 0 ? " " + count : "") + ".png";

            try {
                if (image != null) {
                    ImageIO.write(image, "png", new File("outputs/" + name));
                    logger.Log("Successfully saved current screen as an image", Logger.MessageType.INFO);
                } else {
                    logger.Log("Image to save is null", Logger.MessageType.ERROR);
                }
            } catch (Exception e) {
                logger.Log("Error saving current screen as an image:\n   " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
            }

            imageExportStatus.setText("   Done!");
        }
    }

    public void Reset() {
        logEntries.clear();
        logDates.clear();

        logEntriesGroupedByTime.clear();

        playerNameColorMap.clear();
        playerLastPosMap.clear();
        playerNameEnabledMap.clear();
        playerMarkerCount.clear();
        posActivityMap.clear();

        totalData = 0;
        maxActivity = 0;
        SelectedEntryLabel.setVisible(false);
        selectedEntry = null;
    }
}