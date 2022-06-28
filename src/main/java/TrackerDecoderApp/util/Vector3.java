package TrackerDecoderApp.util;

import java.awt.*;

public class Vector3 {
    public int x;
    public int y;
    public int z;

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3 parseVector3(String input) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector3((int) Float.parseFloat(components[0]), (int) Float.parseFloat(components[1]), (int) Float.parseFloat(components[2]));
    }

    public static Vector3 parseVector3(String input, float scaleMult) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector3((int) (Float.parseFloat(components[0]) * scaleMult), (int) (Float.parseFloat(components[1]) * scaleMult), (int) (Float.parseFloat(components[2]) * scaleMult));
    }

    public static Vector3 parseVector3FromChunk(String input, boolean convertToBlockPos) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector3((int) Math.floor(Float.parseFloat(components[0]) * (convertToBlockPos ? 16 : 1)), 0, (int) Math.floor(Float.parseFloat(components[1]) * (convertToBlockPos ? 16 : 1)));
    }

    public static Vector3 parseVector3FromChunk(String input, boolean convertToBlockPos, float scaleMult) {
        input = input.substring(2, input.length() - 1);

        String[] components = input.split(",");

        return new Vector3((int) (Math.floor(Float.parseFloat(components[0]) * (convertToBlockPos ? 16 : 1)) * scaleMult), 0, (int) (Math.floor(Float.parseFloat(components[1]) * (convertToBlockPos ? 16 : 1)) * scaleMult));
    }

    public Point toPoint() {
        return new Point(x, z);
    }

    public static String toString(Vector3 input) {
        return "(" + input.x + ", " + input.y + ", " + input.z + ")";
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public void Add(int toAdd) {
        x += toAdd;
        y += toAdd;
        z += toAdd;
    }

    public boolean insideBounds(Point start, Point end) {
        float startX = Math.min(start.x, end.x);
        float startY = Math.min(start.y, end.y);
        float endX = Math.max(start.x, end.x);
        float endY = Math.max(start.y, end.y);

        return x > startX && x < endX && y > startY && y < endY;
    }

    public float sqrDistTo(Vector3 to) {
        return (to.x - x) * (to.x - x) + (to.y - y) * (to.y - y);
    }

    public float sqrDistTo(Point to) {
        return (to.x - x) * (to.x - x) + (to.y - y) * (to.y - y);
    }

    public static float sqrDist(Vector3 from, Vector3 to) {
        return (to.x - from.x) * (to.x - from.x) + (to.y - from.y) * (to.y - from.y);
    }

    public static float sqrDist(Point from, Point to) {
        return (to.x - from.x) * (to.x - from.x) + (to.y - from.y) * (to.y - from.y);
    }

    @Override
    public int hashCode() {
        return x * z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Vector3 vec)) {
            return false;
        }

        return this.x == vec.x && this.z == vec.z;
    }
}