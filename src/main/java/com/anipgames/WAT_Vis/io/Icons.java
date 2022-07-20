package com.anipgames.WAT_Vis.io;

import com.anipgames.WAT_Vis.config.Settings;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.util.Logger;
import com.anipgames.WAT_Vis.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Icons {
    private static final HashMap<String, ImageIcon> iconsLight = new HashMap<>();
    private static final HashMap<String, ImageIcon> iconsDark = new HashMap<>();

    private static final HashMap<AbstractButton, String> iconifiedButtons = new HashMap<>();

    public Icons() {
        loadIcons();
    }

    private void loadIcons() {
        Thread loadThread = new Thread(() -> {
            try {
                Logger.info("Loading icons");
                long nowMs = System.currentTimeMillis();

                HashMap<String, Boolean> inverts = new HashMap<>();

                File[] iconFiles = new File(Thread.currentThread().getContextClassLoader().getResource("icons").getPath()).listFiles();

                if (iconFiles != null) {
                    Logger.info("Loading resources");
                    for (File file : iconFiles) {
                        if (!file.getName().toLowerCase().contains("license")) {
                            String name = file.getName().substring(0, file.getName().indexOf('.'));
                            try {
                                boolean invert = name.contains("&");

                                if (name.contains("#")) {
                                    String[] dims = name.substring(name.indexOf("#") + 1).replace("&", "").split("x");
                                    name = name.substring(0, name.indexOf("#"));
                                    iconsLight.put(name, new ImageIcon(Utils.resize(ImageIO.read(file), Integer.parseInt(dims[0]), Integer.parseInt(dims[1]))));
                                } else {
                                    iconsLight.put(name, new ImageIcon(Utils.resize(ImageIO.read(file), 24, 24)));
                                }

                                inverts.put(name, invert);
                            } catch (IOException ex) {
                                Logger.error("Error reading icon file:\n " + ex.getMessage() + "\n Stacktrace:\n " + Arrays.toString(ex.getStackTrace()));
                            }
                        }
                    }

                    Logger.info(String.format("Successully loaded %d icons", iconsLight.size()));
                }

                long durMs = System.currentTimeMillis() - nowMs;

                Logger.info("Successfully loaded resources in " + durMs + "ms");

                Logger.info("Creating inverted icons for dark mode");
                nowMs = System.currentTimeMillis();

                for (String name : iconsLight.keySet()) {
                    if (inverts.get(name)) {
                        iconsDark.put(name, new ImageIcon(Utils.invertImage(iconsLight.get(name).getImage())));
                    }
                }

                durMs = System.currentTimeMillis() - nowMs;

                Logger.info("Successfully created inverted icons in " + durMs + "ms");
            } catch (Exception e) {
                Logger.error("Error loading icon resources:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
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

    public static void setIcon(AbstractButton toSet, String iconName) {
        iconifiedButtons.put(toSet, iconName);

        if (Settings.INSTANCE.uiTheme == PlayerTrackerDecoder.UITheme.Light) {
            toSet.setIcon(iconsLight.get(iconName));
        } else {
            toSet.setIcon(iconsDark.get(iconName));
        }
    }

    public static void changeIconTheme(PlayerTrackerDecoder.UITheme newTheme) {
        boolean dark = newTheme == PlayerTrackerDecoder.UITheme.Dark;

        for (AbstractButton button : iconifiedButtons.keySet()) {
            if (dark) {
                button.setIcon(iconsDark.get(iconifiedButtons.get(button)));
            } else {
                button.setIcon(iconsLight.get(iconifiedButtons.get(button)));
            }
        }
    }
}