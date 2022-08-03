package com.anipgames.WAT_Vis.util.objects;

import com.anipgames.WAT_Vis.util.Utils;

import java.awt.*;

public class Vector2 {
    public final float x;
    public final float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 parseVector2(String input) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2(Float.parseFloat(components[0]), Float.parseFloat(components[1]));
    }

    public static Vector2 parseVector2(String input, float scaleMult) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2(Float.parseFloat(components[0]) * scaleMult, Float.parseFloat(components[2]) * scaleMult);
    }

    public static Vector2 parseVector2FromChunk(String input, boolean convertToBlockPos) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2((float) Math.floor(Float.parseFloat(components[0]) * (convertToBlockPos ? 16 : 1)), (float) Math.floor(Float.parseFloat(components[1]) * (convertToBlockPos ? 16 : 1)));
    }

    public static Vector2 parseVector2FromChunk(String input, boolean convertToBlockPos, float scaleMult) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2((float) (Math.floor(Float.parseFloat(components[0]) * (convertToBlockPos ? 16 : 1)) * scaleMult), (float) (Math.floor(Float.parseFloat(components[1]) * (convertToBlockPos ? 16 : 1)) * scaleMult));
    }

    public float sqrMagnitude() {
        return x * x + y * y;
    }

    public static float sqrMagnitude(Vector2 vec) {
        return vec.x * vec.x + vec.y * vec.y;
    }

    public static Vector2 lerp(Vector2 a, Vector2 b, float t) {
        return new Vector2(Utils.lerp(a.x, b.x, t), Utils.lerp(a.y, b.y, t));
    }

    public Vector2 minus(Vector2 subtractor) {
        return new Vector2(x - subtractor.x, y - subtractor.y);
    }

    public static Vector2 remap(float a, float b, Vector2 c, Vector2 d, float u) {
        return Vector2.lerp(c, d, (u - a) / (b - a));
    }

    public static String toString(Vector2 input) {
        return "(" + input.x + ", " + input.y + ")";
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}