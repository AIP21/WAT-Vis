package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;
import java.awt.geom.Line2D;

public class LineGraph extends AbstractGraph {
    public LineGraph(float[] values, String graphName, String xLabel, String yLabel, boolean drawGrid, boolean drawAxes, boolean drawLabels) {
        super(values, graphName, xLabel, yLabel, drawGrid, drawAxes, drawLabels);
    }

    public LineGraph(float[] values, String graphName, String xLabel, String yLabel) {
        super(values, graphName, xLabel, yLabel);
    }

    public LineGraph(String graphName, String xLabel, String yLabel) {
        super(graphName, xLabel, yLabel);
    }

    @Override
    public void processData() {

    }

    @Override
    public void drawGraph(Graphics2D g2d) {
//        g2d.setColor(Color.red);
//
//        final int xOffset = 20;
//        final int yOffset = 20;
//
//        final float yMax = Utils.max(values);
//        final int width = getHeight() - 20;
//        final int height = getHeight() - 20;

//        int xLength = values.length;
//        for (int i = 0; i < xLength; i++) {
//            if (i != 0) {
//                float x1 = xOffset + fixPos(i - 1, width, xLength);
//                float y1 = yOffset + values[i - 1];
//                float x2 = xOffset + fixPos(i, width, xLength);
//                float y2 = yOffset + values[i];
//
//                Logger.info(x2 + ", " + y2);
//
//                g2d.draw(new Line2D.Float(x1, y1, x2, y2));
//            }
//        }

        g2d.setColor(Color.RED);
        float prevX = 20;
        float prevY = 20;
        for (int i = 0; i < valuesCount; i++) {
            if (i != 0) {
                g2d.draw(new Line2D.Float(prevX, prevY, prevX += xUnits, prevY = Utils.scale((max + 20) - (values[i] * yUnits), 0, getHeight(), 20, getHeight() - 20)));
            }
        }

        g2d.setColor(Color.BLUE);
        prevX = 20;
        prevY = values[0];
        for (int i = 0; i < valuesCount; i++) {
            if (i != 0) {
                g2d.draw(new Line2D.Float(prevX, prevY, prevX+=xUnits, prevY = Utils.scale(values[i], min, max, 20, getHeight()-20)));
            }
        }

        g2d.setColor(Color.GREEN);
        prevX = 20;
        prevY = values[0];
        for (int i = 0; i < valuesCount; i++) {
            if (i != 0) {
                g2d.draw(new Line2D.Float(prevX, prevY, prevX += xUnits, prevY = max - values[i]));
            }
        }
    }
}