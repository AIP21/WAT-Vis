package util.objects;

import java.awt.*;

public class Vector2 {
    public int x;
    public int y;

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Vector2 parseVector2(String input) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2((int) Float.parseFloat(components[0]), (int) Float.parseFloat(components[1]));
    }

    public static Vector2 parseVector2(String input, float scaleMult) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2((int) (Float.parseFloat(components[0]) * scaleMult), (int) (Float.parseFloat(components[2]) * scaleMult));
    }

    public static Vector2 parseVector2FromChunk(String input, boolean convertToBlockPos) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2((int) Math.floor(Float.parseFloat(components[0]) * (convertToBlockPos ? 16 : 1)), (int) Math.floor(Float.parseFloat(components[1]) * (convertToBlockPos ? 16 : 1)));
    }

    public static Vector2 parseVector2FromChunk(String input, boolean convertToBlockPos, float scaleMult) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector2((int) (Math.floor(Float.parseFloat(components[0]) * (convertToBlockPos ? 16 : 1)) * scaleMult), (int) (Math.floor(Float.parseFloat(components[1]) * (convertToBlockPos ? 16 : 1)) * scaleMult));
    }

    public Point toPoint() {
        return new Point(x, y);
    }

    public static String toString(Vector2 input) {
        return "(" + input.x + ", " + input.y + ")";
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public void Add(int toAdd) {
        x += toAdd;
        y += toAdd;
    }
}