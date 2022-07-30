package com.anipgames.WAT_Vis.util;

import java.awt.*;

public class ColorPalette {
    private Color[] colors;

    public ColorPalette(Color[] colors) {
        this.colors = colors;
    }

    public int getColorsCount() {
        return colors.length;
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    public static ColorPalette PALETTE_A = new ColorPalette(new Color[]{
            new Color(234, 85, 69, 255),
            new Color(244, 106, 155, 255),
            new Color(239, 155, 32, 255),
            new Color(237, 191, 51, 255),
            new Color(237, 225, 91, 255),
            new Color(189, 207, 50, 255),
            new Color(135, 188, 69, 255),
            new Color(39, 174, 239, 255),
            new Color(179, 61, 198, 255)
    });

    public static ColorPalette PALETTE_B = new ColorPalette(new Color[]{
            new Color(230, 0, 73, 255),
            new Color(11, 180, 255, 255),
            new Color(80, 233, 145, 255),
            new Color(230, 216, 0, 255),
            new Color(155, 25, 245, 255),
            new Color(255, 163, 0, 255),
            new Color(220, 10, 180, 255),
            new Color(179, 212, 255, 255),
            new Color(0, 191, 160, 255)
    });

    public static ColorPalette PALETTE_C = new ColorPalette(new Color[]{
            new Color(179, 0, 0, 255),
            new Color(124, 17, 88, 255),
            new Color(68, 33, 175, 255),
            new Color(26, 83, 255, 255),
            new Color(13, 136, 230, 255),
            new Color(0, 183, 199, 255),
            new Color(90, 212, 90, 255),
            new Color(139, 224, 78, 255),
            new Color(235, 220, 120, 255)
    });

    public static ColorPalette PALETTE_D = new ColorPalette(new Color[]{
            new Color(253, 127, 111, 255),
            new Color(126, 176, 213, 255),
            new Color(178, 224, 97, 255),
            new Color(189, 126, 190, 255),
            new Color(255, 181, 90, 255),
            new Color(255, 238, 101, 255),
            new Color(190, 185, 219, 255),
            new Color(253, 204, 229, 255),
            new Color(139, 211, 199, 255)
    });

    public static ColorPalette PALETTE_E = new ColorPalette(new Color[]{
            new Color(44, 102, 230, 255),
            new Color(255, 211, 0, 255),
            new Color(206, 123, 241, 255),
            new Color(125, 208, 44, 255),
            new Color(220, 64, 64, 255),
            new Color(135, 215, 209, 255),
            new Color(241, 124, 55, 255),
            new Color(22, 197, 145, 255)
    });
}