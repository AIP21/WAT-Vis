package com.anipgames.WAT_Vis;

import com.anipgames.WAT_Vis.util.Keyboard;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;
import com.seedfinding.mccore.util.math.Vec3i;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.anipgames.WAT_Vis.config.Settings;
import com.anipgames.WAT_Vis.mapping.minemap.map.MapContext;
import com.anipgames.WAT_Vis.mapping.minemap.map.fragment.Fragment;
import com.anipgames.WAT_Vis.mapping.minemap.map.fragment.FragmentScheduler;
import com.anipgames.WAT_Vis.util.objects.DrawInfo;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.anipgames.WAT_Vis.util.objects.LogEntry;
import com.anipgames.WAT_Vis.util.objects.Vector3;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener, Runnable {
    private final Settings settings;

    // region Status variables
    private boolean isRunning = true;
    public boolean shouldDraw = false;
    public boolean exporting = false;
    public boolean isPlaying = false;
    public int animationSpeed = 1;
    private boolean shouldUpdate = false;
    private boolean shouldUpdateLog = false;
    private long last = 0;
    private double curFPS = 0;
    private double frameTime = 0;
    private ArrayList<Double> frameTimeHistory = new ArrayList<>();
    // endregion

    // region Data variables
    public DecodedData decodedData;

    public LinkedHashMap<LocalDateTime, LogEntry> logEntries = new LinkedHashMap<>();
    public LocalDateTime[] logTimes;
    private int totalTimes = 0;
    private int totalData = 0;
    public String dataWorld;

    public Map<String, Color> playerNameColorMap = new LinkedHashMap<>();
    public Map<String, Vector3> playerLastPosMap = new LinkedHashMap<>();
    public Map<String, Integer> playerMarkerCount = new LinkedHashMap<>();
    public Map<Vector3, Integer> posActivityMap = new LinkedHashMap<>();
    public int maxActivity;
    public ArrayList<LogEntry> enabledEntries = new ArrayList<>();
    public Map<String, Boolean> playerNameEnabledMap = new LinkedHashMap<>();

    private HashSet<LogEntry> selectedEntries = new HashSet<>();

    public int renderedPoints = 0;

    public int minX;
    public int minY;
    public int maxX;
    public int maxY;
    private int xRange;
    private int yRange;
    // endregion

    // region Input variables
    private boolean zooming;
    private boolean dragging;
    private boolean released;
    private boolean selecting;

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
    public double curX;
    public double curY;
    private Point startPoint;
    private Point selectionStart = new Point();
    private Point selectionEnd = new Point();
    private Point mousePosition;
    // endregion

    // region UI variables
    private final Font mainFont = new Font("Arial", Font.PLAIN, 15);
    private final Font smallFont = new Font("Arial", Font.PLAIN, 12);

    public JLabel coordinateLabel;
    public JLabel selectedEntryLabel;
    public JLabel renderedPointsLabel;
    // endregion

    // region Rendering variables
    public BufferedImage backgroundImage;
    public int xBackgroundOffset, zBackgroundOffset;
    public float backgroundOpacity = 0.5f;

    public int dateTimeIndex = 0;
    public LocalDateTime startTime;
    public LocalDateTime endTime;

    public int upscale = 1;
    public JLabel imageExportStatus;

    private AffineTransform at = new AffineTransform();
    private AffineTransform inverse = new AffineTransform();
    // endregion

    // region Mapping variables
    public MapContext context;
    public int threadCount;
    public FragmentScheduler scheduler;
    private boolean hasWorldMap = false;

    public static final int DEFAULT_REGION_SIZE = 512;// 8192;
    public static final double DEFAULT_PIXELS_PER_FRAGMENT = 256.0;
    public int blocksPerFragment;
    public double pixelsPerFragment;
    // endregion

    public MainPanel(Settings settings) {
        this(settings, DEFAULT_REGION_SIZE);
    }

    public MainPanel(Settings settings, int blocksPerFragment) {
        super(true);

        this.blocksPerFragment = blocksPerFragment;
        pixelsPerFragment = (int) (DEFAULT_PIXELS_PER_FRAGMENT * (this.blocksPerFragment / DEFAULT_REGION_SIZE));

        setBackground(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.lightGray : Color.darkGray);

        this.settings = settings;

        Logger.info("Initializing main display subsystem");

        initComponents();
        initCommands();

        if (settings.fancyRendering) {
            Logger.info("Fancy rendering initialized on main display panel");
        }

        Logger.info("Successfully initialized main display subsystem");
    }

    //region Initialization
    private void initComponents() {
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        addMouseListener(this);
        mousePosition = new Point();
    }

    private void initCommands() {
        Keyboard.registerTypedAction(KeyEvent.VK_CONTROL, KeyEvent.VK_A, i -> {
            if (selectedEntries.size() != logEntries.size()) {
                selectedEntries.addAll(logEntries.values());
            } else {
                for (LogEntry entry : logEntries.values()) {
                    selectedEntries.remove(entry);
                }
            }
        });
    }
    //endregion

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
                // if (ShouldTick) {
                // System.out.println("thread run");
                if (!exporting && isPlaying) {
                    if (dateTimeIndex < totalTimes) {
                        endTime = logTimes[dateTimeIndex];
                        queuePointUpdate(false);
                        if (dateTimeIndex + animationSpeed < totalTimes) {
                            dateTimeIndex += animationSpeed;
                        } else {
                            dateTimeIndex = totalTimes;
                        }
                        // ShouldTick = true;
                    } else {
                        isPlaying = false;
                        // ShouldTick = false;
                        PlayerTrackerDecoder.INSTANCE.animatePlayPause.setSelected(isPlaying);

                        Logger.info("Finished playing");
                    }

                    PlayerTrackerDecoder.INSTANCE.timeRangeSlider.setUpperValue(dateTimeIndex);
                    PlayerTrackerDecoder.INSTANCE.startTimeLabel.setText(startTime.toString().replace("T", "; "));
                    PlayerTrackerDecoder.INSTANCE.endTimeLabel.setText(endTime.toString().replace("T", "; "));
                }

                if (!exporting) {
                    float speed = 0.3f; // Utils.clamp01((6f / (float) curFPS) * 2.0f));
                    curX = Utils.smoothStep(curX, xTarget, speed);
                    curY = Utils.smoothStep(curY, yTarget, speed);

                    curZoomFactor = Utils.smoothStep(curZoomFactor, zoomFactor, speed);
                    pixelsPerFragment = curZoomFactor * blocksPerFragment;

                    at = new AffineTransform();
                    at.translate(curX, curY);
                    at.scale(curZoomFactor, curZoomFactor);

//                    if (Keyboard.isKeyDown(KeyEvent.VK_UP)) {
//                        sc += 1;
//
//                        Logger.info("FACTOR UP: " + factor);
//                    } else if (Keyboard.isKeyDown(KeyEvent.VK_DOWN)) {
//                        factor = Math.max(1, factor - 1);
//
//                        Logger.info("FACTOR DOWN: " + scaleFactor);
//                    }
                }

                try {
                    inverse = at.createInverse();
                } catch (NoninvertibleTransformException e) {
                    Logger.err("Error inverting rendering transformation:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
                }

                // if (!isPlaying && Utils.approximately(curX, xTarget, 0.001f) &&
                // Utils.approximately(curY, yTarget, 0.001f) &&
                // Utils.approximately(curZoomFactor, zoomFactor, 0.001f)) {
                // ShouldTick = false;
                // }

                if (shouldDraw) {
                    repaint();
                }
                // }

                delta--;
            }
        }
    }

    public void paintComponent(Graphics g) {
        last = System.nanoTime();

        super.paintComponent(g);

        if (!shouldDraw) return;

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHints(settings.renderingHints);

        if (hasWorldMap) {
            scheduler.purge();
            drawMap(g2);
        }

        if (!exporting) {
            if (zooming) {
                double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
                double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

                double zoomDiv = zoomFactor / prevZoomFactor;
                xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * (xRel);
                yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * (yRel);
                xTarget = xOffset;
                yTarget = yOffset;

                prevZoomFactor = zoomFactor;

                zooming = false;
            }

            if (dragging) {
                xTarget = xOffset + xDiff;
                yTarget = yOffset + yDiff;

                if (released) {
                    xOffset += xDiff;
                    yOffset += yDiff;
                    xTarget = xOffset;
                    yTarget = yOffset;

                    dragging = false;
                }
            }
        }

        if (PlayerTrackerDecoder.DEBUG) {
            g2.setColor(Color.blue.brighter());

            Dimension size = getSize();
            float halfX = (float) (size.getWidth() / 2.0);
            float halfY = (float) (size.getHeight() / 2.0);
            drawLine(g2, halfX, halfY, (float) (halfX + xDiff), (float) (halfY + yDiff), 1.0f);
            drawCrossHair(g2, (float) (halfX + xDiff), (float) (halfY + yDiff), 0.5f, String.format("Drag difference: (%.3f, %.3f)", xDiff, yDiff));
        }

        g2.transform(at);

        if (selecting) {
            g2.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? new Color(167, 210, 255) : new Color(33, 65, 130));
            drawRectangle(g2, selectionStart.x - 0.5f, selectionStart.y - 0.5f, selectionEnd.x - 0.5f, selectionEnd.y - 0.5f, true, false);
        }

        if (backgroundImage != null) {
            // (-6384, -5376), (8959, 2767)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));

            g2.drawImage(backgroundImage, xBackgroundOffset, zBackgroundOffset, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        }

        if (shouldUpdate) {
            updatePoints();
            shouldUpdate = false;
        }

        drawPoints(g2, true, getSize(), 0, 0, 1);

        g2.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
        g2.setStroke(new BasicStroke(1));
        float padding = 1.25F;
        g2.drawRect((int) (minX * padding), (int) (minY * padding), (int) (xRange * padding), (int) (yRange * padding));

        g2.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
        g2.setStroke(new BasicStroke(0.25f));
        for (LogEntry selectedEntry : selectedEntries) {
            drawRectangle(g2, selectedEntry.position.x, selectedEntry.position.z, 3, false);
        }

        if (PlayerTrackerDecoder.DEBUG) {
            g2.setColor(Color.magenta);
            drawCrossHair(g2, mousePosition.x, mousePosition.y, 0.25f);

            g2.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
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

            renderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | curX: %.3f, curY: %.3f | px/frag: %f | %.3f FPS | shouldDraw: %b | frameTime: %.3f ms | ", totalData, renderedPoints, curZoomFactor, curX, curY, pixelsPerFragment, curFPS, shouldDraw, frameTime));
            // RenderedPointsLabel.setText(String.format(" Loaded: %d | Rendered: %d | Zoom:
            // %.3f | curX: %.3f, curY: %.3f | %d FPS | ", totalData, renderedPoints,
            // zoomFactor, curX, curY, curFPS));
        } else {
            renderedPointsLabel.setText(String.format("   Loaded: %d | Rendered: %d | Zoom: %.3f | %.3f FPS | ", totalData, renderedPoints, curZoomFactor, curFPS));
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

    // region Drawing
    private BasicStroke stroke;
    private BasicStroke dashedStroke;
    private ArrayList<String> playerFirst = new ArrayList<>();
    private Map<String, Integer> playerOccurrences = new HashMap<>();
    private Dimension screenSize;

    private void drawPoints(Graphics2D g2d, boolean useCulling, Dimension cullBounds, int offsetX, int offsetY, float upscale) {
        Point pt = new Point();
        screenSize = cullBounds;
        renderedPoints = 0;
        playerLastPosMap.clear();
        playerFirst.clear();
        playerOccurrences.clear();

        stroke = new BasicStroke(settings.size);
        dashedStroke = new BasicStroke((settings.size / 2.0F), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0F, new float[]{9.0F}, 0.0F);

        g2d.setColor(Color.black);

        boolean ctrlDown = Keyboard.getKeyPressed(KeyEvent.VK_CONTROL);
        for (LogEntry entry : enabledEntries) {
            if (entry.show || ctrlDown) {
                if (settings._drawType == PlayerTrackerDecoder.DrawType.Fast) {
                    if (at != null) at.transform(entry.position.point, pt);

                    int x = (int) ((entry.position.x + offsetX) * upscale);

                    int y = (int) ((entry.position.z + offsetY) * upscale);

                    if (!useCulling || (pt.x >= -50 && pt.x <= screenSize.width + 50 && pt.y >= -50 && pt.y <= screenSize.height + 50)) {
                        g2d.setColor(playerNameColorMap.get(entry.playerName));
                        g2d.drawRect(x, y, 1, 1);

                        renderedPoints++;
                    }
                } else {
                    if (!entry.show) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    }

                    if (settings.ageFade) {
                        if (!playerOccurrences.containsKey(entry.playerName)) {
                            playerOccurrences.put(entry.playerName, 1);
                        } else {
                            playerOccurrences.put(entry.playerName, playerOccurrences.get(entry.playerName) + 1);
                        }
                    }

                    if (at != null) at.transform(entry.position.point, pt);

                    float x = (entry.position.x + offsetX) * upscale;
                    float y = (entry.position.z + offsetY) * upscale;

                    if (settings.terminusPoints && settings._drawType == PlayerTrackerDecoder.DrawType.Line && !playerFirst.contains(entry.playerName)) {
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

                        if (settings._drawType == PlayerTrackerDecoder.DrawType.Pixel) {
                            drawRectangle(g2d, x, y, settings.size, true);
                            renderedPoints++;
                        } else if (settings._drawType == PlayerTrackerDecoder.DrawType.Dot) {
                            drawDot(g2d, x, y, settings.size, true);
                            renderedPoints++;
                        } else if (settings._drawType == PlayerTrackerDecoder.DrawType.Heat) {
                            if (settings._heatDrawType == PlayerTrackerDecoder.HeatDrawType.Color) {
                                try {
                                    if (settings.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
                                        g2d.setColor(Utils.lerpColor(Color.darkGray, Color.getHSBColor(0.93f, 0.68f, 0.55f), Math.min(1, (posActivityMap.get(entry.position) * Math.abs(settings.heatMapStrength)) / (float) (maxActivity))));
                                    } else {
                                        g2d.setColor(Utils.lerpColor(Color.lightGray, Color.getHSBColor(0.93f, 0.68f, 0.55f), Math.min(1, (posActivityMap.get(entry.position) * Math.abs(settings.heatMapStrength)) / (float) (maxActivity))));
                                    }
                                } catch (IllegalArgumentException e) {
                                    Logger.err("Something wrong happened when lerping colors for the heatmap color (Probably the stupid negative input error):\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
                                }

                                drawRectangle(g2d, x, y, settings.size, true);
                            } else {
                                g2d.setColor(playerNameColorMap.get(entry.playerName));
                                drawRectangle(g2d, x, y, settings.size * Math.min((float) posActivityMap.get(entry.position) * (settings.heatMapStrength / 50.0f) + 0.5f, maxActivity + 0.5f), true);
                            }
                            renderedPoints++;
                        }
                    }

                    if (settings._drawType == PlayerTrackerDecoder.DrawType.Line) {
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
                                    drawArrowLine(g2d, x, y, (lastPos.x + offsetX) * upscale, (lastPos.z + offsetY) * upscale, settings.size * 2, settings.size * 2);
                                } else {
                                    drawLine(g2d, x, y, (lastPos.x + offsetX) * upscale, (lastPos.z + offsetY) * upscale, settings.size);
                                }
                                renderedPoints++;
                            }
                        }

                        playerLastPosMap.put(entry.playerName, entry.position);
                    }

                    if (!entry.show) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
                    }
                }
            }
        }

        if (settings._drawType == PlayerTrackerDecoder.DrawType.Line && settings.terminusPoints) {
            for (String name : playerLastPosMap.keySet()) {
                if (playerNameEnabledMap.get(name)) {
                    Vector3 pos = playerLastPosMap.get(name);
                    if (at != null) at.transform(pos.point, pt);

                    float x = (pos.x + offsetX) * upscale;
                    float y = (pos.z + offsetY) * upscale;

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

    private void drawRectangle(Graphics2D g2d, float x1, float y1, float x2, float y2, boolean filled, boolean showBorder) {
        float startX = Math.min(x1, x2);
        float startY = Math.min(y1, y2);
        float endX = Math.max(x1, x2);
        float endY = Math.max(y1, y2);
        if (filled) {
            Rectangle2D.Float rect = new Rectangle2D.Float(startX, startY, endX - startX, endY - startY);
            g2d.fill(rect);

            if (showBorder) {
                g2d.setColor(g2d.getColor().brighter());
                g2d.draw(rect);
            }
        } else {
            g2d.draw(new Rectangle2D.Float(startX, startY, endX - startX, endY - startY));
        }
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
    // endregion

    // region Data
    public void queuePointUpdate(boolean log) {
        shouldUpdate = true;
        shouldUpdateLog = log;
    }

    private void updatePoints() {
        if (shouldUpdateLog) Logger.info("Updating points");
        boolean start = shouldDraw;
        shouldDraw = false;

        // logEntriesGroupedByTime.clear();
        playerMarkerCount.clear();
        posActivityMap.clear();
        enabledEntries.clear();
        maxActivity = 0;

        for (LogEntry entry : logEntries.values()) {
            if ((entry.time.isAfter(startTime) && entry.time.isBefore(endTime)) || entry.time.equals(startTime) || entry.time.equals(endTime)) {
                if (playerNameEnabledMap.get(entry.playerName) && entry.show) {
                    enabledEntries.add(entry);

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
        }

        shouldDraw = start;

        if (shouldUpdateLog) Logger.info("Updated points: " + logEntries.size());
    }

    public void setData(DecodedData data) {
        Logger.info("Setting data to display");

        decodedData = data;

        startTime = data.startTime;
        endTime = data.endTime;

//        PlayerCounter pc = new PlayerCounter(data);
//        // Run a python script
//        try {
//            PythonIntegration.executePython("OnlinePlayerCount.py", "data.txt", String.format("""
//                    {
//                        "daily": %s,
//                        "periods": %s,
//                    }
//                    """, pc.analyzeActivityWeek().toString(), pc.analyzePerPeriod().toString()));
//        } catch (Exception e) {
//            Logger.err("Error running python file:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
//        }

        logEntries = data.logEntries;
        logTimes = new ArrayList<>(logEntries.keySet()).toArray(new LocalDateTime[0]);
        playerNameColorMap = data.playerNameColorMap;
        playerLastPosMap = data.playerLastPosMap;
        playerNameEnabledMap = data.playerNameEnabledMap;

        assert logEntries != null;
        totalTimes = logEntries.size();

        minX = data.minX;
        minY = data.minY;
        maxX = data.maxX;
        maxY = data.maxY;
        xRange = data.xRange;
        yRange = data.yRange;

        setPreferredSize(new Dimension(xRange, yRange));

        zoomFactor = 1;
        prevZoomFactor = 1;
        curZoomFactor = 1;
        curX = 0;
        curY = 0;
        xTarget = 0;
        yTarget = 0;

        dataWorld = data.dataWorld;

        totalData = logEntries.size();

        selectedEntryLabel.setText("Nothing Selected");
        selectedEntries.clear();

        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        updatePoints();

        Logger.info("Successfully set data to display");
    }
    // endregion

    // region Input
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (selecting || dragging) return;

        zooming = true;
        if (e.getUnitsToScroll() < 0) {
            zoomFactor = Math.min(50.0f, zoomFactor * (1.05f * sensitivity));
        } else if (e.getUnitsToScroll() > 0) {
            zoomFactor = Math.max(0.02f, zoomFactor / (1.05f * sensitivity));
        }

//        double newPixelsPerFragment = zoomFactor * blocksPerFragment;
//
//        if (newPixelsPerFragment > 4096D * (double) blocksPerFragment / DEFAULT_REGION_SIZE) {
//            // restrict min zoom to 4096 chunks per fragment
//            newPixelsPerFragment = 4096D * (double) (blocksPerFragment / DEFAULT_REGION_SIZE);
//        } else if (newPixelsPerFragment < 32D * (double) blocksPerFragment / DEFAULT_REGION_SIZE) {
//            // restrict max zoom to 32 chunks per fragment
//            newPixelsPerFragment = 32D * (double) (blocksPerFragment / DEFAULT_REGION_SIZE);
//        }
//
//        pixelsPerFragment = newPixelsPerFragment;

        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            Point curPoint = e.getLocationOnScreen();
            xDiff = (curPoint.x - startPoint.x) * sensitivity;
            yDiff = (curPoint.y - startPoint.y) * sensitivity;
            dragging = true;

            repaint();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            selecting = true;
            if (inverse != null) inverse.transform(e.getPoint(), selectionEnd);
            selectionEnd.x += 1;
            selectionEnd.y += 1;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (inverse != null) inverse.transform(e.getPoint(), mousePosition);
        coordinateLabel.setText(" (" + mousePosition.x + ", " + mousePosition.y + ") | ");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            boolean shiftDown = e.isShiftDown();
            if (!shiftDown) {
                selectedEntryLabel.setText("Nothing Selected");
                selectedEntries.clear();
            }

            for (LogEntry entry : enabledEntries) {
                if ((Math.abs(entry.position.x - mousePosition.x) < 2 && Math.abs(entry.position.z - mousePosition.y) < 2) && (!shiftDown || !selectedEntries.contains(entry))) {
                    // float dist = entry.position.sqrDistTo(mousePosition);
                    //
                    // if (dist < Math.max(4.0f, settings.size * settings.size)) {
                    selectedEntries.add(entry);

                    Logger.info("Selected a log entry: " + entry);
                    break;
                    // }
                }
            }

            Logger.info("clicked");

            if (selectedEntries.size() > 1) {
                selectedEntryLabel.setText("Selected " + selectedEntries.size() + " points");
            } else if (selectedEntries.size() != 0) {
                selectedEntryLabel.setText(selectedEntries.iterator().next().toString());
            }
        } else if (SwingUtilities.isRightMouseButton(e)) {
            showRightClickMenu(e.getPoint());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            xDiff = 0;
            yDiff = 0;
            released = false;
            startPoint = MouseInfo.getPointerInfo().getLocation();
        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (inverse != null) inverse.transform(e.getPoint(), selectionStart);
            if (inverse != null) inverse.transform(e.getPoint(), selectionEnd);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            released = true;
        } else if (SwingUtilities.isRightMouseButton(e)) {
            if (selecting) {
                selecting = false;

                finishSelect(e.isShiftDown(), e.isControlDown());
            }
        }

        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private void finishSelect(boolean shiftDown, boolean ctrlDown) {
        if (!shiftDown) {
            selectedEntries.clear();
            selectedEntryLabel.setText("Nothing Selected");
        }

        int selectedCount = 0;
        if (ctrlDown) {
            for (LogEntry entry : logEntries.values()) {
                if ((shiftDown || !selectedEntries.contains(entry)) && entry.position.insideBounds(selectionStart, selectionEnd)) {
                    selectedEntries.add(entry);

                    selectedCount++;
                }
            }
        } else {
            for (LogEntry entry : enabledEntries) {
                if ((shiftDown || !selectedEntries.contains(entry)) && entry.position.insideBounds(selectionStart, selectionEnd)) {
                    selectedEntries.add(entry);

                    selectedCount++;
                }
            }
        }

        Logger.info("Selected " + selectedCount + " entries");

        selectedEntryLabel.setText("Selected " + selectedEntries.size() + " points");
    }

    private void showRightClickMenu(Point pos) {
        JPopupMenu rightClickMenu = new JPopupMenu("Right Click");
        JMenuItem hideButton = new JMenuItem("Hide Points");

        rightClickMenu.add(hideButton);
        // add selected entries to the hidden entries list
        hideButton.addActionListener(e -> {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                if (selectedEntries.size() == 0) {
                    enabledEntries.forEach(entry -> entry.show = false);
                } else {
                    selectedEntries.forEach(entry -> entry.show = false);
                }
                queuePointUpdate(true);
            });
        });

        JMenuItem showButton = new JMenuItem("Show Points");

        rightClickMenu.add(showButton);
        // remove selected entries from the hidden entries list
        showButton.addActionListener(e -> {
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                if (selectedEntries.size() == 0) {
                    enabledEntries.forEach(entry -> entry.show = true);
                } else {
                    selectedEntries.forEach(entry -> entry.show = true);
                }
                queuePointUpdate(true);
            });
        });

        rightClickMenu.show(this, pos.x, pos.y);
    }
    // endregion

    //region Seed mapping
    public void setSeedMapInfo(MCVersion version, com.seedfinding.mccore.state.Dimension dimension, int threadCount, long worldSeed) {
        shouldDraw = false;
        this.threadCount = threadCount;

        context = new MapContext(version, dimension, worldSeed);

        hasWorldMap = true;

        restart();
        shouldDraw = true;
    }

    public void resetSeedMapInfo() {
        shouldDraw = false;

        this.context = null;

        this.hasWorldMap = false;

        this.restart();
        shouldDraw = true;
    }
    //endregion

    // region World Map
    public MapContext getContext() {
        return this.context;
    }

    public void restart() {
        if (scheduler != null) scheduler.terminate();
        scheduler = new FragmentScheduler(this, this.threadCount);
        repaint();
    }

    public void drawMap(Graphics2D graphics) {
        Map<Fragment, DrawInfo> drawQueue = getDrawQueue();
        drawQueue.forEach((fragment, info) -> fragment.drawNonLoading(f -> f.drawBiomes(graphics, info)));
        drawQueue.forEach((fragment, info) -> fragment.drawNonLoading(f -> f.drawGrid(graphics, info)));
    }

    public Map<Fragment, DrawInfo> getDrawQueue() {
        Map<Fragment, DrawInfo> drawQueue = new HashMap<>();
        int w = getWidth(), h = getHeight();

        BPos min = getPos(0, 0);
        BPos max = getPos(w, h);

        int factor = 1;

        RPos regionMin = min.toRegionPos(blocksPerFragment);
        RPos regionMax = max.toRegionPos(blocksPerFragment);
        int blockOffsetX = regionMin.toBlockPos().getX() - min.getX();
        int blockOffsetZ = regionMin.toBlockPos().getZ() - min.getZ();
        double pixelOffsetX = blockOffsetX * curZoomFactor;
        double pixelOffsetZ = blockOffsetZ * curZoomFactor;
        for (int regionX = regionMin.getX() / factor; regionX <= regionMax.getX() / factor; regionX++) {
            for (int regionZ = regionMin.getZ() / factor; regionZ <= regionMax.getZ() / factor; regionZ++) {
                Fragment fragment = scheduler.getFragmentAt(regionX * factor, regionZ * factor, factor);
                double x = (regionX * factor - regionMin.getX()) * pixelsPerFragment + pixelOffsetX;
                double z = (regionZ * factor - regionMin.getZ()) * pixelsPerFragment + pixelOffsetZ;
                int size = (int) (pixelsPerFragment) * factor;
                drawQueue.put(fragment, new DrawInfo((int) x, (int) z, size, size));
            }
        }

        return drawQueue;
    }

    public BufferedImage getScreenshot() {
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.drawMap((Graphics2D) image.getGraphics());
        return image;
    }

    public Vec3i getScreenSize() {
        return new Vec3i(getWidth(), 0, getHeight());
    }

    public BPos getCenterPos() {
        Vec3i screenSize = getScreenSize();
        return getPos(screenSize.getX() / 2.0D, screenSize.getZ() / 2.0D);
    }

    public BPos getPos(double mouseX, double mouseY) {
        Vec3i screenSize = getScreenSize();
        double x = (mouseX - curX) / screenSize.getX();
        double y = (mouseY - curY) / screenSize.getZ();
        double blocksPerWidth = (screenSize.getX() / pixelsPerFragment) * (double) blocksPerFragment;
        double blocksPerHeight = (screenSize.getZ() / pixelsPerFragment) * (double) blocksPerFragment;
        x *= blocksPerWidth;
        y *= blocksPerHeight;
        int xi = (int) Math.round(x);
        int yi = (int) Math.round(y);
        return new BPos(xi, 0, yi);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainPanel)) return false;
        MainPanel MainPanel = (MainPanel) o;
        return threadCount == MainPanel.threadCount && context.equals(MainPanel.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, threadCount);
    }
    // endregion

    // region Exporting as image
    public void SaveAsImage(boolean screenshot) {
        boolean playing = isPlaying;
        isPlaying = false;
        Logger.info("Started saving current screen as an image. Currently preparing the image");
        imageExportStatus.setText("  Processing...");
        PlayerTrackerDecoder.INSTANCE.revalidate();
        PlayerTrackerDecoder.INSTANCE.repaint();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        PlayerTrackerDecoder.INSTANCE.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        exporting = true;

        System.gc();

        BufferedImage image = null;

        try {
            image = new BufferedImage((screenshot ? getWidth() : xRange) * upscale, (screenshot ? getHeight() : yRange) * upscale, BufferedImage.TYPE_INT_RGB);
            image.setAccelerationPriority(1);

            System.gc();

            Graphics2D g2d = image.createGraphics();

            drawImg(g2d, image.getWidth(), image.getHeight(), screenshot);

            g2d.dispose();
        } catch (OutOfMemoryError e) {
            Logger.err("Error preparing the image to export (Out of memory):\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }

        Logger.info("Starting to save the exported image file");

        if (!new File(PlayerTrackerDecoder.DIR_OUTPUTS).exists()) {
            boolean val = new File(PlayerTrackerDecoder.DIR_OUTPUTS).mkdir();
            Logger.warn("Outputs folder to save exported image didn't exist so it was just created with result: " + val);
        }

        String name = settings._drawType + "-" + (screenshot ? "screenshot" : "export") + "-" + dataWorld;
        File[] outFiles = new File(PlayerTrackerDecoder.DIR_OUTPUTS + File.separatorChar).listFiles();
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
                    ImageIO.write(image, "png", new File(PlayerTrackerDecoder.DIR_OUTPUTS + File.separatorChar + name));
                    Logger.info("Successfully saved current screen as an image");
                } else {
                    Logger.err("Image to save is null");
                }
            } catch (Exception e) {
                Logger.err("Error saving current screen as an image:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            }

            imageExportStatus.setText("   Done!");
        }

        Toolkit.getDefaultToolkit().beep();
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        PlayerTrackerDecoder.INSTANCE.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        exporting = false;
        isPlaying = playing;
    }

    private void drawImg(Graphics2D g2d, int width, int height, boolean screenshot) {
        int _minX = Math.abs(minX);
        int _minY = Math.abs(minY);

        g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.white : Color.darkGray);
        g2d.fillRect(0, 0, width, height);

        if (screenshot) {
            AffineTransform _at = new AffineTransform();
            _at.translate(xTarget * upscale, yTarget * upscale);
            _at.scale(zoomFactor, zoomFactor);
            _at.scale(upscale, upscale);
            g2d.transform(_at);
        }

        if (backgroundImage != null) {
            // (-6384, -5376), (8959, 2767)
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backgroundOpacity));

            // error of ~500 down and ~100 right FOR SOME STUPID REASON
            g2d.drawImage(backgroundImage, xBackgroundOffset, zBackgroundOffset, null);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
        }

        if (screenshot) {
            drawPoints(g2d, true, getSize(), 0, 0, 1);
        } else {
            drawPoints(g2d, false, getSize(), _minX, _minY, 1);

            g2d.setColor(settings.uiTheme == PlayerTrackerDecoder.UITheme.Light ? Color.black : Color.white);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24 * upscale));
            g2d.drawString(String.format("Scale: 1 pixel = %.1f block(s)", (1.0f / (float) upscale)), 24, 24 + (24 * upscale));
        }

        if (PlayerTrackerDecoder.DEBUG) {
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
    }
    // endregion

    public void Reset() {
        logEntries.clear();
        logTimes = new LocalDateTime[]{};

        playerNameColorMap.clear();
        playerLastPosMap.clear();
        enabledEntries.clear();
        playerMarkerCount.clear();
        posActivityMap.clear();

        totalData = 0;
        maxActivity = 0;
        selectedEntryLabel.setVisible(false);
        selectedEntries.clear();
    }
}

// TODO: FIX GRID BUG WITH SEED MAP