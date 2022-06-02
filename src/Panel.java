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
    public boolean ShouldDraw = false;
    public boolean exporting = false;
    public boolean isPlaying = false;
    private long last = 0;
    private double curFPS = 0;
    private double frameTime = 0;
    private ArrayList<Double> frameTimeHistory = new ArrayList<>();

    private final Font smallFont = new Font("Arial", Font.PLAIN, 12);

    public ArrayList<LogEntry> logEntries = new ArrayList<>();
    public ArrayList<LocalDateTime> logDates = new ArrayList<>();
    private int totalData;

    //    private Map<LocalDateTime, ArrayList<LogEntry>> logEntriesGroupedByTime = new LinkedHashMap<>();
    public int timesCount;

    public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();
    public Map<String, Integer> playerMarkerCount = new LinkedHashMap<>();
    public Map<Vector3, Integer> posActivityMap = new LinkedHashMap<>();
    public int maxActivity;
    public ArrayList<LogEntry> enabledEntries = new ArrayList<>();
    public Map<String, Boolean> playerNameEnabledMap = new LinkedHashMap<>();

    public JLabel CoordinateLabel;
    public JLabel SelectedEntryLabel;
    public JLabel RenderedPointsLabel;
    private LogEntry selectedEntry;

    public int renderedPoints = 0;

    private boolean zoomer;
    private boolean dragger;
    private boolean released;

    public float sensitivity = 1;
    private double curZoomFactor = 1;
    private double zoomFactor = 1;
    private double prevZoomFactor = 1;
    private double xTarget = 0;
    private double yTarget = 0;
    private double xOffset = 0;
    private double yOffset = 0;
    private double xDiff;
    private double yDiff;
    private double curX;
    private double curY;
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

    private AffineTransform at = new AffineTransform();
    private AffineTransform inverse = new AffineTransform();

    private final PlayerTrackerDecoder main;

    public Panel(Settings settings, Logger logger, PlayerTrackerDecoder main) {
        super(true);
        setOpaque(true);

        this.settings = settings;
        this.logger = logger;
        this.main = main;

        logger.info("Initializing main display subsystem", 1);

        initComponents();

        if (settings.fancyRendering) {
            logger.info("Antialiasing enabled on main display panel", 0);
        }

        logger.info("Successfully initialized main display subsystem", 1);
    }

    private void initComponents() {
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

        logger.info("Loading world background image took " + durMs + "ms.", 0);

        return image;
    }


    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000 / (double) settings.fpsLimit;
        long start;
        double delta = 0;

        int fpsLimit = settings.fpsLimit;
        while (isRunning) {
            if (settings.fpsLimit != fpsLimit) {
                ns = 1000000000 / (double) settings.fpsLimit;
            }

            start = System.nanoTime();
            delta += (start - lastTime) / ns;
            lastTime = start;

            if (delta >= 1) {
//                if (ShouldTick) {
//                System.out.println("thread run");
                if (isPlaying) {
                    if (dateTimeIndex < timesCount) {
                        endDate = logDates.get(dateTimeIndex);
                        updatePoints(false);
                        dateTimeIndex++;
//                            ShouldTick = true;

                        logger.info("Playing animated", 0);
                    } else {
                        isPlaying = false;
//                            ShouldTick = false;

                        logger.info("Finished playing", 0);
                    }

                    main.dateRangeSlider.setUpperValue(dateTimeIndex);
                    main.startDateLabel.setText(startDate.toString().replace("T", "; "));
                    main.endDateLabel.setText(endDate.toString().replace("T", "; "));
                }

                if (!exporting) {
                    float speed = 0.3f; // Utils.clamp01((6f / (float) curFPS) * 2.0f));
                    curX = Utils.smoothStep(curX, xTarget, speed);
                    curY = Utils.smoothStep(curY, yTarget, speed);

                    curZoomFactor = Utils.smoothStep(curZoomFactor, zoomFactor, speed);

                    at = new AffineTransform();
                    at.translate(curX, curY);
                    at.scale(curZoomFactor, curZoomFactor);
                }

                try {
                    inverse = at.createInverse();
                } catch (NoninvertibleTransformException nte) {
                    logger.error("Error inverting rendering transformation:\n   " + Arrays.toString(nte.getStackTrace()));
                }

//                        if (!isPlaying && Utils.approximately(curX, xTarget, 0.001f) && Utils.approximately(curY, yTarget, 0.001f) && Utils.approximately(curZoomFactor, zoomFactor, 0.001f)) {
//                            ShouldTick = false;
//                        }

                if (ShouldDraw) {
                    repaint();
                }
//                }

                delta--;
            }
        }
    }

    public void paintComponent(Graphics g) {
        last = System.nanoTime();

        if (!ShouldDraw)
            return;

        Graphics2D g2 = (Graphics2D) g;
//        g2.setClip(0, 0, getWidth(), getHeight());

        g2.setRenderingHints(settings.renderingHints);

        if (!exporting) {
            if (zoomer) {
                double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
                double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

                double zoomDiv = zoomFactor / prevZoomFactor;
                xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * (xRel);
                yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * (yRel);
                xTarget = xOffset;
                yTarget = yOffset;

                prevZoomFactor = zoomFactor;

                zoomer = false;
            }

            if (dragger) {
                xTarget = xOffset + xDiff;
                yTarget = yOffset + yDiff;

                if (released) {
                    xOffset += xDiff;
                    yOffset += yDiff;
                    xTarget = xOffset;
                    yTarget = yOffset;

                    dragger = false;
                }
            }

            if (PlayerTrackerDecoder.debugMode) {
                g2.setColor(Color.blue.brighter());

                Dimension size = getSize();
                float halfX = (float) (size.getWidth() / 2.0);
                float halfY = (float) (size.getHeight() / 2.0);
                drawLine(g2, halfX, halfY, (float) (halfX + xDiff), (float) (halfY + yDiff), 1.0f);
                drawCrossHair(g2, (float) (halfX + xDiff), (float) (halfY + yDiff), 0.5f, String.format("Drag difference: (%.3f, %.3f)", xDiff, yDiff));
            }
        }

        g2.transform(at);

        if (backgroundImage != null) {
            // (-6384, -5376), (8959, 2767)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));

            g2.drawImage(backgroundImage, xBackgroundOffset, zBackgroundOffset, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        }

        drawPoints(g2, true, 0, 0, 1);

        g2.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
        g2.setStroke(new BasicStroke(1));
        float padding = 1.25F;
        g2.drawRect((int) (minX * padding), (int) (minY * padding), (int) (xRange * padding), (int) (yRange * padding));
        g2.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
        if (selectedEntry != null) {
            g2.setStroke(new BasicStroke(0.25f));
            drawRectangle(g2, selectedEntry.position.x, selectedEntry.position.z, 3, false);
        }

        if (PlayerTrackerDecoder.debugMode) {
            drawCrossHair(g2, 0, 0, 2, "Origin");
            drawCrossHair(g2, 500, 0, 1, "500");
            drawCrossHair(g2, -500, 0, 1, "500");
            drawCrossHair(g2, 0, 500, 1, "500");
            drawCrossHair(g2, 0, -500, 1, "500");

            drawCrossHair(g2, 1000, 0, 1, "1000");
            drawCrossHair(g2, -1000, 0, 1, "1000");
            drawCrossHair(g2, 0, 1000, 1, "1000");
            drawCrossHair(g2, 0, -1000, 1, "1000");

            g2.setColor(Color.orange);
            drawCrossHair(g2, xBackgroundOffset, zBackgroundOffset, 0.75f, String.format("Background offset: (%d, %d)", xBackgroundOffset, zBackgroundOffset));

            if (startPoint != null) {
                g2.setColor(Color.pink);
                drawCrossHair(g2, startPoint.x, startPoint.y, 0.5f, String.format("Start point: (%d, %d)", startPoint.x, startPoint.y));
            }

            g2.setColor(Color.green.darker());
            drawCrossHair(g2, (float) xOffset, (float) yOffset, 0.5f, String.format("Render offset: (%.3f, %.3f)", xOffset, yOffset));

            RenderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | targetX: %.3f, targetY: %.3f | curX: %.3f, curY: %.3f | %.3f FPS | shouldDraw: %b | frameTime: %.3f ms | ", totalData, renderedPoints, curZoomFactor, xTarget, yTarget, curX, curY, curFPS, ShouldDraw, frameTime));
//            RenderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | curX: %.3f, curY: %.3f | %d FPS | ", totalData, renderedPoints, zoomFactor, curX, curY, curFPS));
        } else {
            RenderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | %.3f FPS | ", totalData, renderedPoints, curZoomFactor, curFPS));
        }

        g2.dispose();

        double delta = System.nanoTime() - last;
        calculateTimings(delta);
    }

    private void calculateTimings(double delta) {
        if (frameTimeHistory.size() < 10) {
            frameTimeHistory.add(delta / 1000000.0);
        } else {
            frameTimeHistory.remove(0);
            frameTimeHistory.add(delta / 1000000.0);
        }
        frameTime = Utils.calculateAverage(frameTimeHistory);
        curFPS = 1000.0 / frameTime; // 1.e9 / frameTime // (in nanos)
    }

    private BufferedImage toCompatibleImage(BufferedImage image) {
        // obtain the current system graphical settings
        GraphicsConfiguration gfxConfig = GraphicsEnvironment.
                getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();

        /*
         * if image is already compatible and optimized for current system
         * settings, simply return it
         */
        if (image.getColorModel().equals(gfxConfig.getColorModel()))
            return image;

        // image is not optimized, so create a new image that is
        BufferedImage newImage = gfxConfig.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());

        // get the graphics context of the new image to draw the old image on
        Graphics2D g2d = newImage.createGraphics();

        // actually draw the image and dispose of context no longer needed
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // return the new optimized image
        return newImage;
    }

    //region Drawing
    private void drawCrossHair(Graphics2D g2d, float x, float y, float size) {
        g2d.setStroke(new BasicStroke(5 * size));
        g2d.draw(new Ellipse2D.Float(x + (-25 * size), y + (-25 * size), 50 * size, 50 * size));
        g2d.draw(new Line2D.Float(x, y + (10 * size), x, y + (30 * size)));
        g2d.draw(new Line2D.Float(x, y + (-10 * size), x, y + (-30 * size)));
        g2d.draw(new Line2D.Float(x + (-10 * size), y, x + (-30 * size), y));
        g2d.draw(new Line2D.Float(x + (10 * size), y, x + (30 * size), y));
    }

    private void drawCrossHair(Graphics2D g2d, float x, float y, float size, String label) {
        g2d.setFont(smallFont);
        g2d.drawString(label, (int) x + (25 * size) + 10, (int) y + (25 * size) + 10);
        g2d.setStroke(new BasicStroke(5 * size));
        g2d.draw(new Ellipse2D.Float(x + (-25 * size), y + (-25 * size), 50 * size, 50 * size));
        g2d.draw(new Line2D.Float(x, y + (10 * size), x, y + (30 * size)));
        g2d.draw(new Line2D.Float(x, y + (-10 * size), x, y + (-30 * size)));
        g2d.draw(new Line2D.Float(x + (-10 * size), y, x + (-30 * size), y));
        g2d.draw(new Line2D.Float(x + (10 * size), y, x + (30 * size), y));
    }

    private BasicStroke stroke;
    private BasicStroke dashedStroke;
    private ArrayList<String> playerFirst = new ArrayList<>();
    private Map<String, Integer> playerOccurrences = new HashMap<>();
    private Dimension screenSize;

    private void drawPoints(Graphics2D g2d, boolean useCulling, int offsetX, int offsetY, int upscale) {
        Point pt = new Point();
        screenSize = getSize();
        renderedPoints = 0;
        playerLastPosMap.clear();
        playerFirst.clear();
        playerOccurrences.clear();

        stroke = new BasicStroke(settings.size);
        dashedStroke = new BasicStroke((settings.size / 2.0F), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, new float[]{9.0F}, 0.0F);

        g2d.setColor(Color.black);

        for (LogEntry entry : enabledEntries) {
            if (settings.ageFade) {
                if (!playerOccurrences.containsKey(entry.playerName)) {
                    playerOccurrences.put(entry.playerName, 1);
                } else {
                    playerOccurrences.put(entry.playerName, playerOccurrences.get(entry.playerName) + 1);
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
                int val = 255;
                if (settings.ageFade) {
                    int markerCount = playerMarkerCount.get(entry.playerName);
                    val = Utils.clamp(Utils.lerp(255, 0, Math.min(markerCount, playerOccurrences.get(entry.playerName) * settings.ageFadeStrength) / (float) markerCount), 0, 255);
                }
                g2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), val));

                if (settings._drawType == Decoder.DrawType.Pixel) {
                    drawRectangle(g2d, x, y, settings.size, true);
                    renderedPoints++;
                } else if (settings._drawType == Decoder.DrawType.Dot) {
                    drawDot(g2d, x, y, settings.size, true);
                    renderedPoints++;
                } else if (settings._drawType == Decoder.DrawType.Heat) {
                    if (settings._heatDrawType == PlayerTrackerDecoder.HeatDrawType.Color) {
                        try {
                            if (settings.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
                                g2d.setColor(Utils.lerpColor(Color.darkGray, Color.getHSBColor(0.93f, 0.68f, 0.55f), Math.min(1, (posActivityMap.get(entry.position) * Math.abs(settings.heatMapStrength)) / (float) (maxActivity))));
                            } else {
                                g2d.setColor(Utils.lerpColor(Color.lightGray, Color.getHSBColor(0.93f, 0.68f, 0.55f), Math.min(1, (posActivityMap.get(entry.position) * Math.abs(settings.heatMapStrength)) / (float) (maxActivity))));
                            }
                        } catch (IllegalArgumentException e) {
                            logger.error("Something wrong happened when lerping colors for the heatmap color (Probably the stupid negative input error): " + Arrays.toString(e.getStackTrace()));
                        }

                        drawRectangle(g2d, x, y, settings.size, true);
                    } else {
                        g2d.setColor(playerNameColorMap.get(entry.playerName));
                        drawRectangle(g2d, x, y, settings.size * Math.min((float) posActivityMap.get(entry.position) * (settings.heatMapStrength / 50.0f) + 0.5f, maxActivity + 0.5f), true);
                    }
                    renderedPoints++;
                }
            }

            if (settings._drawType == Decoder.DrawType.Line) {
                Vector3 lastPos = playerLastPosMap.get(entry.playerName);

                if (!useCulling || (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50)) {
                    Color col = playerNameColorMap.get(entry.playerName);
                    int val = 255;
                    if (settings.ageFade) {
                        int markerCount = playerMarkerCount.get(entry.playerName);
                        val = Utils.clamp(Utils.lerp(255, 0, Math.min(markerCount, playerOccurrences.get(entry.playerName) * settings.ageFadeStrength) / (float) markerCount), 0, 255);
                    }

                    g2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), val));

                    if (lastPos != null && (settings.hiddenLines || (Math.abs(entry.position.x - lastPos.x) <= settings.lineThreshold && Math.abs(entry.position.z - lastPos.z) <= settings.lineThreshold))) {
                        if (settings.hiddenLines && (Math.abs(entry.position.x - lastPos.x) > settings.lineThreshold || Math.abs(entry.position.z - lastPos.z) > settings.lineThreshold)) {
                            g2d.setStroke(dashedStroke);
                        } else {
                            g2d.setStroke(stroke);
                        }

                        if (settings.fancyLines) {
                            drawArrowLine(g2d, x, y, offsetUpscale(lastPos.x, offsetX, upscale, Math.abs(minX)), offsetUpscale(lastPos.z, offsetY, upscale, Math.abs(minY)), settings.size * 2, settings.size * 2);
                        } else {
                            drawLine(g2d, x, y, offsetUpscale(lastPos.x, offsetX, upscale, Math.abs(minX)), offsetUpscale(lastPos.z, offsetY, upscale, Math.abs(minY)), settings.size);
                        }
                        renderedPoints++;
                    }
                }

                playerLastPosMap.put(entry.playerName, entry.position);
            }
        }

        if (settings.terminusPoints && settings._drawType == Decoder.DrawType.Line) {
            for (String name : playerLastPosMap.keySet()) {
                if (playerNameEnabledMap.containsKey(name)) {
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

    private void drawRectangle(Graphics2D g2d, float x, float y, float size, boolean filled) {
        if (filled) g2d.fill(new Rectangle2D.Float(x - size / 2, y - size / 2, size, size));
        else g2d.draw(new Rectangle2D.Float(x - size / 2, y - size / 2, size, size));
    }

    private void drawDot(Graphics2D g2d, float x, float y, float size, boolean filled) {
        if (filled) g2d.fill(new Ellipse2D.Float(x - size / 2, y - size / 2, size, size));
        else g2d.draw(new Ellipse2D.Float(x - size / 2, y - size / 2, size, size));
    }

    private void drawLine(Graphics2D g2d, float x1, float y1, float x2, float y2, float thickness) {
        g2d.draw(new Line2D.Float(x1 - thickness / 2, y1 - thickness / 2, x2 - thickness / 2, y2 - thickness / 2));
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
    //endregion

    //region Data
    public void updatePoints(boolean log) {
        if (log) logger.info("Updating points", 0);

//        logEntriesGroupedByTime.clear();
        playerMarkerCount.clear();
        posActivityMap.clear();
        enabledEntries.clear();
        maxActivity = 0;

        for (LogEntry entry : logEntries) {
            if (!entry.isChunk && ((entry.time.isAfter(startDate) && entry.time.isBefore(endDate)) || entry.time.equals(startDate) || entry.time.equals(endDate))) {
                if (playerNameEnabledMap.containsKey(entry.playerName)) {
                    enabledEntries.add(entry);
                }

                if (!playerMarkerCount.containsKey(entry.playerName)) {
                    playerMarkerCount.put(entry.playerName, 1);
                } else {
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

//        repaint();

        if (log) logger.info("Updated points: " + logEntries.size(), 0);
    }

    public void setData(Decoder dec) {
        logger.info("Setting data to display", 0);

        _Decoder = dec;
        logEntries = _Decoder.logEntries;
        logDates = _Decoder.logDates;
        timesCount = logDates.size();

        logger.info("Passed time getting", 1);

//        logEntriesGroupedByTime.clear();

        playerNameColorMap = _Decoder.playerNameColorMap;

        playerLastPosMap = _Decoder.playerLastPosMap;
        playerNameEnabledMap = _Decoder.playerNameEnabledMap;

        minX = _Decoder.minX;
        minY = _Decoder.minY;
        maxX = _Decoder.maxX;
        maxY = _Decoder.maxY;
        xRange = _Decoder.xRange;
        yRange = _Decoder.yRange;
        setPreferredSize(new Dimension(xRange, yRange));

        zoomFactor = 0.5;
        prevZoomFactor = 0.5;
        curZoomFactor = 0.5;

        totalData = logEntries.size();
        Collections.sort(logEntries);
        logger.info("Passed sorting", 1);

        SelectedEntryLabel.setText("Nothing Selected");
        selectedEntry = null;

        logger.info("Successfully set data to display", 0);
    }
    //endregion

    //region Inputs
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomer = true;
        if (e.getWheelRotation() < 0) {
            zoomFactor = Math.min(50.0f, zoomFactor * (1.05f * sensitivity));
            repaint();

//            ShouldTick = true;
        } else if (e.getWheelRotation() > 0) {
            zoomFactor = Math.max(0.02f, zoomFactor / (1.05f * sensitivity));
            repaint();

//            ShouldTick = true;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point curPoint = e.getLocationOnScreen();
        xDiff = (curPoint.x - startPoint.x) * sensitivity;
        yDiff = (curPoint.y - startPoint.y) * sensitivity;
        dragger = true;
//        ShouldTick = true;

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (inverse != null) inverse.transform(e.getPoint(), mousePosition);
        CoordinateLabel.setText(" (" + mousePosition.x + ", " + mousePosition.y + ") | ");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        SelectedEntryLabel.setText("Nothing Selected");
        boolean found = false;
        for (LogEntry entry : logEntries) {
            if (selectedEntry != entry && Math.abs(entry.position.x - mousePosition.x) < 2 && Math.abs(entry.position.z - mousePosition.y) < 2) {
                selectedEntry = entry;
                SelectedEntryLabel.setText(entry.toString());

                logger.info("Selected a log entry: " + entry, 1);
                found = true;
                break;
            }
        }

        if (!found) {
            selectedEntry = null;
            SelectedEntryLabel.setText("Nothing Selected");
        }

//        ShouldTick = true;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        xDiff = 0;
        yDiff = 0;
        released = false;
        startPoint = MouseInfo.getPointerInfo().getLocation();

//        ShouldTick = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        released = true;
//        ShouldTick = true;

        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    //endregion

    public void SaveAsImage(boolean screenshot) {
        logger.info("Started saving current screen as an image. Currently preparing the image", 1);
        imageExportStatus.setText("  Processing...");
        main.revalidate();
        main.repaint();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        exporting = true;

        System.gc();

        BufferedImage image = null;
        int _minX = Math.abs(minX);
        int _minY = Math.abs(minY);

        try {
            image = new BufferedImage((screenshot ? getWidth() : xRange) * upscale, (screenshot ? getHeight() : yRange) * upscale, BufferedImage.TYPE_INT_RGB);
            image = toCompatibleImage(image);

            System.gc();

            Graphics2D g2d = image.createGraphics();

            if (screenshot) {
                g2d.transform(at);
            }

            g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.lightGray : Color.darkGray);

            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

            if (backgroundImage != null) {
                // (-6384, -5376), (8959, 2767)
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));

                g2d.drawImage(backgroundImage, (xBackgroundOffset * upscale + (!screenshot ? Math.abs(_minX) : 0)), (zBackgroundOffset * upscale + (!screenshot ? Math.abs(_minY) : 0)), null);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
            }

            if (screenshot) {
                drawPoints(g2d, false, 0, 0, upscale);
            } else {
                drawPoints(g2d, false, _minX, _minY, upscale);

                g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
                g2d.setFont(new Font("Arial", Font.PLAIN, 24 * upscale));
                g2d.drawString(String.format("Scale: 1 pixel = %.1f block(s)", (1.0f / (float) upscale)), 24, 24 + (24 * upscale));
            }

            if (PlayerTrackerDecoder.debugMode) {
                drawCrossHair(g2d, _minX, _minY, 2, "Origin");
                drawCrossHair(g2d, _minX + 500, _minY, 1, "500");
                drawCrossHair(g2d, _minX + -500, _minY, 1, "500");
                drawCrossHair(g2d, _minX, _minY + 500, 1, "500");
                drawCrossHair(g2d, _minX, _minY + -500, 1, "500");

                drawCrossHair(g2d, _minX + 1000, _minY, 1, "1000");
                drawCrossHair(g2d, _minX + -1000, _minY, 1, "1000");
                drawCrossHair(g2d, _minX, _minY + 1000, 1, "1000");
                drawCrossHair(g2d, _minX, _minY + -1000, 1, "1000");
            }

            g2d.dispose();
        } catch (OutOfMemoryError ex) {
            logger.error("Error preparing the image to export (Out of memory):\n   " + Arrays.toString(ex.getStackTrace()));
        }

        logger.info("Starting to save the exported image file", 0);

        if (!new File("outputs").exists()) {
            boolean val = new File("outputs").mkdir();
            logger.warn("Outputs folder to save exported image didn't exist so it was just created with result: " + val);
        }

        String name = settings._drawType + "-" + (screenshot ? "screenshot" : "export") + "-" + _Decoder.dataWorld + "-" + _Decoder.dataDate;
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
                    logger.info("Successfully saved current screen as an image", 1);
                } else {
                    logger.error("Image to save is null");
                }
            } catch (Exception e) {
                logger.error("Error saving current screen as an image:\n   " + Arrays.toString(e.getStackTrace()));
            }

            imageExportStatus.setText("   Done!");
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        main.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        exporting = false;
    }

    private int offsetUpscale(int input, int offset, float upscale, int fallback) {
        return (int) ((input + (offset == 0 ? offset : fallback)) * upscale);
    }

    public void Reset() {
        logEntries.clear();
        logDates.clear();

//        logEntriesGroupedByTime.clear();

        playerNameColorMap.clear();
        playerLastPosMap.clear();
        enabledEntries.clear();
        playerMarkerCount.clear();
        posActivityMap.clear();

        totalData = 0;
        maxActivity = 0;
        SelectedEntryLabel.setVisible(false);
        selectedEntry = null;
    }
}