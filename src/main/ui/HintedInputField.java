package src.main.ui;


import javax.swing.*;
import java.awt.*;

public class HintedInputField extends JTextField {
    private String hintText;

    public HintedInputField(String hintText) {
        super();
        this.hintText = hintText;
    }

    public HintedInputField(String initVal, String hintText) {
        super(initVal);
        this.hintText = hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (getText().length() == 0) {
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int height = getHeight();
            Insets insets = getInsets();
            FontMetrics fontMetrics = g.getFontMetrics();

            int colorA = getBackground().getRGB();
            int colorB = getForeground().getRGB();

            int mult = 0xfefefefe;
            Color finalColor = new Color(((colorA & mult) >>> 1) + ((colorB & mult) >>> 1), true);

            g.setColor(finalColor);
            g.drawString(hintText, insets.left, (height / 2) + (fontMetrics.getAscent() / 2) - 2);
        }
    }
}