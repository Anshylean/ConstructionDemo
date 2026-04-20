package com.constructiondemo.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GameWorld {

    private final List<Structure> structures = new ArrayList<>();
    private final List<ConveyorBelt> belts = new ArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(0);

    public boolean addStructure(Structure s) {
        for (int dx = 0; dx < s.getWidthTiles(); dx++) {
            for (int dy = 0; dy < s.getHeightTiles(); dy++) {
                if (getStructureAt(s.getGridX() + dx, s.getGridY() + dy) != null)
                    return false;
            }
        }
        s.setId("s" + idCounter.incrementAndGet());
        structures.add(s);
        return true;
    }

    public void removeStructure(Structure s) {
        belts.removeIf(b -> b.getSourceStructure() == s || b.getDestinationStructure() == s);
        structures.remove(s);
    }

    public boolean addBelt(ConveyorBelt belt) {
        // prevent duplicate connections on same source port
        for (ConveyorBelt b : belts) {
            if (b.getSource() == belt.getSource()) return false;
        }
        belts.add(belt);
        return true;
    }

    public void removeBelt(ConveyorBelt belt) {
        belts.remove(belt);
    }

    public Structure getStructureAt(int gridX, int gridY) {
        for (Structure s : structures) {
            if (s.occupiesTile(gridX, gridY)) return s;
        }
        return null;
    }

    /** Returns the port within RADIUS world-pixels of (wx, wy), or null. */
    public Port findPortNear(int worldX, int worldY, int tileSize, int radius) {
        Port closest = null;
        int closestDist = radius * radius;
        for (Structure s : structures) {
            for (Port p : s.getPorts()) {
                int[] pos = s.portWorldPos(p, tileSize);
                int dx = pos[0] - worldX;
                int dy = pos[1] - worldY;
                int dist2 = dx * dx + dy * dy;
                if (dist2 < closestDist) {
                    closestDist = dist2;
                    closest = p;
                }
            }
        }
        return closest;
    }

    public ConveyorBelt findBeltWithSource(Port source) {
        for (ConveyorBelt b : belts) {
            if (b.getSource() == source) return b;
        }
        return null;
    }

    public List<Structure>    getStructures() { return Collections.unmodifiableList(structures); }
    public List<ConveyorBelt> getBelts()      { return Collections.unmodifiableList(belts); }
}
