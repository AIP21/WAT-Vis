package com.anipgames.WAT_Vis.analysis.ui;

import com.anipgames.WAT_Vis.analysis.ui.graphs.AbstractGraph;
import com.anipgames.WAT_Vis.analysis.ui.graphs.LineGraph;
import com.anipgames.WAT_Vis.util.Utils;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;

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
    }

    public void run() {
        long lastTime = System.nanoTime();
        double ns = 1000000000 / 60;
        long start;
        double delta = 0;

        while (isRunning) {
            start = System.nanoTime();
            delta += (start - lastTime) / ns;
            lastTime = start;

            if (delta >= 1) {
                AbstractGraph gp4 = graphs.get(1);
                ArrayList<Integer> data4 = gp4.getData(0);

                Function<Integer, Float> randWalkFunc = a -> (float) (data4.get(a - 1) + (Math.random() - 0.5f) * 10f);
                gp4.addData(0, Math.round(randWalkFunc.apply(gp4.getData(0).size())));

                delta--;
            }
        }
    }

    private void createGraphs() {
        graphs = new ArrayList<>();

        Function<Integer, Float> sinFunc = a -> (float) Math.sin((float) a / 10f) * 10;
        Function<Integer, Float> cosFunc = a -> (float) Math.cos((float) a / 10f) * 10;
        Function<Integer, Float> sin2Func = a -> (float) Math.cos((float) a);
        Function<Integer, Float> cos2Func = a -> (float) Math.cos((float) a);
        LineGraph sinCosGraph = new LineGraph("Sine and Cosine");
        sinCosGraph.setGridSize(5, 4);
        sinCosGraph.setGridDrawPrefs(true, true);
        sinCosGraph.setValueDrawPrefs(true, true);
        sinCosGraph.setData(0, getFunctionPointsInt(180, sinFunc));
        sinCosGraph.setData(1, getFunctionPointsInt(180, cosFunc));
        sinCosGraph.setData(2, getFunctionPointsInt(180, sin2Func));
        sinCosGraph.setData(3, getFunctionPointsInt(180, cos2Func));
        graphs.add(sinCosGraph);

        LineGraph randWalkGraph = new LineGraph("Random Walk");
        randWalkGraph.setGridSize(10, 5);
        randWalkGraph.setGridDrawPrefs(true, false);
        randWalkGraph.setValueDrawPrefs(true, true);
        randWalkGraph.setData(0, getRandomWalk(1000, 10));
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