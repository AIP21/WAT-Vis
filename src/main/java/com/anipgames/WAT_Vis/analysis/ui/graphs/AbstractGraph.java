package com.anipgames.WAT_Vis.analysis.ui.graphs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class AbstractGraph extends JPanel {
    private final String graphName;
    private final String xLabel;
    private final String yLabel;

    public ArrayList<Integer> xValues = new ArrayList<>();
    public ArrayList<Integer> yValues = new ArrayList<>();

    public AbstractGraph(String graphName, String xLabel, String yLabel) {
        this.graphName = graphName;
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        this.setToolTipText(graphName);
        this.setBorder(BorderFactory.createTitledBorder(null, graphName));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawString(xLabel, 0, getHeight() / 2);
        g.drawString(xLabel, getWidth() / 2, getHeight() - 20);

        drawGraph(g);
    }

    public abstract void processData();

    public abstract void drawGraph(Graphics g);

    public String getName() {
        return graphName;
    }
}