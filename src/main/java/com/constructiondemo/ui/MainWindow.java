package com.constructiondemo.ui;

import com.constructiondemo.model.GameWorld;
import com.constructiondemo.simulation.SimulationEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainWindow extends JFrame {

    private final GameWorld world;
    private final SimulationEngine engine;
    private final GameCanvas canvas;
    private final InfoPanel infoPanel;
    private JToggleButton playPauseBtn;

    public MainWindow(GameWorld world, SimulationEngine engine) {
        super("Factory Simulator");
        this.world  = world;
        this.engine = engine;

        canvas    = new GameCanvas(world);
        infoPanel = new InfoPanel();

        canvas.setOnSelectionChanged(() -> infoPanel.update(canvas.getSelectedStructure()));

        engine.addTickListener(() -> SwingUtilities.invokeLater(() -> {
            infoPanel.refreshStatus(canvas.getSelectedStructure());
            canvas.repaint();
        }));

        setLayout(new BorderLayout());
        add(buildTopBar(),         BorderLayout.NORTH);
        add(new ToolPanel(canvas), BorderLayout.WEST);
        add(canvas,                BorderLayout.CENTER);
        add(infoPanel,             BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                engine.stop();
                dispose();
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JToolBar buildTopBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setBackground(new Color(40, 40, 55));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        playPauseBtn = new JToggleButton("  Play");
        styleBtn(playPauseBtn, new Color(34, 120, 34));
        playPauseBtn.addActionListener(e -> {
            if (engine.isRunning()) {
                engine.stop();
                playPauseBtn.setText("  Play");
            } else {
                engine.start();
                playPauseBtn.setText("  Pause");
            }
        });

        JButton centerBtn = new JButton("Center View");
        styleBtn(centerBtn, new Color(60, 80, 120));
        centerBtn.addActionListener(e -> canvas.centerView());

        JButton clearBtn = new JButton("Clear All");
        styleBtn(clearBtn, new Color(120, 50, 50));
        clearBtn.addActionListener(e -> promptClear());

        JLabel speedLabel = new JLabel("  Speed:");
        speedLabel.setForeground(Color.LIGHT_GRAY);
        JSlider speedSlider = new JSlider(100, 2000, 500);
        speedSlider.setBackground(new Color(40, 40, 55));
        speedSlider.setToolTipText("Simulation tick interval (ms) — left=faster, right=slower");
        speedSlider.addChangeListener(e -> engine.setTickInterval(speedSlider.getValue()));
        speedSlider.setPreferredSize(new Dimension(130, 24));

        bar.add(playPauseBtn);
        bar.addSeparator(new Dimension(8, 0));
        bar.add(centerBtn);
        bar.addSeparator(new Dimension(8, 0));
        bar.add(clearBtn);
        bar.addSeparator(new Dimension(8, 0));
        bar.add(speedLabel);
        bar.add(speedSlider);
        return bar;
    }

    private void promptClear() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Remove all structures and belts?", "Clear Factory",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            engine.stop();
            playPauseBtn.setText("  Play");
            while (!world.getStructures().isEmpty())
                world.removeStructure(world.getStructures().get(0));
            canvas.repaint();
        }
    }

    private void styleBtn(AbstractButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
