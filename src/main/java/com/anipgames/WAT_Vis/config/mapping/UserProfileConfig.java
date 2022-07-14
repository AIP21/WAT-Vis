package com.anipgames.WAT_Vis.config.mapping;

import com.google.gson.annotations.Expose;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.version.MCVersion;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder;
import com.anipgames.WAT_Vis.config.Config;
import com.anipgames.WAT_Vis.mapping.minemap.map.MapSettings;
import com.anipgames.WAT_Vis.util.Logger;

import java.io.IOException;
import java.util.*;

public class UserProfileConfig extends Config {
    @Expose
    protected int THREAD_COUNT;
    @Expose
    protected MCVersion MC_VERSION;
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

    public int getThreadCount(int cores) {
        if (this.THREAD_COUNT < 1) return 1;
        return Math.min(this.THREAD_COUNT, cores);
    }

    public int getThreadCount() {
        if (this.THREAD_COUNT < 1) return 1;
        return Math.min(this.THREAD_COUNT, Runtime.getRuntime().availableProcessors());
    }

    public String getAppVersion() {
        return APP_VERSION;
    }

    public void setAppVersion(String version) {
        this.APP_VERSION = version;
        this.flush();
    }

    public MCVersion getVersion() {
        return this.MC_VERSION;
    }

    public void setVersion(MCVersion version) {
        this.MC_VERSION = version;
        this.flush();
    }

    public MCVersion getAssetVersion() {
        return this.ASSETS_VERSION;
    }

    public MapSettings getMapSettingsCopy(MCVersion version, Dimension dimension) {
        return this.DEFAULT_MAP_SETTINGS.get(dimension.getName()).copyFor(version, dimension);
    }

    public Map<String, MapSettings> getDefaultMapSettings() {
        return DEFAULT_MAP_SETTINGS;
    }

    public void setAssetsVersion(MCVersion version) {
        this.ASSETS_VERSION = version;
        this.flush();
    }

    public void setDefaultSettings(Dimension dimension, MapSettings settings) {
        this.DEFAULT_MAP_SETTINGS.put(dimension.getName(), settings.copy());
        this.flush();
    }

    public void setDimensionState(Dimension dimension, boolean state) {
        this.DIMENSIONS.put(dimension.getName(), state);
        this.flush();
    }

    public void flush() {
        try {
            this.writeConfig();
        } catch (IOException e) {
            Logger.error("Error flushing user config:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    protected void resetConfig() {
        this.THREAD_COUNT = 1;
        this.MC_VERSION = MCVersion.values()[0];
        this.APP_VERSION = PlayerTrackerDecoder.VERSION;
        this.ASSETS_VERSION = null; // allowed since I use null as an invalid version

        for (Dimension dimension : Dimension.values()) {
            this.DIMENSIONS.put(dimension.getName(), true);
            MapSettings settings = new MapSettings(dimension).refresh();
            this.DEFAULT_MAP_SETTINGS.put(dimension.getName(), settings);
        }
    }

    @Override
    public Config readConfig() {
        return (UserProfileConfig) super.readConfig();
    }

    @Override
    public void maintainConfig() {
        this.THREAD_COUNT = this.THREAD_COUNT == 0 ? 1 : this.THREAD_COUNT;
        this.MC_VERSION = this.MC_VERSION == null ? MCVersion.values()[0] : this.MC_VERSION;
        this.OLD_APP_VERSION = this.OLD_APP_VERSION == null ? this.APP_VERSION : this.OLD_APP_VERSION;
        String previousVersion = this.APP_VERSION;
        this.APP_VERSION = PlayerTrackerDecoder.VERSION;

        //this.ASSET_VERSION=this.ASSET_VERSION; // allowed since I use null as an invalid version
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