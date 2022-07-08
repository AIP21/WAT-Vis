package src.main.mapping.minemap.util.data;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.data.StringUnhasher.Config;
import com.seedfinding.mccore.version.MCVersion;
import src.main.PlayerTrackerDecoder;
import src.main.util.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Assets {
    public final static String DIR_DL_VERSIONS = PlayerTrackerDecoder.DIR_DL + File.separatorChar + "versions";
    public final static String DIR_DL_ASSETS = PlayerTrackerDecoder.DIR_DL + File.separatorChar + "assets";
    private static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final File MANIFEST_FILE = new File(PlayerTrackerDecoder.DIR_DL + File.separator + "version_manifest.json");

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

    /**
     * Get the latest version from the global manifest file
     *
     * @return the latest release version or null if the manifest does not exists or is incorrectly formed
     */
    @SuppressWarnings("unchecked")
    public static MCVersion getLatestVersion() {
        JsonReader jsonReader = openJSON(MANIFEST_FILE);
        if (jsonReader == null) {
            return null;
        }
        Map<String, Object> map = new Gson().fromJson(jsonReader, Map.class);
        if (map.containsKey("latest")) {
            Map<String, String> latest = (Map<String, String>) map.get("latest");
            if (latest.containsKey("release")) {
                String version = latest.get("release");
                return MCVersion.fromString(version);
            }
            Logger.warn("Manifest does not contain a release key");
        } else {
            Logger.warn("Manifest does not contain a latest key");
        }
        return null;
    }

    /**
     * Download a specific version manifest, will return false if the global manifest does not exists
     *
     * @param version a specific version (must not be null)
     * @param force   if force is true then it will download it again even if the file exists
     * @return a boolean specifying if the version manifest was indeed downloaded
     */
    public static boolean downloadVersionManifest(MCVersion version, boolean force) {
        String versionManifestUrl = getVersionManifestUrl(version);
        if (versionManifestUrl == null) {
            Logger.err(String.format("URL was not found for %s", version));
            return false;
        }
        File versionManifest = new File(DIR_DL_VERSIONS + File.separator + version.name + ".json");
        if (!force && versionManifest.exists()) {
            return true;
        }
        return download(versionManifestUrl, versionManifest, null);
    }

    /**
     * Download the manifest from the mojang servers
     *
     * @param version can be null or a specific version to check if the manifest is up to date (aka if the version is not present the manifest is downloaded again)
     * @return boolean to say if the manifest is present and up to date
     */
    public static boolean downloadManifest(MCVersion version) {
        if (!manifestExists(version)) {
            if (!download(MANIFEST_URL, MANIFEST_FILE, null)) {
                return false;
            }
            if (version != null && !manifestExists(version)) {
                Logger.err(String.format("Manifest was incorrectly downloaded or the version does not exists yet %s %s", MANIFEST_FILE.getAbsolutePath(), version));
                return false;
            }
        }
        return true;
    }

    /**
     * Download the assets hash file depending of a version
     *
     * @param version the specific targeted version
     * @param force   if force is true then it will download it again even if the file exists
     * @return the name of the asset as version.json
     */
    public static String downloadVersionAssets(MCVersion version, boolean force) {
        Pair<String, String> assetIndexURL = getAssetIndexURL(version);
        if (assetIndexURL == null) {
            Logger.err(String.format("Could not get asset url for version %s", version));
            return null;
        }
        String[] urlSplit = assetIndexURL.getFirst().split("/");
        if (urlSplit.length < 2) {
            Logger.err(String.format("Could not get name of asset from url %s for version %s", assetIndexURL, version));
            return null;
        }
        String name = urlSplit[urlSplit.length - 1];
        File assetManifest = new File(DIR_DL_ASSETS + File.separator + name);
        if (!force && assetManifest.exists() && compareSha1(assetManifest, assetIndexURL.getSecond())) {
            return name;
        }
        return download(assetIndexURL.getFirst(), assetManifest, assetIndexURL.getSecond()) ? name : null;
    }

    /**
     * Download the client jar file depending of a version
     *
     * @param version the specific targeted version
     * @param force   if force is true then it will download it again even if the file exists
     * @return the name of the client jar as version.json
     */
    public static String downloadClientJar(MCVersion version, boolean force) {
        Pair<String, String> clientURL = getClientURL(version);
        if (clientURL == null) {
            Logger.err(String.format("Could not get client url for version %s", version));
            return null;
        }
        String[] urlSplit = clientURL.getFirst().split("/");
        if (urlSplit.length < 2) {
            Logger.err(String.format("Could not get name of client from url %s for version %s", clientURL, version));
            return null;
        }
        String name = urlSplit[urlSplit.length - 1];
        String versionDir = DIR_DL_VERSIONS + File.separator + version.name;
        try {
            Files.createDirectories(Paths.get(versionDir));
        } catch (IOException e) {
            Logger.err("Could not make the directory for the client.jar for version " + version + ". Error:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            return null;
        }
        File clientJar = new File(versionDir + File.separator + name);
        if (!force && clientJar.exists() && compareSha1(clientJar, clientURL.getSecond())) {
            return name;
        }
        return download(clientURL.getFirst(), clientJar, clientURL.getSecond()) ? name : null;
    }

    public static boolean extractJar(MCVersion version, String
            filename, Predicate<JarEntry> jarEntryPredicate, boolean force) {
        File clientJar = new File(DIR_DL_VERSIONS + File.separator + version.name + File.separator + filename);
        if (!clientJar.exists()) {
            Logger.err(String.format("Could not get client jar file for version %s", version));
            return false;
        }
        try {
            extractFromJar(clientJar, DIR_DL_ASSETS + File.separator + version.name, jarEntryPredicate, force);
        } catch (IOException e) {
            Logger.err("Could not extract from jar file for version. Version " + version + ". Error:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
            return false;
        }
        return true;
    }

    private static void extractFromJar(File jarFile, String
            pathPrefix, Predicate<JarEntry> jarEntryPredicate, boolean force) throws IOException {
        JarFile jar = new JarFile(jarFile);
        Enumeration<JarEntry> enumEntries = jar.entries();
        while (enumEntries.hasMoreElements()) {
            JarEntry entry = enumEntries.nextElement();
            File extractedFile = new File(pathPrefix + File.separator + entry.getName());
            if (extractedFile.exists() && !force || !jarEntryPredicate.test(entry)) continue;
            if (entry.isDirectory()) { // if its a directory, create it
                boolean ignored = extractedFile.mkdir();
                continue;
            }
            Files.createDirectories(extractedFile.toPath().getParent());
            InputStream is = jar.getInputStream(entry); // get the input stream
            FileOutputStream fos = new FileOutputStream(extractedFile);
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
        jar.close();
    }


    private static boolean download(String url, File out, String sha1) {
        Logger.info(String.format("Downloading %s for file %s", url, out.getName()));
        ReadableByteChannel rbc;
        try {
            rbc = Channels.newChannel(new URL(url).openStream());
        } catch (IOException e) {
            Logger.err(String.format("Could not open channel to url %s, Error:\n %s", url, e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(out);
            fileOutputStream.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fileOutputStream.close();
        } catch (IOException e) {
            Logger.err(String.format("Could not download from channel to url %s for file %s, Error:\n %s", url, out.getAbsolutePath(), e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return false;
        }
        return sha1 == null || compareSha1(out, sha1);
    }


    @SuppressWarnings("unchecked")
    private static Pair<String, String> getAssetIndexURL(MCVersion version) {
        JsonReader jsonReader = openJSON(new File(DIR_DL_VERSIONS + File.separator + version.name + ".json"));
        if (jsonReader == null) {
            return null;
        }
        Map<String, Object> map = new Gson().fromJson(jsonReader, Map.class);
        if (map.containsKey("assetIndex")) {
            Map<String, String> assets = (Map<String, String>) map.get("assetIndex");
            if (assets.containsKey("url") && assets.containsKey("sha1")) {
                return new Pair<>(assets.get("url"), assets.get("sha1"));
            }
            Logger.warn(String.format("Version manifest does not contain a asset url/sha1 key for %s", version));
        } else {
            Logger.warn(String.format("Version manifest does not contain a assetIndex key for %s", version));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Pair<String, String> getClientURL(MCVersion version) {
        JsonReader jsonReader = openJSON(new File(DIR_DL_VERSIONS + File.separator + version.name + ".json"));
        if (jsonReader == null) {
            return null;
        }
        Map<String, Object> map = new Gson().fromJson(jsonReader, Map.class);
        if (map.containsKey("downloads")) {
            Map<String, Map<String, String>> downloads = (Map<String, Map<String, String>>) map.get("downloads");
            if (downloads.containsKey("client")) {
                Map<String, String> client = downloads.get("client");
                if (client.containsKey("url") && client.containsKey("sha1")) {
                    return new Pair<>(client.get("url"), client.get("sha1"));
                }
                Logger.warn(String.format("Version manifest does not contain a client url/sha1 key for %s", version));
            } else {
                Logger.warn(String.format("Version manifest does not contain a client key for %s", version));
            }
        } else {
            Logger.warn(String.format("Version manifest does not contain a downloads key for %s", version));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String getVersionManifestUrl(MCVersion version) {
        JsonReader jsonReader = openJSON(MANIFEST_FILE);
        if (jsonReader == null) {
            return null;
        }
        Map<String, Object> map = new Gson().fromJson(jsonReader, Map.class);
        if (map.containsKey("versions")) {
            ArrayList<Map<String, String>> versions = (ArrayList<Map<String, String>>) map.get("versions");
            Optional<Map<String, String>> versionMap = versions.stream().filter(v -> v.containsKey("id") && v.get("id").equals(version.name)).findFirst();
            if (versionMap.isPresent()) {
                if (versionMap.get().containsKey("url")) {
                    return versionMap.get().get("url");
                }
                Logger.warn(String.format("Manifest does not contain the url/sha1 key for %s", version));
            } else {
                Logger.warn(String.format("Manifest does not contain the version array key for %s", version));
            }
        } else {
            Logger.warn("Manifest does not contain a versions key");
        }
        return null;
    }


    private static JsonReader openJSON(File file) {
        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            Logger.err(String.format("Could not open file at %s, Error:\n %s", file.getAbsolutePath(), e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return null;
        }
        return new JsonReader(fileReader);
    }

    private static boolean manifestExists(MCVersion version) {
        if (MANIFEST_FILE.exists()) {
            JsonReader jsonReader = openJSON(MANIFEST_FILE);
            if (jsonReader == null) {
                return false;
            }
            try {
                if (version == null || versionExists(version, jsonReader)) {
                    return true;
                }
            } catch (IOException e) {
                Logger.err(String.format("JSON file had an issue %s, Error:\n %s", MANIFEST_FILE.getAbsolutePath(), e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean versionExists(MCVersion version, JsonReader jsonReader) throws IOException {
        Map<String, Object> map = new Gson().fromJson(jsonReader, Map.class);
        if (map.containsKey("versions")) {
            ArrayList<Map<String, String>> versions = (ArrayList<Map<String, String>>) map.get("versions");
            return versions.stream().anyMatch(v -> v.containsKey("id") && v.get("id").equals(version.name));
        }
        return false;
    }

    private static boolean compareSha1(File file, String sha1) {
        if (sha1 != null && file != null) {
            try {
                return getFileChecksum(MessageDigest.getInstance("SHA-1"), file).equals(sha1);
            } catch (NoSuchAlgorithmException e) {
                Logger.err("Could not compute sha1 since algorithm does not exists. Error:\n " + e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace()));
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
            Logger.err(String.format("Failed to read file for checksum, Error:\n %s", e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return "";
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static Stream<File> collectAllFiles(File path, Predicate<File> predicate) {
        Stream<File> fileStream = Stream.empty();
        for (File file : Objects.requireNonNull(path.listFiles())) {
            if (predicate != null && predicate.test(file)) {
                fileStream = Stream.concat(fileStream, Stream.of(file));
            }
            if (file.isDirectory()) {
                fileStream = Stream.concat(fileStream, collectAllFiles(file, predicate));
            }
        }
        return fileStream;
    }

    public static java.util.List<Path> getFileHierarchical(Path dir, String fileName, String extension) throws
            IOException {
        return Files.walk(dir).filter(file -> Files.isRegularFile(file) && file.toAbsolutePath().getFileName().toString().equals(fileName + extension)).collect(Collectors.toList());
    }

    public static java.util.List<Pair<Path, InputStream>> getInputStream(Path dir, boolean isJar, String
            name, String extension) {
        java.util.List<Path> paths;
        java.util.List<Pair<Path, InputStream>> list = new ArrayList<>();
        try {
            paths = Assets.getFileHierarchical(dir, name, extension);
        } catch (IOException e) {
            Logger.err(String.format("Exception while screening the files for '%s%s' from root %s with error:\n %s", name, extension, dir.toString(), e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            return list;
        }
        for (Path path : paths) {
            try {
                InputStream inputStream = isJar ? Config.class.getResourceAsStream(path.toString()) : new FileInputStream(path.toString());
                if (inputStream == null) {
                    Logger.err(String.format("Input stream is null, %s", path));
                    return list;
                }
                list.add(new Pair<>(path, inputStream));
            } catch (IOException e) {
                Logger.err(String.format("Exception while  getting the file input for %s at %s with error:\n %s", name, dir.toString(), e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
            }
        }
        if (list.isEmpty()) {
            Logger.err(String.format("File not found for %s", name));
        }

        return list;
    }

    public static java.util.List<Pair<String, BufferedImage>> getAsset(Path dir, boolean isJar, String
            name, String extension, Function<Path, String> fnObjectStorage) {
        return getInputStream(dir, isJar, name, extension).stream().map(e -> {
            try {
                return new Pair<>(fnObjectStorage.apply(e.getFirst()), ImageIO.read(e.getSecond()));
            } catch (IOException ex) {
                Logger.err(String.format("Exception while reading the file input stream for %s at %s with error:\n %s", name, dir.toString(), ex.getMessage() + "\n Stacktrace:\n " + Arrays.toString(ex.getStackTrace())));
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static File choseFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private static String getDataRestAPI(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                Logger.err(String.format("Failed to fetch URL %s. Errorcode : %s", apiUrl, responseCode));
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
            Logger.err(String.format("Failed to fetch URL %s. Error:\n %s", apiUrl, e.getMessage() + "\n Stacktrace:\n " + Arrays.toString(e.getStackTrace())));
        }
        return null;
    }
}