package src.main.ui;

import src.main.PlayerTrackerDecoder;
import src.main.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class LabeledComponent<T extends Component> extends JPanel {
    private final JLabel label;
    private final T component;
    private int side;

    private final static int LEFT = 0;
    private final static int RIGHT = 1;

    public LabeledComponent(String labelText, T component, int side) {
        super();
        setLayout(new GridBagLayout());
        boolean left = side == LEFT;

        this.label = new JLabel(labelText, left ? JLabel.RIGHT : JLabel.LEFT);
        this.component = component;
        this.side = side;

        this.label.setLabelFor(component);

        if (PlayerTrackerDecoder.DEBUG) {
            setBackground(Utils.randColor());
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.anchor = left ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.gridx = left ? 0 : 1;
        gbc.insets = new Insets(0, left ? 0 : 10, 0, left ? 10 : 0);
        add(label, gbc);

        gbc.anchor = left ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        gbc.gridx = left ? 1 : 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(component, gbc);
    }

    public LabeledComponent(String label, T component) {
        this(label, component, LEFT);
    }

    public JLabel getLabel() {
        return this.label;
    }

    public void setLabelText(String labelText) {
        label.setText(labelText);
    }

    public T getComponent() {
        return component;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;

        removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.anchor = side == LEFT ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.gridx = side == LEFT ? 0 : 1;
        add(label, gbc);

        gbc.anchor = side == LEFT ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        gbc.gridx = side == LEFT ? 1 : 0;
        add(component, gbc);
    }

    @Override
    public void setEnabled(boolean value) {
        label.setEnabled(value);
        component.setEnabled(value);
    }
}