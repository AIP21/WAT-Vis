package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class BarGraph extends AbstractGraph {
    private int barWidth;

    // maybe??? wider the larger its value
    private boolean barWidthByValue;

    private float xInterval;
    private float newBarWidth;

    public BarGraph(String graphName, int barWidth) {
        super(graphName);
        this.barWidth = barWidth;
    }

    public BarGraph(String graphName) {
        this(graphName, 15);
    }

    @Override
    public void processData() {
        super.processData();

        newBarWidth = (float) barWidth / (float) data.size();

        xStartOffset = barWidth / 2;
    }

    @Override
    public void drawGraph(Graphics2D g2d) {
        float dataIndex = (float) data.values().size() / 2f;
        for (GraphData gd : data.values()) {
            xInterval = ((float) (graphArea.right - (graphArea.left + xStartOffset)) / (float) gd.valuesCount);

            g2d.setColor(gd.color);

            for (int i = 0; i < gd.valuesCount; i++) {
                float y = remapY(gd.values.get(i), gd);
                g2d.fill(new Rectangle2D.Float(remapX(i) - (newBarWidth * dataIndex), y, newBarWidth, remapY(0, gd) - y));
            }

            dataIndex--;
        }
    }

    private float remapX(float x) {
        return graphArea.left + xStartOffset + (x * xInterval);
    }

    private float remapY(float y, GraphData gd) {
        return (Utils.scale(y, lowestMin, highestMax, graphArea.bottom, graphArea.top));
    }
}
