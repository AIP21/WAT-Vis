package com.anipgames.WAT_Vis.analysis.ui;

import com.anipgames.WAT_Vis.analysis.ui.graphs.AbstractGraph;
import com.anipgames.WAT_Vis.analysis.ui.graphs.BarGraph;
import com.anipgames.WAT_Vis.analysis.ui.graphs.LineGraph;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static java.awt.event.InputEvent.BUTTON1_DOWN_MASK;

public class Dashboard extends JFrame implements Runnable {
    private DecodedData data;

    //region Graphs
    private final JPanel graphsPanel;
    private ArrayList<AbstractGraph> graphs = new ArrayList<>();
    //endregion

    //region Updating
    private boolean isRunning = true;
    //endregion

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarculaLaf.setup();
            Dashboard dash = new Dashboard(null);
            dash.setVisible(true);

            (new Thread(dash)).start();
        });
    }

    public Dashboard(DecodedData data) {
        super("Analysis Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(new Dimension(720, 600));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        this.data = data;

        JLabel titleText = new JLabel("Analysis Dashboard");
        Font titleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (titleFont != null) titleText.setFont(titleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        add(titleText, BorderLayout.NORTH);

        graphsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(graphsPanel, BorderLayout.CENTER);

        createGraphs();


        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        JSpinner xg = new JSpinner();
        xg.setValue(graphs.get(1).getGridSpacing().x);
        xg.setToolTipText("x grid");
        xg.addChangeListener((e) -> {
            graphs.get(1).setGridSpacing((int) ((JSpinner) e.getSource()).getValue(), (int) graphs.get(1).getGridSpacing().y);
            graphs.get(1).processData();
            repaint();
        });
        bottomPanel.add(xg);

        JSpinner yg = new JSpinner();
        yg.setValue(graphs.get(1).getGridSpacing().y);
        yg.setToolTipText("y grid");
        yg.addChangeListener((e) -> {
            graphs.get(1).setGridSpacing((int) graphs.get(1).getGridSpacing().x, (int) ((JSpinner) e.getSource()).getValue());
            graphs.get(1).processData();
            repaint();
        });
        bottomPanel.add(yg);
    }

    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000 / 60;
        long start;
        double delta = 0;

        AbstractGraph gp4 = graphs.get(3);
        Function<Integer, Float> randWalkFunc = a -> (float) (gp4.getData().get(a - 1) + (Math.random() - 0.5f) * 10f);

        while (isRunning) {
            start = System.nanoTime();
            delta += (start - lastTime) / ns;
            lastTime = start;

            if (delta >= 1) {
                gp4.addData(Math.round(randWalkFunc.apply(gp4.getData().size())));

                delta--;
            }
        }
    }

    private void createGraphs() {
        graphs = new ArrayList<>();

        LineGraph testLineGraph = new LineGraph("Test Line");
        testLineGraph.setGridDrawPrefs(true, true);
        testLineGraph.setValueDrawPrefs(true, true);
        testLineGraph.setData(new ArrayList<>(List.of(0, 2, 2, 3, 4, 3, 2, 3, 1, 2, 1)));
        testLineGraph.setGridSpacing(1, 2);
        graphs.add(testLineGraph);

        BarGraph testBarGraph = new BarGraph("Test Bar");
        testBarGraph.setGridDrawPrefs(true, true);
        testBarGraph.setValueDrawPrefs(true, true);
        testBarGraph.setData("A", new ArrayList<>(List.of(1, 2, 2, 3, 4, 3, 2, 3, 1, 2, 6)));
        testBarGraph.setData("B", new ArrayList<>(List.of(2, 1, 1, 2, 5, 4, 1, 2, 4, 3, 5)));
        testBarGraph.setData("C", new ArrayList<>(List.of(1, 2, 2, 3, 4, 3, 2, 3, 1, 2, 6)));
        testBarGraph.setData("D", new ArrayList<>(List.of(2, 1, 1, 2, 5, 4, 1, 2, 4, 3, 5)));
        testBarGraph.setGridSpacing(1, 1);
        graphs.add(testBarGraph);

        Function<Integer, Float> sinFunc = a -> (float) Math.sin((float) a / 10f) * 10;
        Function<Integer, Float> cosFunc = a -> (float) Math.cos((float) a / 10f) * 10;
        Function<Integer, Float> sin2Func = a -> (float) Math.cos((float) a);
        Function<Integer, Float> cos2Func = a -> (float) Math.cos((float) a);
        LineGraph sinCosGraph = new LineGraph("Sine and Cosine");
        sinCosGraph.setGridSize(5, 4);
        sinCosGraph.setGridDrawPrefs(true, true);
        sinCosGraph.setValueDrawPrefs(true, true);
        sinCosGraph.setData("Sine / 10", getFunctionPointsInt(180, sinFunc));
        sinCosGraph.setData("Cosine / 10", getFunctionPointsInt(180, cosFunc));
        sinCosGraph.setData("Sine", getFunctionPointsInt(180, sin2Func));
        sinCosGraph.setData("Cosine", getFunctionPointsInt(180, cos2Func));
        sinCosGraph.setXSuffix("\u00B0");
        graphs.add(sinCosGraph);

        LineGraph randWalkGraph = new LineGraph("Random Walk");
        randWalkGraph.setGridSize(10, 5);
        randWalkGraph.setGridDrawPrefs(true, false);
        randWalkGraph.setValueDrawPrefs(false, true);
        randWalkGraph.setData(getRandomWalk(1000, 10));
        graphs.add(randWalkGraph);

        for (JPanel graph : graphs) {
            graphsPanel.add(graph);
        }
    }

    private ArrayList<Integer> getRandomWalk(int numPoints, float scale) {
        ArrayList<Integer> y = new ArrayList<>();
        y.add(0);
        for (int i = 1; i < numPoints; i++) {
            y.add((int) (y.get(i - 1) + (Math.random() - 0.5f) * scale));
        }
        return y;
    }

    private ArrayList<Float> getFunctionPoints(int numPoints, Function<Integer, Float> function) {
        ArrayList<Float> y = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            y.add(function.apply(i));
        }
        return y;
    }

    private ArrayList<Integer> getFunctionPointsInt(int numPoints, Function<Integer, Float> function) {
        ArrayList<Integer> y = new ArrayList<>();
        for (int i = 0; i < numPoints; i++) {
            y.add(Math.round(function.apply(i)));
        }
        return y;
    }
}