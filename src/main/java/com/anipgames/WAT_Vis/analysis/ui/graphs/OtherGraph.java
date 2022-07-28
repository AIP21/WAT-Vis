package com.anipgames.WAT_Vis.analysis.ui.graphs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rodrigo, Maritaria
 */
public final class OtherGraph extends JPanel {
    private final static int padding = 25;
    private final static int labelPadding = 25;
    private final static int pointWidth = 4;
    private final static int numberYDivisions = 10;
    private final static Color lineColor = new Color(44, 102, 230, 180);
    private final static Color pointColor = new Color(100, 100, 100, 180);
    private final static Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke graphStroke = new BasicStroke(2f);
    private List<Integer> values = new ArrayList<>(10);

    public OtherGraph() {
        setPreferredSize(new Dimension(padding * 2 + 300, padding * 2 + 200));
    }

    public void setValues(Collection<Integer> newValues) {
        values.clear();
        addValues(newValues);
    }

    public void addValues(Collection<Integer> newValues) {
        values.addAll(newValues);
        updateUI();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!(graphics instanceof Graphics2D)) {
            graphics.drawString("Graphics is not Graphics2D, unable to render", 0, 0);
            return;
        }

        final Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final var length = values.size();
        final var width = getWidth();
        final var height = getHeight();
        final var maxScore = getMaxScore();
        final var minScore = getMinScore();
        final var scoreRange = maxScore - minScore;

        // draw white background
        g.setColor(Color.WHITE);
        g.fillRect(
                padding + labelPadding,
                padding,
                width - (2 * padding) - labelPadding,
                height - 2 * padding - labelPadding);
        g.setColor(Color.BLACK);

        final FontMetrics fontMetrics = g.getFontMetrics();
        final int fontHeight = fontMetrics.getHeight();

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            final int x1 = padding + labelPadding;
            final int x2 = pointWidth + padding + labelPadding;
            final int y = height - ((i * (height - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            if (length > 0) {
                g.setColor(gridColor);
                g.drawLine(padding + labelPadding + 1 + pointWidth, y, width - padding, y);
                g.setColor(Color.BLACK);
                final int tickValue = (int) (minScore + ((scoreRange * i) / numberYDivisions));
                final String yLabel = tickValue + "";
                final int labelWidth = fontMetrics.stringWidth(yLabel);
                g.drawString(yLabel, x1 - labelWidth - 5, y + (fontHeight / 2) - 3);
            }
            g.drawLine(x1, y, x2, y);
        }

        // and for x axis
        if (length > 1) {
            for (int i = 0; i < length; i++) {
                final int x = i * (width - padding * 2 - labelPadding) / (length - 1) + padding + labelPadding;
                final int y1 = height - padding - labelPadding;
                final int y2 = y1 - pointWidth;
                if ((i % ((int) ((length / 20.0)) + 1)) == 0) {
                    g.setColor(gridColor);
                    g.drawLine(x, height - padding - labelPadding - 1 - pointWidth, x, padding);
                    g.setColor(Color.BLACK);
                    final String xLabel = i + "";
                    final int labelWidth = fontMetrics.stringWidth(xLabel);
                    g.drawString(xLabel, x - labelWidth / 2, y1 + fontHeight + 3);
                }
                g.drawLine(x, y1, x, y2);
            }
        }

        // create x and y axes
        g.drawLine(padding + labelPadding, height - padding - labelPadding, padding + labelPadding, padding);
        g.drawLine(
                padding + labelPadding,
                height - padding - labelPadding,
                width - padding,
                height - padding - labelPadding);

        final Stroke oldStroke = g.getStroke();
        g.setColor(lineColor);
        g.setStroke(graphStroke);

        final double xScale = ((double) width - (2 * padding) - labelPadding) / (length - 1);
        final double yScale = ((double) height - 2 * padding - labelPadding) / scoreRange;

        final List<Point> graphPoints = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            final int x1 = (int) (i * xScale + padding + labelPadding);
            final int y1 = (int) ((maxScore - values.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        for (int i = 0; i < graphPoints.size() - 1; i++) {
            final int x1 = graphPoints.get(i).x;
            final int y1 = graphPoints.get(i).y;
            final int x2 = graphPoints.get(i + 1).x;
            final int y2 = graphPoints.get(i + 1).y;
            g.drawLine(x1, y1, x2, y2);
        }

        boolean drawDots = width > (length * pointWidth);
        if (drawDots) {
            g.setStroke(oldStroke);
            g.setColor(pointColor);
            for (Point graphPoint : graphPoints) {
                final int x = graphPoint.x - pointWidth / 2;
                final int y = graphPoint.y - pointWidth / 2;
                g.fillOval(x, y, pointWidth, pointWidth);
            }
        }
    }

    private double getMinScore() {
        return values.stream().min(Integer::compareTo).orElse(0);
    }

    private double getMaxScore() {
        return values.stream().max(Integer::compareTo).orElse(0);
    }
}