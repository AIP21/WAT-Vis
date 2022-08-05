package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.WatVis;
import com.anipgames.WAT_Vis.util.ColorPalette;
import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class AbstractGraph extends JPanel {
    //region Data Variables
    protected HashMap<String, GraphData> data = new HashMap<>();

    public int highestCount;
    public int highestMax;
    public int lowestMin;

    public float xGridSize = 1;
    public float yGridSize = 1;
    public float xGridSpacing = 1;
    public float yGridSpacing = 1;

    protected int xStartOffset = 0;
    //endregion

    //region Display Variables
    private String[] xNames;

    private Insets outerPadding = new Insets(10, 10, 10, 10);
    protected Insets graphArea = new Insets(outerPadding.top, outerPadding.left, outerPadding.bottom, outerPadding.right);
    //region Graph Settings
    private final String graphName;

    private ColorPalette colorPalette = ColorPalette.PALETTE_A;

    private String xSuffix = "";
    private String ySuffix = "";

    private boolean drawYGrid = true;
    private boolean drawXGrid = true;
    private boolean drawXValues = true;
    private boolean drawYValues = true;
    private Color backgroundColor = new Color(77, 77, 77);
    private Color textColor = new Color(229, 229, 229);
    private Color primaryXGridColor = new Color(255, 255, 255, 153);
    private Color secondaryXGridColor = new Color(255, 255, 255, 79);
    private Color primaryYGridColor = new Color(255, 255, 255, 153);
    private Color secondaryYGridColor = new Color(255, 255, 255, 79);

    private Font titleFont = new Font("Roboto", Font.BOLD, 16);
    private Font labelFont = new Font("Arial", Font.PLAIN, 12);
    private Font keyFont = new Font("Arial", Font.PLAIN, 10);

    protected Stroke normalStroke = new BasicStroke(1);
    private float normalStrokeWidth;
    protected Stroke thickerStroke = new BasicStroke(2);
    private float thickerStrokeWidth;

    //region Positions
    protected Point titlePosition = new Point(5, 0);
    protected Point graphPosition = new Point(0, 0);
    protected Point keyPosition = new Point(0, 0);
    //endregion
    //endregion
    //endregion

    //region Timings
    private long processTime;
    private long gridTime;
    private long keyTime;
    private long graphTime;
    //endregion

    public AbstractGraph(String graphName) {
        super(true);

        this.setToolTipText(graphName);
        this.setPreferredSize(new Dimension(300, 200));

        this.setOpaque(false);

        this.graphName = graphName;

        this.normalStrokeWidth = (Utils.getStrokeWidth(normalStroke) / 2f) - 0.5f;
        this.thickerStrokeWidth = (Utils.getStrokeWidth(thickerStroke) / 2f) - 0.5f;
    }

    //region Data
    public void processData() {
        long start = System.currentTimeMillis();

        highestCount = 0;
        highestMax = Integer.MIN_VALUE;
        lowestMin = Integer.MAX_VALUE;

        for (GraphData gd : data.values()) {
            if (gd.valuesCount > highestCount) {
                highestCount = gd.valuesCount;
            }

            if (gd.max > highestMax) {
                highestMax = gd.max;
            }

            if (gd.min < lowestMin) {
                lowestMin = gd.min;
            }
        }

        xGridSpacing = Math.round((double) highestCount / (double) xGridSize);
        yGridSpacing = Math.round((double) (highestMax - lowestMin) / (double) yGridSize);

        processTime = System.currentTimeMillis() - start;
    }
    //endregion

    //TODO: ADD HOVER INFO TO GRAPH DATA
    //TODO: CLEAN UP THE CODE
    //TODO: ADD TICKING FUNCTION IN ORDER TO ADD ANIMATIONS TO THE GRAPHS!

    //region Drawing
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        long start = System.currentTimeMillis();

        graphArea.set(graphPosition.y + outerPadding.top, graphPosition.x + outerPadding.left, graphPosition.y + outerPadding.bottom, graphPosition.x + outerPadding.right);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw the rounded background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Draw the title
        g2d.setColor(textColor);
        g2d.setFont(titleFont);
        FontMetrics fm = g2d.getFontMetrics();

        g2d.drawString(graphName, outerPadding.left + 5 + titlePosition.x, outerPadding.top + fm.getAscent() + titlePosition.y);

        // Offset graph area to accommodate for title (and for x-axis overflow)
        graphArea.top += fm.getHeight() + titlePosition.y + g2d.getFontMetrics(labelFont).getHeight() + 7;

        long frameTime = System.currentTimeMillis() - start;

        if (data != null && data.size() != 0) {
            // Draw key
            start = System.currentTimeMillis();

            drawKey(g2d);

            keyTime = System.currentTimeMillis() - start;

            // Draw grid
            start = System.currentTimeMillis();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            drawGrid(g2d);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gridTime = System.currentTimeMillis() - start;

            // Draw a graph for every dataset
            start = System.currentTimeMillis();

            AffineTransform atOrig = g2d.getTransform();
            AffineTransform at = new AffineTransform();
            at.translate(graphArea.left, graphArea.top);
            at.scale((float) (getWidth() - (graphArea.left + graphArea.right)) / (float) getWidth(), -((float) (getHeight() - (graphArea.top + graphArea.bottom)) / (float) getHeight()));
            at.translate(0, -getHeight());
            g2d.transform(at);
            drawGraph(g2d);
            graphTime = System.currentTimeMillis() - start;

            if (WatVis.DEBUG) {
                // Draw drawArea rectangle
                g2d.setColor(Color.white);
                g2d.drawRect(0, 0, getWidth(), getHeight());

                g2d.transform(atOrig);

                // Draw timing info
                g2d.setColor(Color.white);
                g2d.drawString(String.format("data %dms, frame %dms, key %dms, grid %dms, rend %dms", processTime, frameTime, keyTime, gridTime, graphTime), 10, 12);
            }
        } else {
            // Show a fallback message if there is no data to graph
            g2d.drawString("No data to display", getWidth() / 2, getHeight() / 2);
        }
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setFont(labelFont);

        FontMetrics fm = g2d.getFontMetrics();
        int fontHeight = fm.getHeight();
        int yLabelWidth = 0;

        if (drawYGrid || drawXValues) graphArea.bottom += fontHeight;

        // Draw the horizontal x-axis lines
        for (int y = lowestMin; y <= highestMax; y += yGridSpacing) {
            int yVal = Math.round(Utils.scale((float) y, lowestMin, highestMax, getHeight() - graphArea.bottom, graphArea.top));

            // Lines
            if (drawXGrid) {
                if (y == 0) {
                    g2d.setColor(primaryXGridColor);
                    g2d.setStroke(thickerStroke);
                    g2d.draw(new Line2D.Float(graphArea.left, yVal + thickerStrokeWidth, getWidth() - graphArea.right, yVal + thickerStrokeWidth));
                    g2d.setStroke(normalStroke);
                } else {
                    g2d.setColor(secondaryXGridColor);
                    g2d.drawLine(graphArea.left, yVal, getWidth() - graphArea.right, yVal);
                }
            }else {
                g2d.setStroke(thickerStroke);
                g2d.setColor(backgroundColor);
                g2d.drawLine(graphArea.left, yVal, getWidth() - graphArea.right, yVal);
                g2d.setStroke(normalStroke);
            }

            // TODO: MAKE IT HIGHLIGHT THE CLOSEST VALUE TO 0 INSTEAD OF ONLY 0

            // Values
            if (drawYValues) {
                g2d.setColor(textColor);
                String yLabel = String.format("%s%s", y, ySuffix);
                int width = fm.stringWidth(yLabel) + 5;
                if (yLabelWidth < width) {
                    yLabelWidth = width;
                }
                g2d.drawString(yLabel, graphArea.left, yVal - (fontHeight / 3));
            }
        }

        if (drawYValues) {
            graphArea.left += yLabelWidth;
        }

        // Draw the vertical y-axis lines
        for (int x = 0; x <= highestCount; x += xGridSpacing) {
            if (highestCount != 0 && x != highestCount) {
                int xVal = Utils.scale(x, 0, highestCount, graphArea.left + xStartOffset, getWidth() - graphArea.right);

                // Lines
                if (drawYGrid) {
                    if (x == 0) {
                        g2d.setColor(primaryYGridColor);
                        g2d.setStroke(thickerStroke);
                        g2d.draw(new Line2D.Float(xVal, graphArea.top - fontHeight - thickerStrokeWidth, xVal, getHeight() - graphArea.bottom - thickerStrokeWidth));
                        g2d.setStroke(normalStroke);
                    } else {
                        g2d.setColor(secondaryYGridColor);
                        g2d.drawLine(xVal, graphArea.top - fontHeight, xVal, getHeight() - graphArea.bottom);
                    }
                } else {
                    g2d.setStroke(thickerStroke);
                    g2d.setColor(backgroundColor);
                    g2d.drawLine(xVal, graphArea.top - fontHeight, xVal, getHeight() - graphArea.bottom);
                    g2d.setStroke(normalStroke);
                }

                // Value
                if (drawXValues) {
                    g2d.setColor(textColor);
                    String xLabel = String.format("%s%s", xNames == null ? x : xNames[x], xSuffix);
                    if (x != highestCount) {
                        g2d.drawString(xLabel, xVal - (fm.stringWidth(xLabel) / 2), getHeight() - (graphArea.bottom - fontHeight));
                    } else {
                        g2d.drawString(xLabel, xVal - (fm.stringWidth(xLabel) + 5), getHeight() - (graphArea.bottom - fontHeight));
                    }
                }
            }
        }
    }

    private void drawKey(Graphics2D g2d) {
        if (data.size() > 1) {
            g2d.setFont(keyFont);
            FontMetrics fm = g2d.getFontMetrics();
            int xPos = 0;
            float textHeight = fm.getAscent() + fm.getDescent();
            graphArea.bottom += textHeight + 5;

            int y = keyPosition.y + ((getHeight() - outerPadding.bottom) - 5);

            for (GraphData gd : data.values()) {
                int x = (keyPosition.x + (getWidth() / 2)) + xPos;

                // Draw color key
                g2d.setColor(gd.color);
                g2d.fill(new RoundRectangle2D.Float(x, y - (textHeight / 2f) - 1, textHeight, textHeight, 5, 5));

                // Draw name of the data that the color corresponds to
                g2d.setColor(textColor);
                g2d.drawString(gd.name, x + textHeight + 3, y + fm.getDescent());
                xPos += fm.stringWidth(gd.name) + textHeight + 3 + 7;
            }
        }
    }

    public abstract void drawGraph(Graphics2D g2d);
    //endregion

    //region Getters and Setters
    public String getName() {
        return graphName;
    }

    public ArrayList<Integer> getData(String dataName) {
        return data.get(dataName).values;
    }

    /**
     * Gets the data with name "0"
     * Important! Use only if you are using JUST one dataset for this graph.
     *
     * @return The data
     */
    public ArrayList<Integer> getData() {
        return data.get("0").values;
    }

    /**
     * Sets this graph's data with the given name to the given values.
     *
     * @param dataName The name of the provided data
     * @param values   The values to set
     */
    public void setData(String dataName, ArrayList<Integer> values) {
        if (!data.containsKey(dataName)) {
            data.put(dataName, new GraphData(dataName, getNextColorFromPalette(), values));
        } else {
            data.get(dataName).setData(values);
        }

        processData();

        repaint();
    }

    /**
     * Sets this graph's data to the given values. Note: This will overwrite any data previously set in this graph.
     * Sets the data with a name of "0"
     * Important! Use only if you are using JUST one dataset for this graph.
     *
     * @param values The values to set
     */
    public void setData(ArrayList<Integer> values) {
        data.clear();
        data.put("0", new GraphData("0", getNextColorFromPalette(), values));

        processData();

        repaint();
    }

    /**
     * Add a value to data in this graph. Adds the value to the data with the given name.
     * If the provided name does not exist, then a new dataset will be added to this graph with the given name.
     *
     * @param dataName The name of the data to add to
     * @param value    The value to add
     */
    public void addData(String dataName, int value) {
        if (!data.containsKey(dataName)) {
            data.put(dataName, new GraphData(dataName, getNextColorFromPalette(), value));
        } else {
            data.get(dataName).addData(value);
        }

        processData();
        repaint();
    }

    /**
     * Add a value to the first dataset in this graph.
     * Adds the value to the data with the name "0"
     * Important! Use only if you are using JUST one dataset for this graph.
     *
     * @param value The value to add
     */
    public void addData(int value) {
        if (!data.containsKey("0")) {
            data.put("0", new GraphData("0", getNextColorFromPalette(), value));
        } else {
            data.get("0").addData(value);
        }

        processData();
        repaint();
    }

    public String[] getXNames() {
        return xNames;
    }

    public void setXNames(String[] xNames) {
        this.xNames = xNames;
    }

    public void setGridDrawPrefs(boolean drawXGrid, boolean drawYGrid) {
        this.drawXGrid = drawXGrid;
        this.drawYGrid = drawYGrid;
    }

    public void setValueDrawPrefs(boolean drawXValues, boolean drawYValues) {
        this.drawXValues = drawXValues;
        this.drawYValues = drawYValues;
    }

    public String getXSuffix() {
        return xSuffix;
    }

    public void setXSuffix(String xSuffix) {
        this.xSuffix = xSuffix;
    }

    public String getYSuffix() {
        return ySuffix;
    }

    public void setYSuffix(String ySuffix) {
        this.ySuffix = ySuffix;
    }

    public Point.Float getGridSize() {
        return new Point.Float(xGridSize, yGridSize);
    }

    /**
     * Set how many ticks each axis should have.
     * Overwrites any value set by the setGridSpacing method.
     *
     * @param x The number of ticks the x-axis should have.
     * @param y The number of ticks the y-axis should have.
     */
    public void setGridSize(float x, float y) {
        xGridSize = Math.max(1, x);
        yGridSize = Math.max(1, y);
        repaint();
    }

    public Point.Float getGridSpacing() {
        return new Point.Float(xGridSpacing, yGridSpacing);
    }

    /**
     * Set the spacing between ticks (in axis values).
     * Overwrites any value set by the setGridSize method.
     *
     * @param x The spacing between x-ticks.
     * @param y The spacing between y-ticks.
     */
    public void setGridSpacing(float x, float y) {
        if (data == null || data.size() == 0) {
            System.err.println("Data is null, unable to set the grid spacing. Please only call this method AFTER setting the data!");
            return;
        }
        xGridSize = Math.round((double) highestCount / (double) Math.max(1, x));
        yGridSize = Math.round((double) (highestMax - lowestMin) / (double) Math.max(1, y));
        processData();
        repaint();
    }

    public ColorPalette getColorPalette() {
        return colorPalette;
    }

    public void setColorPalette(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
    }

    private int colorPaletteIndex;

    private Color getNextColorFromPalette() {
        int index = colorPaletteIndex + 1 >= colorPalette.getColorsCount() ? 0 : colorPaletteIndex++;

        return colorPalette.getColor(index);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public Color getPrimaryXGridColor() {
        return primaryXGridColor;
    }

    public void setPrimaryXGridColor(Color primaryXGridColor) {
        this.primaryXGridColor = primaryXGridColor;
    }

    public Color getSecondaryXGridColor() {
        return secondaryXGridColor;
    }

    public void setSecondaryXGridColor(Color secondaryXGridColor) {
        this.secondaryXGridColor = secondaryXGridColor;
    }

    public Color getPrimaryYGridColor() {
        return primaryYGridColor;
    }

    public void setPrimaryYGridColor(Color primaryYGridColor) {
        this.primaryYGridColor = primaryYGridColor;
    }

    public Color getSecondaryYGridColor() {
        return secondaryYGridColor;
    }

    public void setSecondaryYGridColor(Color secondaryYGridColor) {
        this.secondaryYGridColor = secondaryYGridColor;
    }

    public Font getTitleFont() {
        return titleFont;
    }

    public void setTitleFont(Font titleFont) {
        this.titleFont = titleFont;
    }

    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        this.labelFont = labelFont;
    }

    public Font getKeyFont() {
        return keyFont;
    }

    public void setKeyFont(Font keyFont) {
        this.keyFont = keyFont;
    }

    public Stroke getNormalStroke() {
        return normalStroke;
    }

    public void setNormalStroke(Stroke normalStroke) {
        this.normalStroke = normalStroke;
    }

    public Stroke getThickerStroke() {
        return thickerStroke;
    }

    public void setThickerStroke(Stroke thickerStroke) {
        this.thickerStroke = thickerStroke;
    }

    public Insets getOuterPadding() {
        return outerPadding;
    }

    public void setOuterPadding(Insets outerPadding) {
        this.outerPadding = outerPadding;
    }

    public Point getTitlePosition() {
        return titlePosition;
    }

    public void setTitlePosition(Point titlePosition) {
        this.titlePosition = titlePosition;
    }

    public Point getGraphPosition() {
        return graphPosition;
    }

    public void setGraphPosition(Point graphPosition) {
        this.graphPosition = graphPosition;
    }

    public Point getKeyPosition() {
        return keyPosition;
    }

    public void setKeyPosition(Point keyPosition) {
        this.keyPosition = keyPosition;
    }

    //endregion
}

class GraphData {
    public String name = "";

    public ArrayList<Integer> values;
    public int valuesCount;
    public int max;
    public int min;

    public Color color;

    public GraphData(String name, Color color, ArrayList<Integer> values) {
        this.name = name;
        this.color = color;
        setData(values);
    }

    public GraphData(String name, Color color, int value) {
        this.name = name;
        this.color = color;
        addData(value);
    }

    public void setData(ArrayList<Integer> values) {
        this.values = values;
        if (this.values != null) {
            valuesCount = values.size();
            max = Utils.max(values);
            min = Utils.min(values);
            min = Math.min(min, 0);
        } else {
            this.values = new ArrayList<>();
        }
    }

    public void addData(int value) {
        if (values != null) values.add(value);
        else values = new ArrayList<>();

        valuesCount = values.size();
        max = Utils.max(values);
        min = Utils.min(values);
        min = Math.min(min, 0);
    }
}