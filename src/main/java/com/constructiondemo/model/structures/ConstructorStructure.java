package com.constructiondemo.model.structures;

import com.constructiondemo.model.*;

import java.awt.Color;

/**
 * Takes inputCount of inputItem and produces outputCount of outputItem.
 * Default recipes: Iron Ingot → Iron Plate (3:2) or Iron Ingot → Iron Rod (1:4).
 */
public class ConstructorStructure extends Structure {

    public enum Recipe {
        IRON_PLATE("Iron Plate", ItemType.IRON_INGOT, 3, ItemType.IRON_PLATE, 2, 4),
        IRON_ROD  ("Iron Rod",   ItemType.IRON_INGOT, 1, ItemType.IRON_ROD,   4, 3),
        WIRE      ("Wire",       ItemType.COPPER_INGOT, 1, ItemType.WIRE,     2, 2),
        CABLE     ("Cable",      ItemType.WIRE,        2, ItemType.CABLE,     1, 4);

        public final String label;
        public final ItemType inputItem;
        public final int inputCount;
        public final ItemType outputItem;
        public final int outputCount;
        public final int craftTicks;

        Recipe(String label, ItemType in, int inCount, ItemType out, int outCount, int ticks) {
            this.label = label;
            this.inputItem = in;
            this.inputCount = inCount;
            this.outputItem = out;
            this.outputCount = outCount;
            this.craftTicks = ticks;
        }
    }

    private final Recipe recipe;
    private int progressTicks = 0;
    private boolean processing = false;

    private final Port inputPort;
    private final Port outputPort;

    public ConstructorStructure(Recipe recipe) {
        super("Constructor (" + recipe.label + ")", new Color(34, 139, 34), 2, 2);
        this.recipe = recipe;

        inputPort  = new Port(Port.PortType.INPUT,  recipe.inputItem,  Direction.WEST, 0.5, 8);
        outputPort = new Port(Port.PortType.OUTPUT, recipe.outputItem, Direction.EAST, 0.5, 8);
        addPort(inputPort);
        addPort(outputPort);
    }

    @Override
    public void tick() {
        if (!processing) {
            if (inputPort.itemCount() >= recipe.inputCount && !outputPort.isFull()) {
                for (int i = 0; i < recipe.inputCount; i++) inputPort.takeItem();
                processing = true;
                progressTicks = 0;
            }
        } else {
            progressTicks++;
            if (progressTicks >= recipe.craftTicks) {
                for (int i = 0; i < recipe.outputCount; i++) outputPort.addItem(recipe.outputItem);
                processing = false;
                progressTicks = 0;
            }
        }
    }

    @Override
    public String getStatusText() {
        String state = processing
            ? String.format("Crafting... %d/%d", progressTicks, recipe.craftTicks)
            : "Idle";
        return String.format("%dx %s → %dx %s | %s | In: %d  Out: %d",
            recipe.inputCount, recipe.inputItem.getDisplayName(),
            recipe.outputCount, recipe.outputItem.getDisplayName(),
            state, inputPort.itemCount(), outputPort.itemCount());
    }

    public Port getInputPort()  { return inputPort; }
    public Port getOutputPort() { return outputPort; }
    public Recipe getRecipe()   { return recipe; }
}
