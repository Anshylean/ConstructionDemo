package com.constructiondemo.ui;

import com.constructiondemo.model.Structure;

import javax.swing.*;
import java.awt.*;

public class InfoPanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel statusLabel;
    private final JLabel hintLabel;

    public InfoPanel() {
        setLayout(new BorderLayout(6, 0));
        setBackground(new Color(35, 35, 45));
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        setPreferredSize(new Dimension(0, 60));

        titleLabel  = styledLabel("No selection", Font.BOLD, 12, new Color(220, 220, 255));
        statusLabel = styledLabel("", Font.PLAIN, 11, new Color(180, 180, 180));
        hintLabel   = styledLabel("Left-click: place/select  |  Right-click: delete  |  Middle/Alt+drag: pan  |  Scroll: zoom", Font.ITALIC, 10, new Color(120, 120, 140));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(new Color(35, 35, 45));
        left.add(titleLabel);
        left.add(statusLabel);

        add(left, BorderLayout.CENTER);
        add(hintLabel, BorderLayout.EAST);
    }

    public void update(Structure s) {
        if (s == null) {
            titleLabel.setText("No selection");
            statusLabel.setText("");
        } else {
            titleLabel.setText(s.getName());
            statusLabel.setText(s.getStatusText());
        }
    }

    public void refreshStatus(Structure s) {
        if (s != null) statusLabel.setText(s.getStatusText());
    }

    private JLabel styledLabel(String text, int style, int size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        return l;
    }
}
