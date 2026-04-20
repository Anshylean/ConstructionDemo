package com.constructiondemo.model;

import java.util.ArrayDeque;
import java.util.Queue;

public class Port {

    public enum PortType { INPUT, OUTPUT }

    private final PortType type;
    private final ItemType itemType;
    private final Direction side;
    // fractional offset along the structure edge (0.0 = start, 1.0 = end)
    private final double edgeOffset;
    private final int capacity;
    private final Queue<ItemType> buffer = new ArrayDeque<>();

    private Structure owner;

    public Port(PortType type, ItemType itemType, Direction side, double edgeOffset, int capacity) {
        this.type = type;
        this.itemType = itemType;
        this.side = side;
        this.edgeOffset = edgeOffset;
        this.capacity = capacity;
    }

    public boolean canAccept(ItemType item) {
        return type == PortType.INPUT && (itemType == null || itemType == item) && buffer.size() < capacity;
    }

    public boolean hasItem() {
        return !buffer.isEmpty();
    }

    public boolean isFull() {
        return buffer.size() >= capacity;
    }

    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    public int itemCount() {
        return buffer.size();
    }

    public boolean addItem(ItemType item) {
        if (buffer.size() < capacity) {
            buffer.offer(item);
            return true;
        }
        return false;
    }

    public ItemType takeItem() {
        return buffer.poll();
    }

    public ItemType peekItem() {
        return buffer.peek();
    }

    public void clearBuffer() {
        buffer.clear();
    }

    // Screen position relative to the structure's top-left world coordinate
    public int[] getRelativeScreenPos(int tileSize) {
        int sw = owner.getWidthTiles() * tileSize;
        int sh = owner.getHeightTiles() * tileSize;
        switch (side) {
            case WEST:  return new int[]{ 0,  (int)(sh * edgeOffset) };
            case EAST:  return new int[]{ sw, (int)(sh * edgeOffset) };
            case NORTH: return new int[]{ (int)(sw * edgeOffset), 0  };
            case SOUTH: return new int[]{ (int)(sw * edgeOffset), sh };
            default:    return new int[]{ 0, 0 };
        }
    }

    public PortType getType()      { return type; }
    public ItemType getItemType()  { return itemType; }
    public Direction getSide()     { return side; }
    public double getEdgeOffset()  { return edgeOffset; }
    public int getCapacity()       { return capacity; }
    public Structure getOwner()    { return owner; }
    public void setOwner(Structure owner) { this.owner = owner; }
}
