package IO;

import config.Settings;
import main.PlayerTrackerDecoder;
import util.Logger;
import util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class Icons {
    private static final HashMap<String, ImageIcon> iconsLight = new HashMap<>();
    private static final HashMap<String, ImageIcon> iconsDark = new HashMap<>();

    public Icons() {
        loadIcons();
    }

    private void loadIcons() {
        Thread t1 = new Thread(() -> {
            ClassLoader classLoader = getClass().getClassLoader();

            try {
                Logger.info("Loading resources");
                long nowMs = System.currentTimeMillis();

                try (Stream<Path> paths = Files.walk(Path.of(Objects.requireNonNull(classLoader.getResource("icons/")).getPath()))) {
                    ArrayList<File> iconFiles = new ArrayList<>(paths.filter(Files::isRegularFile).map(Path::toFile).toList());
                    for (File file : iconFiles) {
                        if (!file.getName().toLowerCase().contains("license")) {
                            String name = file.getName().substring(0, file.getName().indexOf('.'));
                            iconsLight.put(name, new ImageIcon(ImageIO.read(file).getScaledInstance(24, 24, 4)));
                        }
                    }
                }

                long durMs = System.currentTimeMillis() - nowMs;

                Logger.info("Successfully loaded resources in " + durMs + "ms");

                Logger.info("Creating inverted icons for dark mode");
                nowMs = System.currentTimeMillis();

                for (String name : iconsLight.keySet()) {
                    iconsDark.put(name, new ImageIcon(Utils.invertImage(iconsLight.get(name).getImage())));
                }

                durMs = System.currentTimeMillis() - nowMs;

                Logger.info("Successfully created inverted icons in " + durMs + "ms");
            } catch (Exception e) {
                Logger.err("Error loading icon resources:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            }
        });
        t1.start();
    }

    public static ImageIcon getIcon(String name) {
        if (Settings.INSTANCE.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
            return iconsLight.get(name);
        } else {
            return iconsDark.get(name);
        }
    }
}