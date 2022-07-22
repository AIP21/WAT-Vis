package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public abstract class AbstractGraph extends JPanel {
    private final String graphName;
    private final String xLabel;
    private final String yLabel;

    public float[] values;
    public int valuesCount;
    public float max;
    public float min;
    public float xUnits;
    public float yUnits;

    private final boolean drawGrid;
    private final boolean drawAxes;
    private final boolean drawLabels;

    public AbstractGraph(float[] values, String graphName, String xLabel, String yLabel, boolean drawGrid, boolean drawAxes, boolean drawLabels) {
        super();

        this.setToolTipText(graphName);
        this.setBorder(BorderFactory.createTitledBorder(null, graphName));
        this.setPreferredSize(new Dimension(300, 200));

        this.values = values;

        if (values != null) {
            this.valuesCount = values.length;
            this.max = Utils.max(values);
            this.min = Utils.min(values);
            this.xUnits = (float) (300 - 40) / (float) valuesCount;
            this.yUnits = Math.abs(max - min) / 10.0F;

            this.drawGrid = drawGrid;
            this.drawAxes = drawAxes;
            this.drawLabels = drawLabels;
        } else {
            this.drawGrid = false;
            this.drawAxes = false;
            this.drawLabels = false;
        }

        this.graphName = graphName;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
    }

    public AbstractGraph(float[] values, String graphName, String xLabel, String yLabel) {
        this(values, graphName, xLabel, yLabel, true, true, true);
    }

    public AbstractGraph(String graphName, String xLabel, String yLabel) {
        this(new float[0], graphName, xLabel, yLabel);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

//        if (drawGrid) {
//            g2d.setColor(Color.BLUE);
//            for (int i = 0; i <= valuesCount; i += xUnits) {
//                g2d.drawLine(i, 0, i, valuesCount);
//            }
//
//            for (int i = (int) min; i <= (int) max; i += yUnits) {
//                g2d.drawLine((int) min, i, (int) max, i);
//            }
//        }

        if (drawAxes) {
            g2d.setColor(Color.BLACK);
            g2d.drawLine(20, getHeight() - 20, getWidth() - 20, getHeight() - 20); // Draw x-axis
            g2d.drawLine(20, getHeight() - 20, 20, 20); // Draw y-axis
        }

        if (drawLabels) {
            // Axis labels
            g2d.drawString(xLabel, getWidth() / 2, getHeight() - 7);
            g2d.drawString(yLabel, 7, getHeight() / 2);

            // Axis numbers
            g2d.drawString(String.valueOf(min), 7, 20);
            g2d.drawString(String.valueOf(max), 7, getHeight() - 25);


            g2d.drawString("0", 20, getHeight() - 7);
            g2d.drawString(String.valueOf(valuesCount), getWidth() - 25, getHeight() - 7);
        }

        if (values != null) {
            drawGraph(g2d);
        } else {
            g2d.drawString("No data to display", getWidth() / 2, getHeight() / 2);
        }
    }

    public abstract void processData();

    public abstract void drawGraph(Graphics2D g2d);

    public String getName() {
        return graphName;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values;
    }
}