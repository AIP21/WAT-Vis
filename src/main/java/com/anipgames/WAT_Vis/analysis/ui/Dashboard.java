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
        double ns = 1000000000/10;
        long start;
        double delta = 0;

        while (isRunning) {
            start = System.nanoTime();
            delta += (start - lastTime) / ns;
            lastTime = start;

            if (delta >= 1) {
                AbstractGraph gp1 = graphs.get(0);

                Function<Integer, Float> sinFunc = a -> (float) Math.sin((float) a / 10f) * 10;
                gp1.addData(Math.round(sinFunc.apply(gp1.getData().size())));

                AbstractGraph gp2 = graphs.get(1);
                Function<Integer, Float> cosFunc = a -> (float) Math.cos((float) a / 10f) * 10;
                gp2.addData(Math.round(cosFunc.apply(gp2.getData().size())));

                AbstractGraph gp3 = graphs.get(2);
                Function<Integer, Float> expFunc = a -> (float) Math.pow((float) a / 3f, 2) + 1;
                gp3.addData(Math.round(expFunc.apply(gp3.getData().size())));

                AbstractGraph gp4 = graphs.get(3);
                ArrayList<Integer> data4 = gp4.getData();

                Function<Integer, Float> randWalkFunc = a -> (float) (data4.get(a - 1) + (Math.random() - 0.5f) * 10f);
                gp4.addData(Math.round(randWalkFunc.apply(gp4.getData().size())));

                delta--;
            }
        }
    }

    private void createGraphs() {
        graphs = new ArrayList<>();
        Function<Integer, Float> sinFunc = a -> (float) Math.sin((float) a / 10f) * 10;
        LineGraph sineGraph = new LineGraph("Sine", new Color(255, 211, 0));
        sineGraph.setGridSize(5, 4);
        sineGraph.setGridDrawPrefs(true, true);
        sineGraph.setValueDrawPrefs(true, true);
        sineGraph.setData(getFunctionPointsInt(180, sinFunc));
        graphs.add(sineGraph);

        Function<Integer, Float> cosFunc = a -> (float) Math.cos((float) a / 10f) * 10;
        LineGraph cosGraph = new LineGraph("Cosine", new Color(255, 211, 0));
        cosGraph.setGridSize(5, 4);
        cosGraph.setGridDrawPrefs(true, true);
        cosGraph.setValueDrawPrefs(true, true);
        cosGraph.setData(getFunctionPointsInt(53, cosFunc));
        graphs.add(cosGraph);

        Function<Integer, Float> expFunc = a -> (float) Math.pow((float) a / 3f, 2) + 1;
        LineGraph expGraph = new LineGraph("Exponential", new Color(255, 211, 0));
        expGraph.setGridSize(10, 5);
        expGraph.setGridDrawPrefs(true, false);
        expGraph.setValueDrawPrefs(true, true);
        expGraph.setData(getFunctionPointsInt(100, expFunc));
        graphs.add(expGraph);

        LineGraph randWalkGraph = new LineGraph("Random Walk", new Color(255, 211, 0));
        randWalkGraph.setGridSize(10, 5);
        randWalkGraph.setGridDrawPrefs(true, false);
        randWalkGraph.setValueDrawPrefs(true, true);
        randWalkGraph.setData(getRandomWalk(100, 10));
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