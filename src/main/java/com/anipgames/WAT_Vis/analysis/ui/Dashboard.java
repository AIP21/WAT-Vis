package com.anipgames.WAT_Vis.analysis.ui;

import com.anipgames.WAT_Vis.analysis.ui.graphs.LineGraph;
import com.anipgames.WAT_Vis.util.Utils;
import com.anipgames.WAT_Vis.util.objects.DecodedData;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Function;

public class Dashboard extends JFrame {//implements Runnable {
    private DecodedData data;

    //region Graphs
    private final JPanel graphsPanel;
    private ArrayList<JPanel> graphs = new ArrayList<>();
    //endregion

    //region Updating
    private boolean isRunning = true;
    //endregion

    public static void main(String[] args) {
        Dashboard dash = new Dashboard(null);
        dash.setVisible(true);
    }

    public Dashboard(DecodedData data) {
        super("Analysis Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(new Dimension(720, 480));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.LIGHT_GRAY);

        setLayout(new BorderLayout());

        this.data = data;

        JLabel titleText = new JLabel("Analysis Dashboard");
        Font titleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (titleFont != null) titleText.setFont(titleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        add(titleText, BorderLayout.NORTH);

        graphsPanel = new JPanel(new FlowLayout());
        graphsPanel.setBackground(Color.LIGHT_GRAY);
        add(graphsPanel, BorderLayout.CENTER);

        createGraphs();
    }

//    public void run() {
//        long lastTime = System.nanoTime();
//        double ns = 1000000000;
//        long start;
//        double delta = 0;
//
//        while (isRunning) {
//            start = System.nanoTime();
//            delta += (start - lastTime) / ns;
//            lastTime = start;
//
//            if (delta >= 1) {
//                Logger.info("Dashboard ticked (1 sec interval)");
//                float[] arr = new float[1000];
//                int e = arr.length;
//
//                delta--;
//            }
//        }
//    }

    private void createGraphs() {
        graphs = new ArrayList<>();
//        XYChart allActivityChart = new XYChartBuilder().width(500).height(200).title("Activity (All)").xAxisTitle("Day").yAxisTitle("Players").build();

//        ImmutablePair<ArrayList<String>, ArrayList<Integer>> allActivity = PlayerCounter.analyzeActivityAll(data);
//        allActivityChart.addSeries("Players", allActivity.getRight());
//
//        graphs.add(new XChartPanel<>(allActivityChart));
//
//        XYChart weekActivityChart = new XYChartBuilder().width(500).height(200).title("Activity (Week)").xAxisTitle("Day of Week").yAxisTitle("Players").build();
//
//        ImmutablePair<ArrayList<String>, ArrayList<Integer>> weekActivity = PlayerCounter.analyzeActivityWeek(data);
//        weekActivityChart.addSeries("Players", weekActivity.getRight());
//
//        graphs.add(new XChartPanel<>(weekActivityChart));
//
//        XYChart dayActivityChart = new XYChartBuilder().width(500).height(200).title("Activity (Day)").xAxisTitle("Hour of Day").yAxisTitle("Players").build();
//
//        ImmutablePair<ArrayList<String>, ArrayList<Integer>> dayActivity = PlayerCounter.analyzeActivityDay(data);
//        dayActivityChart.addSeries("Players", dayActivity.getRight());
//
//        graphs.add(new XChartPanel<>(dayActivityChart));
//
//        XYChart dailyEntriesChart = new XYChartBuilder().width(500).height(200).title("Daily Logged Entries").xAxisTitle("Day").yAxisTitle("Logged Entries").build();
//
//        ImmutablePair<ArrayList<String>, ArrayList<Integer>> dailyEntries = PlayerCounter.analyzeDailyEntries(data);
//        dailyEntriesChart.addSeries("Entries", dailyEntries.getRight());
//
//        graphs.add(new XChartPanel<>(dailyEntriesChart));


//        LineGraph testLineGraph = new LineGraph(getRandomWalk(100, 10.0F), "Test Line", "X", "Y");
        Function<Float, Float> y2xFunc = a -> (float) Math.sin(a);
        LineGraph testLineGraph = new LineGraph(getFunctionPoints(10, y2xFunc), "Test Line", "X", "Y");
        graphs.add(testLineGraph);

        for (JPanel graph : graphs) {
            graphsPanel.add(graph);
        }
    }

    private static float[] getRandomWalk(int numPoints, float scale) {
        float[] y = new float[numPoints];
        y[0] = 0;
        for (int i = 1; i < numPoints; i++) {
            y[i] = (float) (y[i - 1] + (Math.random() - 0.5) * scale);
        }
        return y;
    }

    private static float[] getFunctionPoints(int numPoints, Function<Float, Float> function) {
        float[] y = new float[numPoints];
        for (int i = 0; i < numPoints; i++) {
            y[i] = function.apply((float) i);
        }
        return y;
    }
}