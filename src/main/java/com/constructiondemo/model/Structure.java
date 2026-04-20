package com.constructiondemo.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class Structure {

    protected int gridX, gridY;
    protected final int widthTiles, heightTiles;
    protected final String name;
    protected final Color color;
    protected final List<Port> ports = new ArrayList<>();

    private String id;

    protected Structure(String name, Color color, int widthTiles, int heightTiles) {
        this.name = name;
        this.color = color;
        this.widthTiles = widthTiles;
        this.heightTiles = heightTiles;
    }

    protected void addPort(Port port) {
        port.setOwner(this);
        ports.add(port);
    }

    public abstract void tick();
    public abstract String getStatusText();

    public boolean occupiesTile(int x, int y) {
        return x >= gridX && x < gridX + widthTiles
            && y >= gridY && y < gridY + heightTiles;
    }

    // World-pixel top-left corner
    public int worldX(int tileSize) { return gridX * tileSize; }
    public int worldY(int tileSize) { return gridY * tileSize; }

    // World-pixel position of a port
    public int[] portWorldPos(Port port, int tileSize) {
        int[] rel = port.getRelativeScreenPos(tileSize);
        return new int[]{ worldX(tileSize) + rel[0], worldY(tileSize) + rel[1] };
    }

    public int getGridX()        { return gridX; }
    public int getGridY()        { return gridY; }
    public void setGridX(int x)  { this.gridX = x; }
    public void setGridY(int y)  { this.gridY = y; }
    public int getWidthTiles()   { return widthTiles; }
    public int getHeightTiles()  { return heightTiles; }
    public String getName()      { return name; }
    public Color getColor()      { return color; }
    public List<Port> getPorts() { return ports; }

    public List<Port> getInputPorts() {
        List<Port> in = new ArrayList<>();
        for (Port p : ports) if (p.getType() == Port.PortType.INPUT) in.add(p);
        return in;
    }

    public List<Port> getOutputPorts() {
        List<Port> out = new ArrayList<>();
        for (Port p : ports) if (p.getType() == Port.PortType.OUTPUT) out.add(p);
        return out;
    }

    public String getId()          { return id; }
    public void setId(String id)   { this.id = id; }
}
