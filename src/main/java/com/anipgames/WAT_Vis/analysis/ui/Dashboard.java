package com.anipgames.WAT_Vis.analysis.ui;

import com.anipgames.WAT_Vis.python.PlayerCounter;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;
import com.anipgames.WAT_Vis.util.objects.DecodedData;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Dashboard extends JFrame implements Runnable {
    private DecodedData data;

    //region Graphs
    private final JPanel graphsPanel;
    private final ArrayList<JPanel> graphs = new ArrayList<>();
    //endregion

    //region Updating
    private boolean isRunning = true;
    //endregion

    public Dashboard(DecodedData data) {
        super("Analysis Dashboard");
        setLayout(new BorderLayout());

        this.data = data;

        JPanel panel = new JPanel(new BorderLayout());
        this.getContentPane().add(panel, BorderLayout.CENTER);

        this.setMinimumSize(new Dimension(720, 480));

        JLabel titleText = new JLabel("Analysis Dashboard");
        Font titleFont = Utils.getFont(null, Font.BOLD, 26, titleText.getFont());
        if (titleFont != null) titleText.setFont(titleFont);
        titleText.setHorizontalAlignment(0);
        titleText.setHorizontalTextPosition(0);
        panel.add(titleText, BorderLayout.NORTH);

        graphsPanel = new JPanel(new FlowLayout());
        panel.add(graphsPanel, BorderLayout.CENTER);

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
        XYChart allActivityChart = new XYChartBuilder().width(500).height(200).title("Activity (All)").xAxisTitle("Day").yAxisTitle("Players").build();

        ImmutablePair<ArrayList<String>, ArrayList<Integer>> allActivity = PlayerCounter.analyzeActivityAll(data);
        allActivityChart.addSeries("Players", allActivity.getRight());

        graphs.add(new XChartPanel<>(allActivityChart));

        XYChart weekActivityChart = new XYChartBuilder().width(500).height(200).title("Activity (Week)").xAxisTitle("Day of Week").yAxisTitle("Players").build();

        ImmutablePair<ArrayList<String>, ArrayList<Integer>> weekActivity = PlayerCounter.analyzeActivityWeek(data);
        weekActivityChart.addSeries("Players", weekActivity.getRight());

        graphs.add(new XChartPanel<>(weekActivityChart));

        XYChart dayActivityChart = new XYChartBuilder().width(500).height(200).title("Activity (Day)").xAxisTitle("Hour of Day").yAxisTitle("Players").build();

        ImmutablePair<ArrayList<String>, ArrayList<Integer>> dayActivity = PlayerCounter.analyzeActivityDay(data);
        dayActivityChart.addSeries("Players", dayActivity.getRight());

        graphs.add(new XChartPanel<>(dayActivityChart));

        XYChart dailyEntriesChart = new XYChartBuilder().width(500).height(200).title("Daily Logged Entries").xAxisTitle("Day").yAxisTitle("Logged Entries").build();

        ImmutablePair<ArrayList<String>, ArrayList<Integer>> dailyEntries = PlayerCounter.analyzeDailyEntries(data);
        dailyEntriesChart.addSeries("Entries", dailyEntries.getRight());

        graphs.add(new XChartPanel<>(dailyEntriesChart));

        for (JPanel graph : graphs) {
            graphsPanel.add(graph);
        }
    }

    private static int[] getRandomWalk(int numPoints) {
        int[] y = new int[numPoints];
        y[0] = 0;
        for (int i = 1; i < numPoints; i++) {
            y[i] = (int) (y[i - 1] + (Math.random() * 10) - 5);
        }
        return y;
    }
}