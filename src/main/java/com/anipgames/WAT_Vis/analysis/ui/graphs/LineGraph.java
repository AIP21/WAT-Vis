package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;
import java.awt.geom.*;

public class LineGraph extends AbstractGraph {
    private boolean drawDots = false;
    private int dotSize = 3;

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
        for (GraphData gd : data) {
            float xInterval = (float) (graphArea.right - graphArea.left) / (float) gd.valuesCount;

            int newMax = gd.max % yGridSpacing == 0 ? gd.max : (int) (gd.max + yGridSpacing);

            boolean useCurves = true;
            if (useCurves) {
                Path2D curve = new Path2D.Float();
                curve.moveTo(graphArea.left, Utils.scale(gd.values.get(0), gd.min, newMax, graphArea.bottom, graphArea.top));

                for (int i = 2; i < gd.valuesCount; i += 3) {
                    float x = graphArea.left + (i * xInterval);
                    float y = Utils.scale(gd.values.get(i), gd.min, newMax, graphArea.bottom, graphArea.top);
                    float ctrl1x = graphArea.left + ((i - 1) * xInterval);
                    float ctrl1y = Utils.scale(gd.values.get(i - 1), gd.min, newMax, graphArea.bottom, graphArea.top);
                    float ctrl2x = graphArea.left + ((i - 2) * xInterval);
                    float ctrl2y = Utils.scale(gd.values.get(i - 2), gd.min, newMax, graphArea.bottom, graphArea.top);

                    curve.curveTo(ctrl1x, ctrl1y, ctrl2x, ctrl2y, x, y);

                    // TODO: ADD KEY FOR GRAPH COLORS AND THEIR NAMES

                    if (drawDots && gd.valuesCount <= 200) {
                        g2d.setColor(gd.color.darker());
                        g2d.fillOval((int) (x - (dotSize / 2)), (int) (y - (dotSize / 2)), dotSize, dotSize);
                    }
                }
                g2d.setColor(gd.color);
                g2d.draw(curve);
            } else {
                Path2D polyline = new Path2D.Float();
                polyline.moveTo(graphArea.left, Utils.scale(gd.values.get(0), gd.min, newMax, graphArea.bottom, graphArea.top));
                for (int i = 0; i < gd.valuesCount; i++) {
                    float x = graphArea.left + (i * xInterval);
                    float y = Utils.scale(gd.values.get(i), gd.min, newMax, graphArea.bottom, graphArea.top);

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
    }
}