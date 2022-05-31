package src;/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/* src.com.anip24.trackerDecoder.Utils.java is used by FileChooserDemo2.java. */
public class Utils {
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

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
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
        if (Math.abs(goal - cur) <= maxDelta)
            return goal;
        return cur + sign(goal - cur) * maxDelta;
    }

    public static float sign(float f) {
        return f >= 0f ? 1f : -1f;
    }

    public static int clamp(int v, int min, int max) {
        if (v < min)
            v = min;
        else if (v > max)
            v = max;
        return v;
    }

    public static float clamp(float v, float min, float max) {
        if (v < min)
            v = min;
        else if (v > max)
            v = max;
        return v;
    }

    public static float clamp01(float v) {
        if (v < 0)
            v = 0;
        else if (v > 1)
            v = 1;
        return v;
    }

    public static double calculateAverage(ArrayList<Double> input) {
        return input.stream().mapToDouble(d -> d).average().orElse(0.0);
    }
}