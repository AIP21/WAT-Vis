package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class AbstractGraph extends JPanel {
    //region Data Variables
    protected ArrayList<Integer> values;

    public int valuesCount;
    public int max;
    public int min;

    public int xGridSize = 1;
    public int yGridSize = 1;
    public float xGridSpacing = 1;
    public float yGridSpacing = 1;
    //endregion

    //region Display Variables
    private final String graphName;

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

    public AbstractGraph(String graphName) {
        super(true);

        this.setToolTipText(graphName);
        this.setPreferredSize(new Dimension(300, 200));

        this.setOpaque(false);

        this.graphName = graphName;
    }

    public abstract void processData();

    //region Drawing
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        graphArea.set(outerPadding.top, outerPadding.left, getHeight() - outerPadding.bottom, getWidth() - outerPadding.right);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the rounded background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Draw the title
        g2d.setColor(textColor);
        g2d.setFont(titleFont);
        int titleHeight = g2d.getFontMetrics().getHeight();
        g2d.drawString(graphName, graphArea.left + 5, titleHeight + outerPadding.top);
        graphArea.top = titleHeight + g2d.getFontMetrics(labelFont).getHeight() + outerPadding.top + 5; // Using a fixed value (10) instead of doubling outerPadding.top because if you ever increase the outerPadding.top then it will pad the title A LOT

        if (values != null) {
            drawGrid(g2d);

            drawGraph(g2d);
        } else {
            g2d.drawString("No data to display", getWidth() / 2, getHeight() / 2);
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
            for (int i = min; i <= max + yGridSpacing; i += yGridSpacing) {
                g2d.setColor(xGridColor);

                int yVal = (int) Utils.scale(i, min, max + yGridSpacing, graphArea.bottom, graphArea.top);

                if (i == 0) {
                    g2d.setStroke(thickerStroke);
                }

                FIX FLICKERING THING WHEN ADDING DATA, IT IS WITH THE LAST VERTICAL BAR AND WITH THE MODULO MOVING THE GRAPHED LINE UP AND DOWN (JUST CHOOSE ONE WAY FOR IT TO WORK AND REMOVE THE ALTERNATION)
                ADD MULTIPLE LINES TO A SINGLE GRAPH

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
            for (int i = 0; i <= valuesCount; i += xGridSpacing) {
                g2d.setColor(yGridColor);

                int xVal = Utils.scale(i, 0, valuesCount, graphArea.left, graphArea.right);
                if (i != valuesCount)
                    g2d.drawLine(xVal, graphArea.top - fontHeight, xVal, graphArea.bottom);

                // Values
                if (drawXValues) {
                    g2d.setColor(textColor);
                    String xLabel = String.format("%s%s", xNames == null ? i : xNames[i], ySuffix);
                    if(i!=valuesCount){
                        g2d.drawString(xLabel, xVal - (fm.stringWidth(xLabel) / 2), graphArea.bottom + fontHeight);
                    }else{
                        g2d.drawString(xLabel, xVal - (fm.stringWidth(xLabel) + 5), graphArea.bottom + fontHeight);
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

    public ArrayList<Integer> getData() {
        return values;
    }

    public void setData(ArrayList<Integer> values) {
        this.values = values;
        if (values != null) {
            this.valuesCount = values.size();
            this.max = Utils.max(values);
            this.min = Utils.min(values);
            this.min = Math.min(this.min, 0);
            this.xGridSpacing = (float) Math.round((float) valuesCount / (float) xGridSize);
            this.yGridSpacing = (float) Math.round((float) (max - min) / (float) yGridSize);
        }

        repaint();
    }

    public void addData(int value) {
        if (values != null)
            this.values.add(value);
        else
            this.values = new ArrayList<>();

        this.valuesCount = values.size();
        this.max = Utils.max(values);
        this.min = Utils.min(values);
        this.min = Math.min(this.min, 0);
        this.xGridSpacing = (float) Math.round((float) valuesCount / (float) xGridSize);
        this.yGridSpacing = (float) Math.round((float) (max - min) / (float) yGridSize);

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
    //endregion
}