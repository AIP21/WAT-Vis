package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.WatVis;
import com.anipgames.WAT_Vis.util.ColorPalette;
import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class AbstractGraph extends JPanel {
    //region Data Variables
    protected ArrayList<GraphData> data = new ArrayList<>();

    public int highestCount;
    public int highestMax;
    public int lowestMin;

    public int xGridSize = 1;
    public int yGridSize = 1;
    public float xGridSpacing = 1;
    public float yGridSpacing = 1;
    //endregion

    //region Display Variables
    private final String graphName;

    private ColorPalette colorPalette = ColorPalette.PALETTE_A;

    private String[] xNames;

    private String xSuffix = "";
    private String ySuffix = "";

    protected final Insets outerPadding = new Insets(10, 10, 10, 10);
    protected Insets graphArea = new Insets(outerPadding.top, outerPadding.left, outerPadding.bottom, outerPadding.right);

    private boolean drawYGrid = true;
    private boolean drawXGrid = true;
    private boolean drawXValues = true;
    private boolean drawYValues = true;

    private Color backgroundColor = new Color(77, 77, 77);
    private Color textColor = new Color(187, 187, 187);
    private Color xGridColor = new Color(200, 200, 200, 131);
    private Color yGridColor = new Color(200, 200, 200, 200);

    private final Font titleFont = new Font("Arial", Font.BOLD, 16);
    private final Font labelFont = new Font("Arial", Font.PLAIN, 12);

    private Stroke normalStroke = new BasicStroke(1);
    private Stroke thickerStroke = new BasicStroke(2);
    //endregion

    //region Timings
    private long processTime;
    private long frameTime;
    private long gridTime;
    private long graphTime;
    //endregion

    public AbstractGraph(String graphName) {
        super(true);

        this.setToolTipText(graphName);
        this.setPreferredSize(new Dimension(300, 200));

        this.setOpaque(false);

        this.graphName = graphName;
    }

    public void processData() {
        long start = System.currentTimeMillis();

        highestCount = 0;
        highestMax = Integer.MIN_VALUE;
        lowestMin = Integer.MAX_VALUE;

        for (GraphData gd : data) {
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

        xGridSpacing = (float) Math.round((float) highestCount / (float) xGridSize);
        yGridSpacing = (float) Math.round((float) (highestMax - lowestMin) / (float) yGridSize);

        processTime = System.currentTimeMillis() - start;
    }

    //region Drawing
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        long start = System.currentTimeMillis();

        graphArea.set(outerPadding.top, outerPadding.left, getHeight() - outerPadding.bottom, getWidth() - outerPadding.right);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw the rounded background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Draw the title
        g2d.setColor(textColor);
        g2d.setFont(titleFont);
        int titleHeight = g2d.getFontMetrics().getHeight();
        g2d.drawString(graphName, graphArea.left + 5, titleHeight + outerPadding.top);
        graphArea.top = titleHeight + g2d.getFontMetrics(labelFont).getHeight() + outerPadding.top + 5; // Using a fixed value (10) instead of doubling outerPadding.top because if you ever increase the outerPadding.top then it will pad the title A LOT

        frameTime = System.currentTimeMillis() - start;

        if (data != null) {
            start = System.currentTimeMillis();

            drawGrid(g2d);

            gridTime = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();

            drawGraph(g2d);

            graphTime = System.currentTimeMillis() - start;
        } else {
            g2d.drawString("No data to display", getWidth() / 2, getHeight() / 2);
        }

        if(WatVis.DEBUG){
            g2d.setColor(Color.white);
            g2d.drawString(String.format("data %dms, frame %dms, grid %dms, rend %dms", processTime, frameTime, gridTime, graphTime), 10, 12);
        }
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setFont(labelFont);

        FontMetrics fm = g2d.getFontMetrics();
        int fontHeight = fm.getHeight();
        int yLabelWidth = 0;

        if (drawYGrid && drawXValues)
            graphArea.bottom -= fontHeight;

        // Draw the horizontal x-axis lines
        if (drawXGrid) {
            for (int i = lowestMin; i <= highestMax + yGridSpacing; i += yGridSpacing) {
                g2d.setColor(xGridColor);

                int yVal = (int) Utils.scale(i, lowestMin, highestMax + yGridSpacing, graphArea.bottom, graphArea.top);

                if (i == 0) {
                    g2d.setStroke(thickerStroke);
                }

                g2d.drawLine(graphArea.left, yVal, graphArea.right, yVal);
                g2d.setStroke(normalStroke);

                // Values
                if (drawYValues) {
                    g2d.setColor(textColor);
                    String yLabel = String.format("%s%s", i, ySuffix);
                    int width = fm.stringWidth(yLabel) + 5;
                    if (yLabelWidth < width) {
                        yLabelWidth = width;
                    }
                    g2d.drawString(yLabel, graphArea.left, yVal - (fontHeight / 3));
                }
            }
        }

        graphArea.left += yLabelWidth;

        // Draw the vertical y-axis lines
        if (drawYGrid) {
            for (int i = 0; i <= highestCount; i += xGridSpacing) {
                g2d.setColor(yGridColor);

                if (highestCount != 0) {
                    int xVal = Utils.scale(i, 0, highestCount, graphArea.left, graphArea.right);
                    if (i != highestCount)
                        g2d.drawLine(xVal, graphArea.top - fontHeight, xVal, graphArea.bottom);

                    // Values
                    if (drawXValues) {
                        g2d.setColor(textColor);
                        String xLabel = String.format("%s%s", xNames == null ? i : xNames[i], ySuffix);
                        if (i != highestCount) {
                            g2d.drawString(xLabel, xVal - (fm.stringWidth(xLabel) / 2), graphArea.bottom + fontHeight);
                        } else {
                            g2d.drawString(xLabel, xVal - (fm.stringWidth(xLabel) + 5), graphArea.bottom + fontHeight);
                        }
                    }
                }
            }
        }
    }

    public abstract void drawGraph(Graphics2D g2d);
    //endregion

    //region Getters and Setters
    public String getName() {
        return graphName;
    }

    public ArrayList<Integer> getData(int index) {
        return data.get(index).values;
    }

    public void setData(int index, ArrayList<Integer> values) {
        if (data.size() <= index) {
            data.add(new GraphData(values, getNextColorFromPalette()));
        } else {
            data.get(index).setData(values);
        }
        processData();

        repaint();
    }

    public void addData(int index, int value) {
        if (data.size() <= index) {
            data.add(new GraphData(value, getNextColorFromPalette()));
        } else {
            data.get(index).addData(value);
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

    public int getXGridSize() {
        return xGridSize;
    }

    public void setXGridSize(int xGridSize) {
        this.xGridSize = xGridSize;
    }

    public int getYGridSize() {
        return yGridSize;
    }

    public void setYGridSize(int yGridSize) {
        this.yGridSize = yGridSize;
    }

    /**
     * Set how many ticks each axis should have.
     *
     * @param x The number of ticks the x-axis should have.
     * @param y The number of ticks the y-axis should have.
     */
    public void setGridSize(int x, int y) {
        this.xGridSize = x;
        this.yGridSize = y;
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
    //endregion
}

class GraphData {
    public ArrayList<Integer> values;
    public int valuesCount;
    public int max;
    public int min;

    public Color color;

    public GraphData(ArrayList<Integer> values, Color color) {
        setData(values);
        this.color = color;
    }

    public GraphData(int value, Color color) {
        addData(value);
        this.color = color;
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
        if (values != null)
            values.add(value);
        else
            values = new ArrayList<>();

        valuesCount = values.size();
        max = Utils.max(values);
        min = Utils.min(values);
        min = Math.min(min, 0);
    }
}