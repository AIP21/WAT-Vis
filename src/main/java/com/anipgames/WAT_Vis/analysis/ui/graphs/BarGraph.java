package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class BarGraph extends AbstractGraph {
    private int[] colRanges;
    private int[] colCounts;

    private int barWidth;

    private boolean stackBars = true;
    private boolean roundBarTops = true;
    private int barRounding = 10;
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

        colRanges = new int[highestCount];
        colCounts = new int[highestCount];

        for (int i = 0; i < highestCount; i++) {
            for (GraphData gd : data.values()) {
                colRanges[i] += Math.abs(gd.values.get(i));
            }
        }
    }

    @Override
    public void drawGraph(Graphics2D g2d) {
        xInterval = ((float) (getWidth() + xStartOffset) / (float) highestCount);

        for (int i = 0; i < highestCount; i++) {
            float dataIndex = (float) data.values().size() / 2f;
            int prevBarSegmentVal = 0;

            for (GraphData gd : data.values()) {
                if (i < gd.valuesCount) {
                    g2d.setColor(gd.color);
                    float y = remapY(gd.values.get(i), i);

                    if (stackBars && data.size() > 1) {
                        float startY = remapY(prevBarSegmentVal, i);

                        g2d.fill(new Rectangle2D.Float(remapX(i) - (barWidth / 2f), startY, barWidth, y - startY));
                        prevBarSegmentVal += gd.values.get(i);
                    } else {
                        if (y < 5 || !roundBarTops) {
                            g2d.fill(new Rectangle2D.Float(remapX(i) - (newBarWidth * dataIndex), 0, newBarWidth, y));
                        } else { // Draw with rounded top if the bar is tall enough and if rounding is enabled
                            g2d.fill(new Rectangle2D.Float(remapX(i) - (newBarWidth * dataIndex), 0, newBarWidth, y - barRounding));
                            g2d.fill(new RoundRectangle2D.Float(remapX(i) - (newBarWidth * dataIndex), y - (barRounding * 2), newBarWidth, (barRounding * 2), barRounding, barRounding));
                        }
                        dataIndex--;
                    }
                }
            }
        }

    }

    private float remapX(float x) {
        return xStartOffset + (x * xInterval);
    }

    private float remapY(float y, int index) {
        return Utils.scale(y, lowestMin, highestMax, 0, getHeight());
    }
}
