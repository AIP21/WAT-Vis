package IO;

import main.PlayerTrackerDecoder;
import util.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MultiIconLoader extends SwingWorker<ImageIcon[], Void> {
    private ImageIcon[] finalIcons;
    private final String[] imageNames;
    private final int width, height;
    private final String[] descriptions;

    private final ClassLoader classLoader;

    public MultiIconLoader(String[] imageNames, int width, int height, String[] descriptions) {
        this.imageNames = imageNames;
        this.width = width;
        this.height = height;
        this.descriptions = descriptions;
        this.classLoader = PlayerTrackerDecoder.INSTANCE.getClass().getClassLoader();
    }

    @Override
    protected ImageIcon[] doInBackground() throws Exception {
        ArrayList<ImageIcon> imgs = new ArrayList<>();

        final long nowMs = System.currentTimeMillis();

        for (int i = 0; i < imageNames.length; i++) {
            imgs.add(new ImageIcon(ImageIO.read(Objects.requireNonNull(classLoader.getResource("src/resources/icons/" + imageNames[i]))).getScaledInstance(width, height, 1), descriptions[i]));
        }

        final long durMs = System.currentTimeMillis() - nowMs;

        return imgs.toArray(new ImageIcon[0]);
    }

    @Override
    protected void done() {
        try {
            PlayerTrackerDecoder.INSTANCE.setIcons(get(), imageNames.length == 2);
        } catch (Exception e) {
            Logger.err(String.format("Error loading image icon: %s with stacktrace: %s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
    }
}