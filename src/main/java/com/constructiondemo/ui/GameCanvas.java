package com.constructiondemo.ui;

import com.constructiondemo.model.*;
import com.constructiondemo.model.structures.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;

public class GameCanvas extends JPanel {

    public enum Tool { SELECT, PLACE, BELT, DELETE }

    private static final int TILE_SIZE = 64;
    private static final int PORT_RADIUS = 7;
    private static final int PORT_DETECT_RADIUS = 20;
    private static final int GRID_EXTENT = 50; // tiles in each direction

    private final GameWorld world;

    // Camera
    private double camX = 0, camY = 0;
    private double zoom = 1.0;

    // Interaction
    private Tool currentTool = Tool.SELECT;
    private String placeType = null;
    private Object placeParam = null;

    // Selection
    private Structure selectedStructure = null;

    // Belt in progress
    private Port beltStartPort = null;
    private Point mousePos = new Point(0, 0);

    // Panning
    private Point panStart = null;
    private double panCamX, panCamY;

    // Ghost structure for placement preview
    private int ghostGridX = -1, ghostGridY = -1;

    // Callback
    private Runnable onSelectionChanged;

    public GameCanvas(GameWorld world) {
        this.world = world;
        setBackground(new Color(30, 30, 30));
        setPreferredSize(new Dimension(900, 700));

        MouseAdapter mouse = buildMouseAdapter();
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(e -> {
            double factor = e.getWheelRotation() < 0 ? 1.1 : 0.9;
            zoom = Math.max(0.3, Math.min(3.0, zoom * factor));
            repaint();
        });
    }

    // ── Painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform orig = g.getTransform();
        g.translate(-camX, -camY);
        g.scale(zoom, zoom);

        drawGrid(g);
        drawBelts(g);
        drawStructures(g);
        drawBeltInProgress(g);
        drawGhost(g);

        g.setTransform(orig);
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(50, 50, 50));
        g.setStroke(new BasicStroke(0.5f));
        int tileScreenSize = (int)(TILE_SIZE * zoom);
        if (tileScreenSize < 8) return; // too small to draw

        // visible world bounds
        int[] tl = screenToWorld(0, 0);
        int[] br = screenToWorld(getWidth(), getHeight());
        int wx0 = tl[0], wy0 = tl[1];
        int wx1 = br[0], wy1 = br[1];

        int gx0 = (int)Math.floor((double)wx0 / TILE_SIZE) - 1;
        int gx1 = (int)Math.ceil ((double)wx1 / TILE_SIZE) + 1;
        int gy0 = (int)Math.floor((double)wy0 / TILE_SIZE) - 1;
        int gy1 = (int)Math.ceil ((double)wy1 / TILE_SIZE) + 1;

        for (int gx = gx0; gx <= gx1; gx++) {
            int px = gx * TILE_SIZE;
            g.drawLine(px, gy0 * TILE_SIZE, px, gy1 * TILE_SIZE);
        }
        for (int gy = gy0; gy <= gy1; gy++) {
            int py = gy * TILE_SIZE;
            g.drawLine(gx0 * TILE_SIZE, py, gx1 * TILE_SIZE, py);
        }

        // Draw origin marker
        g.setColor(new Color(80, 80, 80));
        g.fillRect(-2, -2, 4, 4);
    }

    private void drawBelts(Graphics2D g) {
        for (ConveyorBelt belt : world.getBelts()) {
            int[] src = belt.getSourceStructure().portWorldPos(belt.getSource(), TILE_SIZE);
            int[] dst = belt.getDestinationStructure().portWorldPos(belt.getDestination(), TILE_SIZE);

            g.setColor(new Color(200, 180, 50));
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(src[0], src[1], dst[0], dst[1]);

            // Arrow head at destination
            drawArrow(g, src[0], src[1], dst[0], dst[1]);

            // Show queued items as dots
            int itemCount = belt.getSource().itemCount();
            if (itemCount > 0) {
                ItemType item = belt.getSource().peekItem();
                Color itemColor = item != null ? item.getColor() : Color.WHITE;
                double steps = Math.max(1, itemCount + 1);
                for (int i = 0; i < Math.min(itemCount, 4); i++) {
                    double t = (i + 1.0) / steps;
                    int ix = (int)(src[0] + t * (dst[0] - src[0]));
                    int iy = (int)(src[1] + t * (dst[1] - src[1]));
                    g.setColor(itemColor);
                    g.fillOval(ix - 4, iy - 4, 8, 8);
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(1f));
                    g.drawOval(ix - 4, iy - 4, 8, 8);
                }
            }
        }
    }

    private void drawArrow(Graphics2D g, int x1, int y1, int x2, int y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int ax = (x1 + x2) / 2;
        int ay = (y1 + y2) / 2;
        int len = 8;
        double spread = Math.toRadians(30);
        int bx1 = ax - (int)(Math.cos(angle - spread) * len);
        int by1 = ay - (int)(Math.sin(angle - spread) * len);
        int bx2 = ax - (int)(Math.cos(angle + spread) * len);
        int by2 = ay - (int)(Math.sin(angle + spread) * len);
        g.setColor(new Color(255, 220, 60));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(ax, ay, bx1, by1);
        g.drawLine(ax, ay, bx2, by2);
    }

    private void drawStructures(Graphics2D g) {
        for (Structure s : world.getStructures()) {
            int x = s.worldX(TILE_SIZE);
            int y = s.worldY(TILE_SIZE);
            int w = s.getWidthTiles()  * TILE_SIZE;
            int h = s.getHeightTiles() * TILE_SIZE;

            // Body
            g.setColor(s.getColor());
            g.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 10, 10);

            // Selection highlight
            if (s == selectedStructure) {
                g.setColor(Color.YELLOW);
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 10, 10);
            } else {
                g.setColor(s.getColor().darker());
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 10, 10);
            }

            // Label
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            String label = s.getName();
            int lx = x + (w - fm.stringWidth(label)) / 2;
            int ly = y + h / 2 + fm.getAscent() / 2 - 4;
            g.drawString(label, lx, ly);

            // Ports
            drawPorts(g, s);
        }
    }

    private void drawPorts(Graphics2D g, Structure s) {
        for (Port p : s.getPorts()) {
            int[] pos = s.portWorldPos(p, TILE_SIZE);
            Color portColor = p.getType() == Port.PortType.INPUT
                    ? new Color(80, 160, 255)
                    : new Color(255, 140, 0);

            // Highlight port being connected from
            if (p == beltStartPort) portColor = Color.YELLOW;

            g.setColor(portColor);
            g.fillOval(pos[0] - PORT_RADIUS, pos[1] - PORT_RADIUS,
                        PORT_RADIUS * 2, PORT_RADIUS * 2);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(pos[0] - PORT_RADIUS, pos[1] - PORT_RADIUS,
                        PORT_RADIUS * 2, PORT_RADIUS * 2);

            // Item type label
            if (p.getItemType() != null) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.PLAIN, 8));
                String typeLabel = p.getItemType().getDisplayName().substring(0, Math.min(4, p.getItemType().getDisplayName().length()));
                g.drawString(typeLabel, pos[0] - PORT_RADIUS, pos[1] - PORT_RADIUS - 2);
            }
        }
    }

    private void drawBeltInProgress(Graphics2D g) {
        if (beltStartPort == null || mousePos == null) return;
        int[] src = beltStartPort.getOwner().portWorldPos(beltStartPort, TILE_SIZE);
        int[] worldMouse = screenToWorld(mousePos.x, mousePos.y);
        g.setColor(new Color(255, 255, 0, 180));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1f, new float[]{6, 4}, 0f));
        g.drawLine(src[0], src[1], worldMouse[0], worldMouse[1]);
    }

    private void drawGhost(Graphics2D g) {
        if (currentTool != Tool.PLACE || placeType == null) return;
        if (ghostGridX < 0 || ghostGridY < 0) return;

        // Determine size from type
        int gw = 2, gh = 2;

        boolean blocked = false;
        for (int dx = 0; dx < gw; dx++) {
            for (int dy = 0; dy < gh; dy++) {
                if (world.getStructureAt(ghostGridX + dx, ghostGridY + dy) != null) {
                    blocked = true;
                    break;
                }
            }
        }

        g.setColor(blocked
                ? new Color(255, 80, 80, 100)
                : new Color(80, 255, 80, 100));
        g.fillRoundRect(ghostGridX * TILE_SIZE + 2, ghostGridY * TILE_SIZE + 2,
                gw * TILE_SIZE - 4, gh * TILE_SIZE - 4, 10, 10);
        g.setColor(blocked ? Color.RED : Color.GREEN);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(ghostGridX * TILE_SIZE + 2, ghostGridY * TILE_SIZE + 2,
                gw * TILE_SIZE - 4, gh * TILE_SIZE - 4, 10, 10);
    }

    // ── Mouse Handling ────────────────────────────────────────────────────────

    private MouseAdapter buildMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isMiddleMouseButton(e) ||
                    (SwingUtilities.isLeftMouseButton(e) && e.isAltDown())) {
                    panStart = e.getPoint();
                    panCamX = camX;
                    panCamY = camY;
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handleLeftClick(e.getPoint());
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    handleRightClick(e.getPoint());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (panStart != null) {
                    camX = panCamX + (panStart.x - e.getX());
                    camY = panCamY + (panStart.y - e.getY());
                    repaint();
                } else {
                    mousePos = e.getPoint();
                    if (currentTool == Tool.PLACE) updateGhost(e.getPoint());
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = e.getPoint();
                if (currentTool == Tool.PLACE) updateGhost(e.getPoint());
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                panStart = null;
            }
        };
    }

    private void handleLeftClick(Point screenPoint) {
        int[] wp = screenToWorld(screenPoint.x, screenPoint.y);
        int gx = (int)Math.floor((double)wp[0] / TILE_SIZE);
        int gy = (int)Math.floor((double)wp[1] / TILE_SIZE);

        switch (currentTool) {
            case PLACE:
                placeStructure(gx, gy);
                break;
            case DELETE:
                Structure ds = world.getStructureAt(gx, gy);
                if (ds != null) {
                    if (ds == selectedStructure) { selectedStructure = null; fireSelectionChanged(); }
                    world.removeStructure(ds);
                    repaint();
                }
                break;
            case BELT:
                Port port = world.findPortNear(wp[0], wp[1], TILE_SIZE, PORT_DETECT_RADIUS);
                if (port == null) {
                    beltStartPort = null;
                } else if (beltStartPort == null) {
                    if (port.getType() == Port.PortType.OUTPUT) beltStartPort = port;
                } else {
                    if (port.getType() == Port.PortType.INPUT && port != beltStartPort) {
                        ConveyorBelt belt = new ConveyorBelt(beltStartPort, port);
                        world.addBelt(belt);
                        beltStartPort = null;
                    } else {
                        beltStartPort = null;
                    }
                }
                repaint();
                break;
            case SELECT:
                selectedStructure = world.getStructureAt(gx, gy);
                fireSelectionChanged();
                repaint();
                break;
        }
    }

    private void handleRightClick(Point screenPoint) {
        if (currentTool == Tool.BELT) {
            beltStartPort = null;
            repaint();
            return;
        }
        int[] wp = screenToWorld(screenPoint.x, screenPoint.y);
        int gx = (int)Math.floor((double)wp[0] / TILE_SIZE);
        int gy = (int)Math.floor((double)wp[1] / TILE_SIZE);
        Structure s = world.getStructureAt(gx, gy);
        if (s != null) {
            if (s == selectedStructure) { selectedStructure = null; fireSelectionChanged(); }
            world.removeStructure(s);
            repaint();
        }
    }

    private void placeStructure(int gx, int gy) {
        if (placeType == null) return;
        Structure s = createStructure(placeType, placeParam);
        if (s == null) return;
        s.setGridX(gx);
        s.setGridY(gy);
        if (world.addStructure(s)) repaint();
    }

    private Structure createStructure(String type, Object param) {
        switch (type) {
            case "Miner":
                ItemType ore = (param instanceof ItemType) ? (ItemType) param : ItemType.IRON_ORE;
                return new MinerStructure(ore);
            case "Smelter_Iron":
                return new SmelterStructure(ItemType.IRON_ORE, ItemType.IRON_INGOT);
            case "Smelter_Copper":
                return new SmelterStructure(ItemType.COPPER_ORE, ItemType.COPPER_INGOT);
            case "Constructor":
                ConstructorStructure.Recipe recipe = (param instanceof ConstructorStructure.Recipe)
                        ? (ConstructorStructure.Recipe) param : ConstructorStructure.Recipe.IRON_PLATE;
                return new ConstructorStructure(recipe);
            case "Storage":
                return new StorageStructure();
            default:
                return null;
        }
    }

    private void updateGhost(Point screenPoint) {
        int[] wp = screenToWorld(screenPoint.x, screenPoint.y);
        ghostGridX = (int)Math.floor((double)wp[0] / TILE_SIZE);
        ghostGridY = (int)Math.floor((double)wp[1] / TILE_SIZE);
    }

    // ── Coordinate helpers ────────────────────────────────────────────────────

    /** Screen pixel → world pixel */
    private int[] screenToWorld(int sx, int sy) {
        return new int[]{ (int)((sx + camX) / zoom), (int)((sy + camY) / zoom) };
    }

// ── Public API ────────────────────────────────────────────────────────────

    public void setTool(Tool tool)               { this.currentTool = tool; beltStartPort = null; repaint(); }
    public void setPlaceType(String type, Object param) { this.placeType = type; this.placeParam = param; }
    public Tool getCurrentTool()                 { return currentTool; }
    public Structure getSelectedStructure()      { return selectedStructure; }
    public void setOnSelectionChanged(Runnable r){ this.onSelectionChanged = r; }

    private void fireSelectionChanged() {
        if (onSelectionChanged != null) onSelectionChanged.run();
    }

    public void centerView() {
        camX = 0;
        camY = 0;
        zoom = 1.0;
        repaint();
    }
}
