package src.main.mapping.minemap.util.math;

public class DisplayMaths {
    public static double smartClamp(double value, double constraint1, double constraint2) {
        double min = Math.min(constraint1, constraint2);
        double max = Math.max(constraint1, constraint2);
        if (value < min) {
            return min;
        } else {
            return Math.min(value, max);
        }
    }
}