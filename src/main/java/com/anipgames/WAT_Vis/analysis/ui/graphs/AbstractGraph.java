package com.anipgames.WAT_Vis.analysis.ui.graphs;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public abstract class AbstractGraph extends JPanel {
    private final String graphName;
    private final String xLabel;
    private final String yLabel;

    public ArrayList<Integer> values;

    public AbstractGraph(ArrayList<Integer> values, String graphName, String xLabel, String yLabel) {
        this.values = values;
        this.graphName = graphName;
        this.xLabel = xLabel;
        this.yLabel = yLabel;

        this.setToolTipText(graphName);
        this.setBorder(BorderFactory.createTitledBorder(null, graphName));
        this.setPreferredSize(new Dimension(300, 200));
    }

    public AbstractGraph(String graphName, String xLabel, String yLabel) {
        this(null, graphName, xLabel, yLabel);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawString(xLabel, 0, getHeight() / 2);
        g.drawString(yLabel, getWidth() / 2, getHeight() - 20);

        if (values != null) {
            drawGraph(g);
        } else {
            g.drawString("No data to display", getWidth() / 2, getHeight() / 2);
        }
    }

    public abstract void processData();

    public abstract void drawGraph(Graphics g);

    public String getName() {
        return graphName;
    }

    public ArrayList<Integer> getValues() {
        return values;
    }

    public void setValues(ArrayList<Integer> values) {
        this.values = values;
    }
}