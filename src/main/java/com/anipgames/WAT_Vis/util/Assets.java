package com.anipgames.WAT_Vis.util;

import com.google.gson.Gson;
import com.seedfinding.mccore.util.data.Pair;
import com.anipgames.WAT_Vis.PlayerTrackerDecoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Assets {
    public final static String DIR_DL_VERSIONS = PlayerTrackerDecoder.DIR_DL + File.separatorChar + "versions";
    public final static String DIR_DL_ASSETS = PlayerTrackerDecoder.DIR_DL + File.separatorChar + "assets";

    public static void createDirs() throws IOException {
        String[] dirs = {DIR_DL_VERSIONS, DIR_DL_ASSETS};
        for (String dir : dirs) {
            Files.createDirectories(Paths.get(dir));
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Pair<Pair<String, String>, String>> shouldUpdate() {
        String data = getDataRestAPI("https://api.github.com/repos/AIP21/TrackerDecoderApp/releases/latest");
        if (data == null) {
            return null;
        }
        Map<String, Object> map = new Gson().fromJson(data, Map.class);
        if (map.containsKey("tag_name")) {
            String tagName = ((String) map.get("tag_name")).replace("v", "");
            if (!tagName.equals(PlayerTrackerDecoder.VERSION)) {
                if (map.containsKey("assets")) {
                    ArrayList<Map<String, Object>> assets = (ArrayList<Map<String, Object>>) map.get("assets");
                    HashMap<String, Pair<Pair<String, String>, String>> versionToDownload = new LinkedHashMap<>();
                    for (Map<String, Object> asset : assets) {
                        if (asset.containsKey("browser_download_url") && asset.containsKey("name") && ((String) asset.get("name")).startsWith("PlayerTrackerDecoder-")) {
                            String url = (String) asset.get("browser_download_url");
                            String filename = (String) asset.get("name");
                            String[] split = filename.split("\\.");
                            if (split.length > 1) {
                                versionToDownload.put(split[split.length - 1], new Pair<>(new Pair<>(url, filename), tagName));
                            }
                        }
                    }

                    if (versionToDownload.isEmpty() || !versionToDownload.containsKey("jar")) {
                        Logger.warn("Github release does not contain a correct release");
                    } else {
                        return versionToDownload;
                    }
                } else {
                    Logger.warn("Github release does not contain a assets key");
                }
            } else {
                Logger.info(String.format("Version match so we are not updating current :%s, github :%s", PlayerTrackerDecoder.VERSION, tagName));
            }
        } else {
            Logger.warn("Github release does not contain a tag_name key");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static String[] getCurrentBuildInfo() {
        String data = getDataRestAPI("https://api.github.com/repos/AIP21/TrackerDecoderApp/releases/tags/v" + PlayerTrackerDecoder.VERSION);
        if (data == null) {
            return null;
        }
        Map<String, Object> map = new Gson().fromJson(data, Map.class);
        if (map.containsKey("tag_name")) {
            String tagName = ((String) map.get("tag_name")).replace("v", "");
            if (tagName.equals(PlayerTrackerDecoder.VERSION)) {
                return new String[]{(String) map.get("node_id"), (String) map.get("published_at"), (String) map.get("html_url"), (String) map.get("release_notes"), (String) map.get("name")};
            }
        }
        return null;
    }

    public static String downloadLatestVersion(String url, String filename) {
        if (download(url, new File(filename), null)) {
            return filename;
        }
        Logger.warn(String.format("Failed to download jar from url %s with filename %s", url, filename));
        return null;
    }

    private static boolean download(String url, File out, String sha1) {
        Logger.info(String.format("Downloading %s for file %s", url, out.getName()));
        ReadableByteChannel rbc;
        try {
            rbc = Channels.newChannel(new URL(url).openStream());
        } catch (IOException e) {
            Logger.error(String.format("Could not open channel to url %s, Error:\n %s", url, e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(out);
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        } catch (IOException e) {
            Logger.error(String.format("Could not download from channel to url %s for file %s, Error:\n %s", url, out.getAbsolutePath(), e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return false;
        }
        return sha1 == null || compareSha1(out, sha1);
    }

    private static boolean compareSha1(File file, String sha1) {
        if (sha1 != null && file != null) {
            try {
                return getFileChecksum(MessageDigest.getInstance("SHA-1"), file).equals(sha1);
            } catch (NoSuchAlgorithmException e) {
                Logger.error("Could not compute sha1 since algorithm does not exists. Error:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            }
        }
        return false;
    }

    private static String getFileChecksum(MessageDigest digest, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);

            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
        } catch (IOException e) {
            Logger.error(String.format("Failed to read file for checksum, Error:\n %s", e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return "";
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private static String getDataRestAPI(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                Logger.error(String.format("Failed to fetch URL %s. Errorcode : %s", apiUrl, responseCode));
            } else {

                StringBuilder inline = new StringBuilder();
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNext()) {
                    inline.append(scanner.nextLine());
                }
                scanner.close();
                return inline.toString();
            }
        } catch (Exception e) {
            Logger.error(String.format("Failed to fetch URL %s. Error:\n %s", apiUrl, e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
        }
        return null;
    }
}