package src.main.mapping.minemap.map;

import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.init.Configs;
import com.seedfinding.minemap.map.fragment.Fragment;
import com.seedfinding.minemap.map.fragment.FragmentScheduler;
import com.seedfinding.minemap.util.data.DrawInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MapPanel extends JPanel {
    public final MapContext context;
    public final MapManager manager;
    public final int threadCount;
    public FragmentScheduler scheduler;

    public MapPanel(MCVersion version, Dimension dimension, long worldSeed, int threadCount) {
        this.threadCount = threadCount;
        this.setLayout(new BorderLayout());

        this.context = new MapContext(version, dimension, worldSeed);
        this.manager = new MapManager(this);

        /*
         * HIDDEN BY ME
         * if (MineMap.isDarkTheme()) {
         * this.setBackground(WorldTabs.BACKGROUND_COLOR.darker());
         * } else {
         * this.setBackground(WorldTabs.BACKGROUND_COLOR_LIGHT.darker());
         * }
         */

        this.restart();
    }

    public MapContext getContext() {
        return this.context;
    }

    public MapManager getManager() {
        return this.manager;
    }

    public void restart() {
        if (this.scheduler != null)
            this.scheduler.terminate();
        this.scheduler = new FragmentScheduler(this, this.threadCount);
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        System.out.println("Paintcomponent in mapPanel");

        long start = System.nanoTime();
        super.paintComponent(graphics);
        if (MineMap.DEBUG)
            System.out.println("Draw super " + " " + (System.nanoTime() - start));
        start = System.nanoTime();
        this.scheduler.purge();
        if (MineMap.DEBUG)
            System.out.println("Draw scheduler " + " " + (System.nanoTime() - start));
        start = System.nanoTime();
        this.drawMap(graphics);
        if (MineMap.DEBUG)
            System.out.println("Draw map " + " " + (System.nanoTime() - start));
        start = System.nanoTime();
        this.drawCrossHair(graphics);
        if (MineMap.DEBUG)
            System.out.println("Draw crosshair " + " " + (System.nanoTime() - start));
    }

    public void drawMap(Graphics graphics) {
        long start = System.nanoTime();
        Map<Fragment, DrawInfo> drawQueue = this.getDrawQueue();
        if (MineMap.DEBUG)
            System.out.println("Draw queue " + " " + (System.nanoTime() - start));

        start = System.nanoTime();
        if (Configs.USER_PROFILE.getUserSettings().doHeightmapGrayScale) {
            drawQueue.forEach((fragment, info) -> fragment.drawNonLoading(f -> f.drawHeight(graphics, info)));
        } else {
            drawQueue.forEach((fragment, info) -> fragment.drawNonLoading(f -> f.drawBiomes(graphics, info)));
        }
        if (MineMap.DEBUG)
            System.out.println("Draw Biomes/Heights " + " " + (System.nanoTime() - start));

        drawQueue.forEach((fragment, info) -> fragment.drawNonLoading(f -> f.drawGrid(graphics, info)));
    }

    public void drawCrossHair(Graphics graphics) {
        graphics.setXORMode(Color.BLACK);
        int cx = this.getWidth() / 2, cz = this.getHeight() / 2;
        graphics.fillRect(cx - 4, cz - 1, 8, 2);
        graphics.fillRect(cx - 1, cz - 4, 2, 8);
        graphics.setPaintMode();
    }

    public Map<Fragment, DrawInfo> getDrawQueue() {
        Map<Fragment, DrawInfo> drawQueue = new HashMap<>();
        int w = this.getWidth(), h = this.getHeight();

        BPos min = this.manager.getPos(0, 0);
        BPos max = this.manager.getPos(w, h);

        double scaleFactor = this.manager.pixelsPerFragment / this.manager.blocksPerFragment;
        int factor = 1;
        // if (scaleFactor<0.04){
        // factor=8;
        // }
        RPos regionMin = min.toRegionPos(this.manager.blocksPerFragment);
        RPos regionMax = max.toRegionPos(this.manager.blocksPerFragment);
        int blockOffsetX = regionMin.toBlockPos().getX() - min.getX();
        int blockOffsetZ = regionMin.toBlockPos().getZ() - min.getZ();
        double pixelOffsetX = blockOffsetX * scaleFactor;
        double pixelOffsetZ = blockOffsetZ * scaleFactor;
        for (int regionX = regionMin.getX() / factor; regionX <= regionMax.getX() / factor; regionX++) {
            for (int regionZ = regionMin.getZ() / factor; regionZ <= regionMax.getZ() / factor; regionZ++) {
                Fragment fragment = this.scheduler.getFragmentAt(regionX * factor, regionZ * factor, factor);
                double x = (regionX * factor - regionMin.getX()) * this.manager.pixelsPerFragment + pixelOffsetX;
                double z = (regionZ * factor - regionMin.getZ()) * this.manager.pixelsPerFragment + pixelOffsetZ;
                int size = (int) (this.manager.pixelsPerFragment) * factor;
                drawQueue.put(fragment, new DrawInfo((int) x, (int) z, size, size));
            }
        }

        return drawQueue;
    }

    public BufferedImage getScreenshot() {
        BufferedImage image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
        this.drawMap(image.getGraphics());
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MapPanel))
            return false;
        MapPanel mapPanel = (MapPanel) o;
        return threadCount == mapPanel.threadCount && context.equals(mapPanel.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, threadCount);
    }
}
