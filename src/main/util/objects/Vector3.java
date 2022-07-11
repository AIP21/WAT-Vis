package src.main.util.objects;

import java.awt.*;

public class Vector3 {
    public final int x;
    public final int y;
    public final int z;

    public final Point point;

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.point = new Point(x, z);
    }

    public static Vector3 parseVector3(String input, boolean convertChunks) {
        if (input.contains("[")) {
            return parseVector3FromChunk(input, convertChunks);
        }

        String[] components = input.replace(" (", "").replace(")", "").split(", ");

        return new Vector3(Integer.parseInt(components[0]), Integer.parseInt(components[1]), Integer.parseInt(components[2]));
    }

    public static Vector3 parseVector3FromChunk(String input, boolean convertToBlockPos) {
        String[] components = input.replace(" [", "").replace("]", "").split(", ");

        return new Vector3((int) Math.floor(Integer.parseInt(components[0]) * (convertToBlockPos ? 16 : 1)), 0, (int) Math.floor(Integer.parseInt(components[1]) * (convertToBlockPos ? 16 : 1)));
    }

    public static String toString(Vector3 input) {
        return "(" + input.x + ", " + input.y + ", " + input.z + ")";
    }

    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public boolean insideBounds(Point start, Point end) {
        float startX = Math.min(start.x, end.x);
        float startY = Math.min(start.y, end.y);
        float endX = Math.max(start.x, end.x);
        float endY = Math.max(start.y, end.y);

        return x >= startX && x < endX && z >= startY && z < endY;
    }

    public float sqrDistTo(Vector3 to) {
        return (to.z - z) * (to.z - z) + (to.x - x) * (to.x - x);
    }

    public float sqrDistTo(Point to) {
        return (to.y - z) * (to.y - z) + (to.x - x) * (to.x - x);
    }

    public static float sqrDist(Vector3 from, Vector3 to) {
        return (to.z - from.z) * (to.z - from.z) + (to.x - from.x) * (to.x - from.x);
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