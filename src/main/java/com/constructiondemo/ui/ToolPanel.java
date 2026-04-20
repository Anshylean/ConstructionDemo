package com.constructiondemo.ui;

import com.constructiondemo.model.ItemType;
import com.constructiondemo.model.structures.ConstructorStructure;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class ToolPanel extends JPanel {

    private final GameCanvas canvas;

    public ToolPanel(GameCanvas canvas) {
        this.canvas = canvas;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(45, 45, 55));
        setPreferredSize(new Dimension(160, 0));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        addSection("Tools", buildToolButtons());
        addSection("Miners", buildMinerButtons());
        addSection("Processing", buildProcessingButtons());
        addSection("Constructor", buildConstructorButtons());
        addSection("Logistics", buildLogisticsButtons());
    }

    private void addSection(String title, JPanel content) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100)),
                title, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 11), new Color(180, 180, 200));
        content.setBorder(border);
        add(content);
        add(Box.createVerticalStrut(6));
    }

    private JPanel buildToolButtons() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.setBackground(new Color(45, 45, 55));
        p.add(toolButton("Select", GameCanvas.Tool.SELECT));
        p.add(toolButton("Belt",   GameCanvas.Tool.BELT));
        p.add(toolButton("Delete", GameCanvas.Tool.DELETE));
        return p;
    }

    private JPanel buildMinerButtons() {
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.setBackground(new Color(45, 45, 55));
        p.add(placeButton("Iron Miner",   "Miner", ItemType.IRON_ORE,   new Color(139, 90, 43)));
        p.add(placeButton("Copper Miner", "Miner", ItemType.COPPER_ORE, new Color(139, 90, 43)));
        return p;
    }

    private JPanel buildProcessingButtons() {
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.setBackground(new Color(45, 45, 55));
        p.add(placeButton("Iron Smelter",   "Smelter_Iron",   null, new Color(178, 34, 34)));
        p.add(placeButton("Copper Smelter", "Smelter_Copper", null, new Color(178, 34, 34)));
        return p;
    }

    private JPanel buildConstructorButtons() {
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.setBackground(new Color(45, 45, 55));
        for (ConstructorStructure.Recipe r : ConstructorStructure.Recipe.values()) {
            p.add(placeButton(r.label, "Constructor", r, new Color(34, 139, 34)));
        }
        return p;
    }

    private JPanel buildLogisticsButtons() {
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.setBackground(new Color(45, 45, 55));
        p.add(placeButton("Storage", "Storage", null, new Color(100, 100, 200)));
        return p;
    }

    private JButton toolButton(String label, GameCanvas.Tool tool) {
        JButton btn = styledButton(label, new Color(70, 70, 90));
        btn.addActionListener(e -> canvas.setTool(tool));
        return btn;
    }

    private JButton placeButton(String label, String type, Object param, Color accent) {
        JButton btn = styledButton(label, accent.darker().darker());
        btn.addActionListener(e -> {
            canvas.setTool(GameCanvas.Tool.PLACE);
            canvas.setPlaceType(type, param);
        });
        return btn;
    }

    private JButton styledButton(String label, Color bg) {
        JButton btn = new JButton(label);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }
}
