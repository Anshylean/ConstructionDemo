package com.constructiondemo.model;

import java.awt.Color;

public enum ItemType {
    IRON_ORE("Iron Ore", new Color(160, 82, 45)),
    IRON_INGOT("Iron Ingot", new Color(176, 196, 222)),
    IRON_PLATE("Iron Plate", new Color(100, 149, 237)),
    IRON_ROD("Iron Rod", new Color(70, 130, 180)),
    COPPER_ORE("Copper Ore", new Color(184, 115, 51)),
    COPPER_INGOT("Copper Ingot", new Color(218, 165, 32)),
    WIRE("Wire", new Color(255, 215, 0)),
    CABLE("Cable", new Color(255, 165, 0));

    private final String displayName;
    private final Color color;

    ItemType(String displayName, Color color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public Color getColor() { return color; }
}
