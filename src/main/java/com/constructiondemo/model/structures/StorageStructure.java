package com.constructiondemo.model.structures;

import com.constructiondemo.model.*;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

public class StorageStructure extends Structure {

    private static final int PORT_BUFFER = 10;
    private static final int STORAGE_CAPACITY = 200;

    private final Map<ItemType, Integer> inventory = new EnumMap<>(ItemType.class);
    private int totalItems = 0;

    private final Port inputPort;

    public StorageStructure() {
        super("Storage", new Color(100, 100, 200), 2, 2);

        inputPort = new Port(Port.PortType.INPUT, null, Direction.WEST, 0.5, PORT_BUFFER);
        addPort(inputPort);
    }

    @Override
    public void tick() {
        while (inputPort.hasItem() && totalItems < STORAGE_CAPACITY) {
            ItemType item = inputPort.takeItem();
            inventory.merge(item, 1, Integer::sum);
            totalItems++;
        }
    }

    @Override
    public String getStatusText() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Storage: %d/%d | ", totalItems, STORAGE_CAPACITY));
        inventory.forEach((k, v) -> sb.append(k.getDisplayName()).append(": ").append(v).append("  "));
        return sb.toString().trim();
    }

    public Map<ItemType, Integer> getInventory() { return inventory; }
    public int getTotalItems()                    { return totalItems; }
    public int getStorageCapacity()               { return STORAGE_CAPACITY; }
    public Port getInputPort()                    { return inputPort; }
}
