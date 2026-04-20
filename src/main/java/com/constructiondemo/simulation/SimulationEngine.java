package com.constructiondemo.simulation;

import com.constructiondemo.model.ConveyorBelt;
import com.constructiondemo.model.GameWorld;
import com.constructiondemo.model.Structure;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class SimulationEngine {

    private static final int TICK_MS = 500;

    private final GameWorld world;
    private final Timer timer;
    private final List<Runnable> tickListeners = new ArrayList<>();
    private boolean running = false;

    public SimulationEngine(GameWorld world) {
        this.world = world;
        this.timer = new Timer(TICK_MS, e -> tick());
    }

    private void tick() {
        for (Structure s : world.getStructures())   s.tick();
        for (ConveyorBelt b : world.getBelts())     b.tick();
        for (Runnable listener : tickListeners)     listener.run();
    }

    public void start() {
        if (!running) { timer.start(); running = true; }
    }

    public void stop() {
        if (running) { timer.stop(); running = false; }
    }

    public boolean isRunning()                        { return running; }
    public void addTickListener(Runnable r)            { tickListeners.add(r); }
    public void removeTickListener(Runnable r)         { tickListeners.remove(r); }
    public void setTickInterval(int ms)                { timer.setDelay(ms); }
}
