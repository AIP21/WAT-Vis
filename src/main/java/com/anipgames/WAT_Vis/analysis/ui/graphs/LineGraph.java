package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;
import java.awt.geom.*;

public class LineGraph extends AbstractGraph {
    private boolean drawDots = false;
    private int dotSize = 3;

    private float xInterval;
    private int curMax;
    private int curMin;

    public LineGraph(String graphName, boolean drawDots, int dotSize) {
        super(graphName);

        this.drawDots = drawDots;
        this.dotSize = dotSize;
    }

    public LineGraph(String graphName) {
        super(graphName);
    }

    @Override
    public void processData() {
        super.processData();
    }

    @Override
    public void drawGraph(Graphics2D g2d) {
        for (GraphData gd : data.values()) {

            xInterval = (float) (graphArea.right - graphArea.left) / (float) gd.valuesCount;

            curMax = gd.max % yGridSpacing == 0 ? gd.max : (int) (gd.max + yGridSpacing);
            curMin = gd.min;

            //TODO: ADD CURVED RENDERING TO LINE GRAPH?

            Path2D polyline = new Path2D.Float();
            polyline.moveTo(graphArea.left, remapY(gd.values.get(0)));

            for (int i = 0; i < gd.valuesCount; i++) {
                float x = remapX(i);
                float y = remapY(gd.values.get(i));

                if (i != 0) {
                    polyline.lineTo(x, y);
                }

                if (drawDots && gd.valuesCount <= 200) {
                    g2d.setColor(gd.color.darker());
                    g2d.fillOval((int) (x - (dotSize / 2)), (int) (y - (dotSize / 2)), dotSize, dotSize);
                }
            }
            g2d.setColor(gd.color);
            g2d.draw(polyline);
        }
    }

    private float remapX(float x) {
        return graphArea.left + (x * xInterval);
    }

    private float remapY(float y) {
        return Utils.scale(y, curMin, curMax, graphArea.bottom, graphArea.top);
    }
}