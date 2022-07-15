package com.anipgames.WAT_Vis.analysis.ui.graphs;

import java.awt.*;

public class LineGraph extends AbstractGraph {
    public LineGraph(String graphName, String xLabel, String yLabel) {
        super(graphName, xLabel, yLabel);
        this.setPreferredSize(new Dimension(100, 200));
    }

    @Override
    public void processData() {

    }

    @Override
    public void drawGraph(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int xLength = xValues.size();

    }
}