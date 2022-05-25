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
    private final Logger logger;
    public Decoder _Decoder;

    private boolean isRunning = true;
    private int curFPS = 0;
    public boolean update = false;
    public boolean ShouldDraw = false;
    public boolean isPlaying = false;

    public ArrayList<LogEntry> logEntries = new ArrayList<>();
    public ArrayList<LocalDateTime> logDates = new ArrayList<>();
    private int totalData;

    private Map<LocalDateTime, ArrayList<LogEntry>> logEntriesGroupedByTime = new LinkedHashMap<>();
    public int timesCount;

    public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();
    public Map<String, Integer> playerMarkerCount = new LinkedHashMap<>();
    public Map<Vector3, Integer> posActivityMap = new LinkedHashMap<>();
    public int maxActivity;
    public Map<String, Boolean> playerNameEnabledMap = new LinkedHashMap<>();

    public JLabel CoordinateLabel;
    public JLabel SelectedEntryLabel;
    public JLabel RenderedPointsLabel;
    private LogEntry selectedEntry;

    public int renderedPoints = 0;

    private boolean zoomer;
    private boolean dragger;
    private boolean released;

    public float sensitivity = 1.0F;
    private float curZoomFactor = 1.0F;
    private float zoomFactor = 1.0F;
    private float prevZoomFactor = 1.0F;
    private float xOffset = 0.0F;
    private float yOffset = 0.0F;
    private int xDiff;
    private int yDiff;
    private float curX;
    private float curY;
    private Point startPoint;
    private Point mousePosition;

    public int minX;
    public int minY;
    public int maxX;
    public int maxY;
    private int xRange;
    private int yRange;

    public BufferedImage backgroundImage;
    public int xBackgroundOffset, zBackgroundOffset;
    public float backgroundOpacity = 0.5f;

    public int dateTimeIndex = 0;
    public LocalDateTime startDate;
    public LocalDateTime endDate;

    public int upscale = 1;
    public JLabel imageExportStatus;

    private AffineTransform at;
    private AffineTransform inverse;

    private final PlayerTrackerDecoder main;

    public Panel(Settings settings, Logger logger, PlayerTrackerDecoder main) {
        super(true);

        this.settings = settings;
        this.logger = logger;
        this.main = main;

        logger.Log("Initializing main display subsystem", Logger.MessageType.INFO);

        initComponent();

        if (settings.antialiasing) {
            logger.Log("Antialiasing enabled on main display panel", Logger.MessageType.INFO);
        }

        logger.Log("Successfully initialized main display subsystem", Logger.MessageType.INFO);
    }

    private void initComponent() {
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        setBackground(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.lightGray : Color.darkGray);
        mousePosition = new Point();
    }

    public BufferedImage LoadBackgroundImage(File imageFile) throws IOException {
        final long nowMs = System.currentTimeMillis();

        BufferedImage image = ImageIO.read(imageFile);

        final long durMs = System.currentTimeMillis() - nowMs;

        logger.Log("Loading world background image took " + durMs + "ms.", Logger.MessageType.INFO);

        return image;
    }

    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000 / (double) settings.fpsLimit;
        double delta = 0;
        int frames = 0;
        double time = (double) System.currentTimeMillis();

        int fpsLimit = settings.fpsLimit;
        while (isRunning) {
            if (settings.fpsLimit != fpsLimit) {
                ns = 1000000000 / (double) settings.fpsLimit;
            }

            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            if (delta >= 1) {
//                if (ShouldDraw) {
//                    System.out.println("thread run");

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

//                float speed = 2f;
                curX = Utils.smoothStep(curX, xOffset + xDiff, Utils.clamp01(0.3f)); // (6f / (float) curFPS) * speed)
                curY = Utils.smoothStep(curY, yOffset + yDiff, Utils.clamp01(0.3f));

                curZoomFactor = Utils.smoothStep(curZoomFactor, zoomFactor, Utils.clamp01(0.3f));

                at = new AffineTransform();
                at.translate(curX, curY);
                at.scale(curZoomFactor, curZoomFactor);

                try {
                    inverse = at.createInverse();
                } catch (NoninvertibleTransformException nte) {
                    logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
                }

                update = true;
                repaint();
//                }

                frames++;
                delta--;
                if (System.currentTimeMillis() - time >= 1000) {
                    curFPS = frames;
                    time += 1000;
                    frames = 0;
                }
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
            float xRel = (float) MouseInfo.getPointerInfo().getLocation().getX() - (float) getLocationOnScreen().getX();
            float yRel = (float) MouseInfo.getPointerInfo().getLocation().getY() - (float) getLocationOnScreen().getY();
            float zoomDiv = zoomFactor / prevZoomFactor;
            xOffset = zoomDiv * xOffset + (1.0F - zoomDiv) * xRel;
            yOffset = zoomDiv * yOffset + (1.0F - zoomDiv) * yRel;

//            at = new AffineTransform();
//            at.translate(xOffset, yOffset);
//            at.scale(zoomFactor, zoomFactor);
//            g2.transform(at);
//
//            try {
//                inverse = at.createInverse();
//            } catch (NoninvertibleTransformException nte) {
//                logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
//            }

            prevZoomFactor = zoomFactor;
            zoomer = false;
        }

        if (dragger) {
//            at = new AffineTransform();
//            at.translate(curXOffset + curXDiff, curYOffset + curYDiff);
//            at.scale(zoomFactor, zoomFactor);
//            g2.transform(at);

//            try {
//                inverse = at.createInverse();
//            } catch (NoninvertibleTransformException nte) {
//                logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
//            }

            if (released) {
                xOffset += xDiff;
                yOffset += yDiff;
                xDiff = 0;
                yDiff = 0;

                dragger = false;
            }
        }

//        if (update) {
//            update = false;
//            if (at != null) {
//            }
//
////            try {
////                inverse = at.createInverse();
////            } catch (NoninvertibleTransformException nte) {
////                logger.Log("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()), Logger.MessageType.ERROR);
////            }
//        }

        g2.transform(at);

//        drawCrossHair(g2, 1.0f);

        if (backgroundImage != null) {
            // (-6384, -5376), (8959, 2767)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));

            g2.drawImage(backgroundImage, xBackgroundOffset, zBackgroundOffset, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        }

        drawPoints(g2, true, 0, 0, 1);

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(1));
        float padding = 1.25F;
        g2.drawRect((int) (minX * padding), (int) (minY * padding), (int) (xRange * padding), (int) (yRange * padding));
        g2.setColor(Color.white);
        if (selectedEntry != null) {
            g2.setStroke(new BasicStroke(0.25f));
            drawRectangle(g2, selectedEntry.position.x, selectedEntry.position.z, 3, false);
        }

        g2.dispose();

//        RenderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | diffX: %d, diffY: %d | startX: %d, startY: %d | %d FPS | ", totalData, renderedPoints, zoomFactor, xDiff, yDiff, startPoint == null ? 0 : startPoint.x, startPoint == null ? 0 : startPoint.y, curFPS));
//        RenderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | curX: %.3f, curY: %.3f | %d FPS | ", totalData, renderedPoints, zoomFactor, curX, curY, curFPS));
        RenderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | %d FPS | ", totalData, renderedPoints, zoomFactor, curFPS));
    }

    private void drawCrossHair(Graphics2D g2d, float size) {
        g2d.setStroke(new BasicStroke(5));
        g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
        float halfSize = size / 2;
        float fifthSize = size / 5;
        float otherSize = size / 1.66f;
        drawDot(g2d, -size / 2, -size / 2, size, false);
        drawLine(g2d, 0, fifthSize, 0, otherSize, 1);
        drawLine(g2d, 0, -fifthSize, 0, -otherSize, 1);
        drawLine(g2d, -fifthSize, 0, 0, -otherSize, 1);
        drawLine(g2d, 0, fifthSize, 0, otherSize, 1);
    }

    private void drawPoints(Graphics2D g2d, boolean useCulling, int offsetX, int offsetY, int upscale) {
        Point pt = new Point();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        renderedPoints = 0;
        playerLastPosMap.clear();
        ArrayList<String> playerFirst = new ArrayList<>();
        Map<String, Integer> playerOccurences = new LinkedHashMap<>();

        if (settings._drawType == Decoder.DrawType.Heat) {
            Vector3[] positions = posActivityMap.keySet().toArray(new Vector3[0]);
            for (Vector3 vec : positions) {
                if (at != null) at.transform(vec.toPoint(), pt);

                int x = offsetUpscale(vec.x, offsetX, upscale, Math.abs(minX));
                int y = offsetUpscale(vec.z, offsetY, upscale, Math.abs(minY));

                if (!useCulling || (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50)) {
                    float value = Math.min(1, (float) (posActivityMap.get(vec) * Math.abs(settings.heatMapThreshold)) / (float) (maxActivity));

                    if (settings._heatDrawType == PlayerTrackerDecoder.HeatDrawType.Color) {
                        try {
                            if (settings.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
                                g2d.setColor(Utils.lerpColor(Color.darkGray, Color.getHSBColor(0.93f, 0.68f, 0.55f), value));
                            } else {
                                g2d.setColor(Utils.lerpColor(Color.lightGray.brighter(), Color.getHSBColor(0.93f, 0.68f, 0.55f), value));
                            }
                        } catch (IllegalArgumentException e) {
                            logger.Log("Something wrong happened when lerping colors for the heatmap color (Probably the stupid negative input error): " + Arrays.toString(e.getStackTrace()), Logger.MessageType.ERROR);
                        }

                        drawRectangle(g2d, x, y, settings.size, true);
                    } else {
                        drawRectangle(g2d, x, y, settings.size * value, true);
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

                        int x = offsetUpscale(entry.position.x, offsetX, upscale, Math.abs(minX));
                        int y = offsetUpscale(entry.position.z, offsetY, upscale, Math.abs(minY));

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
                                        drawArrowLine(g2d, x, y, offsetUpscale(lastPos.x, offsetX, upscale, Math.abs(minX)), offsetUpscale(lastPos.z, offsetY, upscale, Math.abs(minY)), settings.size * 2, settings.size * 2);
                                    } else {
                                        drawLine(g2d, x, y, offsetUpscale(lastPos.x, offsetX, upscale, Math.abs(minX)), offsetUpscale(lastPos.z, offsetY, upscale, Math.abs(minY)), settings.size);
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

                        int x = offsetUpscale(pos.x, offsetX, upscale, Math.abs(minX));
                        int y = offsetUpscale(pos.z, offsetY, upscale, Math.abs(minY));

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
        if (e.getPreciseWheelRotation() < 0 && zoomFactor < 50.0F) {
            zoomFactor *= 1.05F * sensitivity;
//            repaint();
        } else if (e.getPreciseWheelRotation() > 0 && zoomFactor > 0.02F) {
            zoomFactor /= 1.05F * sensitivity;
//            repaint();
        }
    }

    public void mouseDragged(MouseEvent e) {
        Point curPoint = e.getLocationOnScreen();
        xDiff = (int) ((curPoint.x - startPoint.x) * sensitivity);
        yDiff = (int) ((curPoint.y - startPoint.y) * sensitivity);
        dragger = true;
    }

    public void mouseMoved(MouseEvent e) {
        if (inverse != null) inverse.transform(e.getPoint(), mousePosition);
        CoordinateLabel.setText(" (" + mousePosition.x + ", " + mousePosition.y + ") | ");
    }

    public void mouseClicked(MouseEvent e) {
//        update = true;
        SelectedEntryLabel.setText("Nothing Selected");
        selectedEntry = null;
        boolean done = false;
        for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
            ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);
            for (LogEntry entry : entries) {
                if (selectedEntry != entry && Math.abs(entry.position.x - mousePosition.x) < 2 && Math.abs(entry.position.z - mousePosition.y) < 2) {
                    selectedEntry = entry;
                    SelectedEntryLabel.setText(entry.toString());

                    logger.Log("Selected a log entry: " + entry, Logger.MessageType.INFO);
                    done = true;
                    break;
                }
            }

            if (done) break;
        }
    }

    public void mousePressed(MouseEvent e) {
        released = false;
        startPoint = MouseInfo.getPointerInfo().getLocation();
    }

    public void mouseReleased(MouseEvent e) {
        released = true;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void SaveAsImage(PlayerTrackerDecoder playerTrackerDecoder) {
        logger.Log("Started saving current screen as an image. Currently preparing the image", Logger.MessageType.INFO);
        imageExportStatus.setText("  Processing...");
        playerTrackerDecoder.revalidate();
        playerTrackerDecoder.repaint();

        System.gc();

        int imagePadding = 100;
        BufferedImage image = null;

        try {
            image = new BufferedImage((xRange + (imagePadding * 2)) * upscale, (yRange + (imagePadding * 2)) * upscale, BufferedImage.TYPE_INT_RGB);

            System.gc();

            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            //g2d.transform(at); // Use to capture only the current view (aka. screenshot)

//            if (backgroundImage != null) {
//                // (-6384, -5376), (8959, 2767)
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));
//
//                g2d.drawImage(backgroundImage, xBackgroundOffset, zBackgroundOffset, null);
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
//            } else {
            g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.lightGray : Color.darkGray);
//
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
//            }

            drawPoints(g2d, false, imagePadding, imagePadding, upscale);

            g2d.setColor(Color.black);
            for (LocalDateTime time : logEntriesGroupedByTime.keySet()) {
                ArrayList<LogEntry> entries = logEntriesGroupedByTime.get(time);

                for (LogEntry entry : entries) {
                    if (playerNameEnabledMap.get(entry.playerName)) {
                        int x = offsetUpscale(entry.position.x, imagePadding, upscale, Math.abs(minX));
                        int y = offsetUpscale(entry.position.z, imagePadding, upscale, Math.abs(minY));

                        Color col = playerNameColorMap.get(entry.playerName);
                        g2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue()));

                        drawRectangle(g2d, x, y, settings.size, true);
                    }
                }
            }

            g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24 * upscale));
            g2d.drawString(String.format("Scale: 1 pixel = %.1f block(s)", (1.0f / (float) upscale)), 24, 24 + (24 * upscale));

            g2d.dispose();
        } catch (OutOfMemoryError ex) {
            logger.Log("Error preparing the image to export (Out of memory):\n   " + Arrays.toString(ex.getStackTrace()), Logger.MessageType.ERROR);
        }

        logger.Log("Starting to save the exported image file", Logger.MessageType.INFO);

        if (!new File("outputs").exists()) {
            boolean val = new File("outputs").mkdir();
            logger.Log("Outputs folder to save exported image didn't exist so it was just created with result: " + val, Logger.MessageType.WARNING);
        }

        String name = settings._drawType + "-export-" + _Decoder.dataWorld + "-" + _Decoder.dataDate;
        File[] outFiles = new File("outputs/").listFiles();
        int count = 0;

        if (outFiles != null) {
            for (File file : outFiles) {
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

    private int offsetUpscale(int input, int offset, float upscale, float fallback) {
        return (int) ((input + (offset == 0 ? offset : fallback + offset)) * upscale);
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