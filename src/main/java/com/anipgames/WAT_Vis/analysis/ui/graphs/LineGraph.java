package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;
import java.awt.geom.*;

public class LineGraph extends AbstractGraph {
    private Color lineColor = new Color(255, 211, 0); // new Color(44, 102, 230)
    private Color dotColor = new Color(255, 211, 0); // new Color(44, 102, 230)
    private boolean drawDots = false;
    private final int dotSize = 3;

    public LineGraph(String graphName, Color lineColor) {
        super(graphName);

        this.lineColor = lineColor;
    }

    public LineGraph(String graphName) {
        super(graphName);
    }

    @Override
    public void processData() {

    }

    @Override
    public void drawGraph(Graphics2D g2d) {
        float xInterval = (float) (graphArea.right - graphArea.left) / (float) valuesCount;

        int newMax = max % yGridSpacing == 0 ? max : (int) (max + yGridSpacing);

        Path2D polyline = new Path2D.Float();
        polyline.moveTo(graphArea.left, Utils.scale(values.get(0), min, newMax, graphArea.bottom, graphArea.top));
        for (int i = 0; i < valuesCount; i++) {
            float x = graphArea.left + (i * xInterval);
            float y = Utils.scale(values.get(i), min, newMax, graphArea.bottom, graphArea.top);

            if (i != 0) {
                polyline.lineTo(x, y);
            }

            if (drawDots && valuesCount <= 200) {
                g2d.setColor(dotColor);
                g2d.fillOval((int) (x - (dotSize / 2)), (int) (y - (dotSize / 2)), dotSize, dotSize);
            }
        }
        g2d.setColor(lineColor);
        g2d.draw(polyline);
    }
}