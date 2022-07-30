package com.anipgames.WAT_Vis.ui;

import com.anipgames.WAT_Vis.WatVis;
import com.anipgames.WAT_Vis.util.Utils;

import javax.swing.*;
import java.awt.*;

public class LabeledComponent<T extends Component> extends JPanel {
    private final JLabel label;
    private final T component;
    private int side;

    public final static int LEFT = 0;
    public final static int RIGHT = 1;

    public LabeledComponent(String labelText, T component, int side, double leftWeight, double rightWeight) {
        super();
        setLayout(new GridBagLayout());
        boolean left = side == LEFT;

        this.label = new JLabel(labelText, left ? JLabel.RIGHT : JLabel.LEFT);
        this.component = component;
        this.side = side;

        this.label.setLabelFor(component);

        if (WatVis.DEBUG) {
            setBackground(Utils.randColor());
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.anchor = left ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.gridx = left ? 0 : 1;
        gbc.weightx = leftWeight;
        gbc.weighty = leftWeight;
        gbc.insets = new Insets(0, left ? 0 : 10, 0, left ? 10 : 0);
        add(label, gbc);

        gbc.anchor = left ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        gbc.gridx = left ? 1 : 0;
        gbc.weightx = rightWeight;
        gbc.weighty = rightWeight;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(component, gbc);
    }

    public LabeledComponent(String labelText, T component, int side) {
        this(labelText, component, side, 1, 1);
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