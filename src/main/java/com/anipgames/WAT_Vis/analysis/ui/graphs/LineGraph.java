package com.anipgames.WAT_Vis.analysis.ui.graphs;

import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class LineGraph extends AbstractGraph {
    public LineGraph(ArrayList<Integer> values, String graphName, String xLabel, String yLabel) {
        super(values, graphName, xLabel, yLabel);
    }

    public LineGraph(String graphName, String xLabel, String yLabel) {
        this(null, graphName, xLabel, yLabel);
    }

    @Override
    public void processData() {

    }

    @Override
    public void drawGraph(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.red);

        final int xOffset = 20;
        final int yOffset = 20;

        final int yMax = Collections.max(values);
        final int width = getHeight() - 20;
        final int height = getHeight() - 20;

        int xLength = values.size();
        for (int i = 0; i < xLength; i++) {
            if (i != 0) {
                float x1 = xOffset + fixPos(i - 1, width, xLength);
                int y1 = yOffset + values.get(i - 1);
                float x2 = xOffset + fixPos(i, width, xLength);
                int y2 = yOffset + values.get(i);

                Logger.info(x2 + ", " + y2);

                g2d.drawLine((int) x1, y1, (int) x2, y2);
            }
        }
    }

    private float fixPos(int x, int size, int max) {
        return Utils.lerp(20f, (float) size, (float) x / (float) max);
    }
}