package com.anipgames.WAT_Vis.util;

import com.anipgames.WAT_Vis.util.objects.Vector2;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Utils {
    public final static Random random = new Random();

    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    public final static String txt = "txt";

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = Utils.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    public static boolean isEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }

        return false;
    }

    public static float lerp(float a, float b, float t) {
        float val = a + (b - a) * t;
        return !approximately(val, b, 0.001f) ? val : b;
    }

    public static float lerp(float a, float b, double t) {
        float val = (float) (a + (b - a) * t);
        return !approximately(val, b, 0.001f) ? val : b;
    }

    public static double lerp(double a, double b, double t) {
        double val = (a + (b - a) * t);
        return !approximately(val, b, 0.001f) ? val : b;
    }

    public static int lerp(int a, int b, float t) {
        float val = (int) (a + (b - a) * t);
        return (int) val;
    }

    public static int lerp(int a, int b, double t) {
        float val = (float) (a + (b - a) * t);
        return (int) val;
    }

    public static float lerpClamped(float a, float b, float t) {
        float val = a + (b - a) * t;
        return !approximately(val, b, 0.001f) ? clamp(val, a, b) : b;
    }

    public static int lerpClamped(int a, int b, float t) {
        float val = a + (b - a) * t;
        return (int) clamp(val, a, b);
    }

    public static Color lerpColor(Color a, Color b, float percent) {
        int red = lerp(a.getRed(), b.getRed(), percent);
        int blue = lerp(a.getBlue(), b.getBlue(), percent);
        int green = lerp(a.getGreen(), b.getGreen(), percent);
        int alpha = lerp(a.getAlpha(), b.getAlpha(), percent);
        return new Color(red, green, blue, alpha);
    }

    public static float inverseLerp(float a, float b, float val) {
        return (val - a) / (b - a);
    }

    public static float remap(float min1, float max1, float min2, float max2, float val) {
        return lerp(min2, max2, inverseLerp(min1, max1, val));
    }

    public static boolean approximately(int a, int b, float threshold) {
        return ((a - b) < threshold);
    }

    public static boolean approximately(float a, float b, float threshold) {
        return ((a - b) < threshold);
    }

    public static boolean approximately(double a, double b, float threshold) {
        return ((a - b) < threshold);
    }

    public static float smoothStep(float from, float to, float t) {
        t = -2.0F * t * t * t + 3.0F * t * t;
        return to * t + from * (1F - t);
    }

    public static double smoothStep(double from, double to, float t) {
        t = -2.0F * t * t * t + 3.0F * t * t;
        return to * t + from * (1F - t);
    }

    public static float moveTo(float cur, float goal, float maxDelta) {
        if (Math.abs(goal - cur) <= maxDelta) return goal;
        return cur + sign(goal - cur) * maxDelta;
    }

    public static float sign(float f) {
        return f >= 0f ? 1f : -1f;
    }

    public static int clamp(int v, int min, int max) {
        if (v < min) v = min;
        else if (v > max) v = max;
        return v;
    }

    public static float clamp(float v, float min, float max) {
        if (v < min) v = min;
        else if (v > max) v = max;
        return v;
    }

    public static float clamp01(float v) {
        if (v < 0) v = 0;
        else if (v > 1) v = 1;
        return v;
    }

    public static double calculateAverage(ArrayList<Double> input) {
        return input.stream().mapToDouble(d -> d).average().orElse(0.0);
    }

    public static double smartClamp(double value, double constraint1, double constraint2) {
        double min = Math.min(constraint1, constraint2);
        double max = Math.max(constraint1, constraint2);
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }

    public static String convertToMultiline(String orig) {
        return "<html>" + orig.replaceAll("\n", "<br>");
    }

    public static BufferedImage invertImage(BufferedImage inputImage) {
        BufferedImage newImg = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < inputImage.getWidth(); x++) {
            for (int y = 0; y < inputImage.getHeight(); y++) {
                int rgba = inputImage.getRGB(x, y);
                Color col = new Color(rgba, true);
                col = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue(), col.getAlpha());
                newImg.setRGB(x, y, col.getRGB());
            }
        }
        return newImg;
    }

    public static BufferedImage invertImage(Image inputImage) {
        BufferedImage buffImage = toBufferedImage(inputImage);
        BufferedImage newImg = new BufferedImage(buffImage.getWidth(), buffImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < buffImage.getWidth(); x++) {
            for (int y = 0; y < buffImage.getHeight(); y++) {
                int rgba = buffImage.getRGB(x, y);
                Color col = new Color(rgba, true);
                col = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue(), col.getAlpha());
                newImg.setRGB(x, y, col.getRGB());
            }
        }
        return newImg;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage buffImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = buffImage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return buffImage;
    }

    public static Font getFont(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;

        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }

        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());

        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    public static Color randColor() {
        return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
    }

    public static boolean approximately(Color a, Color b) {
        return (Utils.approximately(a.getRed(), b.getRed(), 20.0F) && Utils.approximately(a.getGreen(), b.getGreen(), 20.0F) && Utils.approximately(a.getBlue(), b.getBlue(), 20.0F));
    }

    public static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = bi.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return bi;
    }

    public static float max(float[] input) {
        float max = Float.NEGATIVE_INFINITY;
        for (float v : input)
            if (v > max) max = v;

        return max;
    }

    public static int max(Collection<Integer> input) {
        int max = Integer.MIN_VALUE;
        for (int v : input)
            if (v > max) max = v;

        return max;
    }

    public static int max(int[] input) {
        int max = Integer.MIN_VALUE;
        for (int v : input)
            if (v > max) max = v;

        return max;
    }

    public static float min(float[] input) {
        float min = Float.POSITIVE_INFINITY;
        for (float v : input)
            if (v < min) min = v;

        return min;
    }

    public static int min(Collection<Integer> input) {
        int min = Integer.MAX_VALUE;
        for (int v : input)
            if (v < min) min = v;

        return min;
    }

    public static int min(int[] input) {
        int min = Integer.MAX_VALUE;
        for (int v : input)
            if (v < min) min = v;

        return min;
    }

    public static float scale(float input, float min1, float max1, float min2, float max2) {
        return (input - min1) * (max2 - min2) / (max1 - min1) + min2;
    }

    public static int scale(int input, int min1, int max1, int min2, int max2) {
        return Math.round((float) (input - min1) * (float) (max2 - min2) / (float) (max1 - min1) + min2);
    }

    public static String formatPretty(double d) {
        if (d == (long) d) return String.format("%d", (long) d);
        else return String.format("%s", d);
    }

    public static double roundToSigFigs(double input, int digits) {
        BigDecimal bd = new BigDecimal(input).round(new MathContext(digits));
        return bd.doubleValue();
    }

    public static double roundToSigFigs(double input) {
        return roundToSigFigs(input, 3);
    }

    public static float getTextHeight(Graphics2D g, String string, Font font) {
        return font.getLineMetrics(string, g.getFontRenderContext()).getHeight();
    }
}