package com.anipgames.WAT_Vis.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class VersionGetter {
    private static final Properties versionProperties = new Properties();

    public static String getVersion() {
        InputStream inputStream = VersionGetter.class.getClassLoader().getResourceAsStream("classpath:/version.properties");

        if (inputStream == null) {
            // When running unit tests, no jar is built, so we load a copy of the file that we saved during build.gradle.
            // Possibly this also is the case during debugging, therefore we save in bin/main instead of bin/test.
            try {
                inputStream = new FileInputStream("src/main/resources/version.properties");
            } catch (FileNotFoundException e) {
                Logger.err("Could not create input stream for project version:\n Message: " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            }
        }

        try {
            versionProperties.load(inputStream);
        } catch (IOException e) {
            Logger.err("Could not load classpath:/version.properties\n Message: " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
        }

        return versionProperties.getProperty("version");
    }
}