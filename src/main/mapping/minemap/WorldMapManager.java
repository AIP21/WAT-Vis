package src.main.mapping.minemap;

import src.main.mapping.minemap.map.MapContext;
import src.main.mapping.minemap.map.MapPanel;
import src.main.mapping.minemap.map.MapSettings;
import src.main.mapping.minemap.map.fragment.Fragment;
import src.main.mapping.minemap.util.data.DrawInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WorldMapManager {
    public MapPanel currentMapPanel;

    public WorldMapManager() {

    }

    private static void doScreenshot(long seed, MCVersion version, int numThreads) throws IOException {
        int blockX = 0;
        int blockZ = 0;
        int size = 1;

        MapSettings settings = new MapSettings(version, Dimension.OVERWORLD).refresh();
        MapContext context = new MapContext(seed, settings);
        Fragment fragment = new Fragment(blockX, blockZ, size, context);
        BufferedImage screenshotImg = getScreenShot(fragment, size, size);
        ImageIO.write(screenshotImg, "png", new File(context.worldSeed + ".png"));
        System.out.println("Done saving screenshot!");
    }

    private static BufferedImage getScreenShot(Fragment fragment, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        DrawInfo info = new DrawInfo(0, 0, width, height);
        fragment.drawBiomes(image.getGraphics(), info);
        return image;
    }

    private void initComponents() {
    }

    public void createNewMap(MCVersion version, Dimension dimension, long seed, int numThreads) {
        currentMapPanel = new MapPanel(version, dimension, seed, numThreads);
    }
}