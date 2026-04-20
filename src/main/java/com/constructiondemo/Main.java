package com.constructiondemo;

import com.constructiondemo.model.GameWorld;
import com.constructiondemo.simulation.SimulationEngine;
import com.constructiondemo.ui.MainWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            GameWorld world = new GameWorld();
            SimulationEngine engine = new SimulationEngine(world);
            MainWindow window = new MainWindow(world, engine);
            window.setVisible(true);
        });
    }
}
