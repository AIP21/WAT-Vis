package config.mapping;

import main.PlayerTrackerDecoder;

public class Configs {

    public static BiomeColorsConfig BIOME_COLORS;
    public static UserProfileConfig USER_PROFILE;

    public static void registerConfigs() {
        USER_PROFILE = (UserProfileConfig) new UserProfileConfig().readConfig();
        BIOME_COLORS = (BiomeColorsConfig) new BiomeColorsConfig().readConfig();
        Configs.updateConfigs();
    }

    public static void updateConfigs() {
        if (USER_PROFILE == null) USER_PROFILE = (UserProfileConfig) new UserProfileConfig().forceGenerateConfig();
        if (BIOME_COLORS == null) BIOME_COLORS = (BiomeColorsConfig) new BiomeColorsConfig().forceGenerateConfig();

        if (USER_PROFILE.getAppVersion() == null || !USER_PROFILE.getAppVersion().equals(PlayerTrackerDecoder.VERSION)) {
            USER_PROFILE.updateConfig();
            BIOME_COLORS.updateConfig();
        }
    }
}