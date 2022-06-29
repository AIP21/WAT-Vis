package src.main.config.mapping;

import com.google.gson.annotations.Expose;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.minemap.MineMap;
import com.seedfinding.minemap.init.Logger;
import com.seedfinding.minemap.map.MapSettings;
import com.vdurmont.semver4j.Semver;
import src.main.PlayerTrackerDecoder;
import src.main.config.Config;
import src.main.mapping.minemap.map.MapSettings;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

import static src.main.util.Logger.LOGGER;

public class UserProfileConfig extends Config {
    public static int MAX_SIZE = 15;
    @Expose
    protected int THREAD_COUNT;
    @Expose
    protected MCVersion ASSETS_VERSION;
    @Expose
    protected String APP_VERSION;
    @Expose
    protected String OLD_APP_VERSION;
    @Expose
    protected Map<String, Boolean> DIMENSIONS = new LinkedHashMap<>();
    @Expose
    protected Map<String, MapSettings> DEFAULT_MAP_SETTINGS = new LinkedHashMap<>();

    @Override
    public String getName() {
        return "user_profile";
    }

    public String getAppVersion() {
        return APP_VERSION;
    }

    public void setAppVersion(String version) {
        this.APP_VERSION = version;
        this.flush();
    }

    public MCVersion getAssetVersion() {
        return this.ASSETS_VERSION;
    }

    public void setAssetsVersion(MCVersion version) {
        this.ASSETS_VERSION = version;
        this.flush();
    }

    public void flush() {
        try {
            this.writeConfig();
        } catch (IOException e) {
            LOGGER.severe(e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void resetConfig() {
        this.THREAD_COUNT = 1;
        this.APP_VERSION = PlayerTrackerDecoder.VERSION;
        this.ASSETS_VERSION = null; // allowed since I use null as an invalid version
    }

    @Override
    public Config readConfig() {
        UserProfileConfig config = (UserProfileConfig) super.readConfig();
        return config;
    }

    @Override
    public void maintainConfig() {
        this.THREAD_COUNT = this.THREAD_COUNT == 0 ? 1 : this.THREAD_COUNT;
        this.OLD_APP_VERSION = this.OLD_APP_VERSION == null ? this.APP_VERSION : this.OLD_APP_VERSION;

        String previousVersion = this.APP_VERSION;
        this.APP_VERSION = PlayerTrackerDecoder.VERSION;

        for (Dimension dimension : Dimension.values()) {
            String old = dimension.getName().replace("the_", "");
            if (!this.DIMENSIONS.containsKey(dimension.getName())) {
                // this is a hacky fix for the migration
                this.DIMENSIONS.put(dimension.getName(), this.DIMENSIONS.getOrDefault(old, true));
            }
            // Cleanup for old values
            if (!old.equals(dimension.getName())) {
                this.DIMENSIONS.remove(old);
                this.DEFAULT_MAP_SETTINGS.remove(old);
            }
            MapSettings settings = this.DEFAULT_MAP_SETTINGS.get(dimension.getName());
            settings.maintainConfig(dimension, this.MC_VERSION);
        }
    }
}