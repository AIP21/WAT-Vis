package com.anipgames.WAT_Vis.analysis.ui;

import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.analysis.ui.graphs.AbstractGraph;
import com.anipgames.WAT_Vis.analysis.ui.graphs.LineGraph;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Arrays;

public class Dashboard extends JInternalFrame implements Runnable {
    //region Graphs
    private final JPanel graphsPanel;
    private final ArrayList<AbstractGraph> graphs = new ArrayList<>();
    //endregion

    //region Updating
    private boolean isRunning = true;
    //endregion

    public Dashboard() {
        this.setTitle("Analysis Dashboard");
        this.setMinimumSize(new Dimension(720, 480));

        JLabel titleText = new JLabel("Analysis Dashboard");
        Font titleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (titleFont != null) titleText.setFont(titleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        add(titleText, BorderLayout.NORTH);

        graphsPanel = new JPanel(new FlowLayout());
        add(graphsPanel, BorderLayout.CENTER);

        createGraphs();
    }

    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000;
        long start;
        double delta = 0;

        while (isRunning) {
            start = System.nanoTime();
            delta += (start - lastTime) / ns;
            lastTime = start;

            if (delta >= 1) {
                Logger.info("Dashboard ticked (1 sec interval)");


                delta--;
            }
        }
    }

    private void createGraphs() {
        graphs.add(new LineGraph("Test", "X", "Y"));

        for (AbstractGraph graph : graphs) {
            graphsPanel.add(graph);
        }
    }
}