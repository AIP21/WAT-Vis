package com.anipgames.WAT_Vis.IO;

import com.anipgames.WAT_Vis.config.Settings;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
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
        Thread loadThread = new Thread(() -> {
            try {
                Logger.info("Loading resources");
                long nowMs = System.currentTimeMillis();

                File iconsDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("icons")).getFile());

                try (Stream<Path> paths = Files.walk(iconsDir.getAbsoluteFile().toPath())) {
                    paths.filter(Files::isRegularFile).forEach(e -> {
                        File file = e.toFile();
                        if (!file.getName().toLowerCase().contains("license")) {
                            String name = file.getName().substring(0, file.getName().indexOf('.'));
                            try {
                                if (name.contains("#")) {
                                    String[] dims = name.substring(name.indexOf("#") + 1).split("x");
                                    iconsLight.put(name, new ImageIcon(ImageIO.read(file))); //Utils.resize(ImageIO.read(file), Integer.parseInt(dims[0]), Integer.parseInt(dims[1]))));
                                } else {
                                    iconsLight.put(name, new ImageIcon(ImageIO.read(file)));
                                }
                            } catch (IOException ex) {
                                Logger.err("Error reading icon file:\n " + ex.getMessage() + "\n Stacktrace:\n " + Arrays.toString(ex.getStackTrace()));
                            }
                        }
                    });
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

        loadThread.start();
    }

    public static ImageIcon getIcon(String name) {
        if (Settings.INSTANCE.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
            return iconsLight.get(name);
        } else {
            return iconsDark.get(name);
        }
    }
}