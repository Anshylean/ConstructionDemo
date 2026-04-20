package com.constructiondemo.model.structures;

import com.constructiondemo.model.*;

import java.awt.Color;

public class SmelterStructure extends Structure {

    private final ItemType inputItem;
    private final ItemType outputItem;
    private static final int SMELT_TICKS = 3;

    private int progressTicks = 0;
    private boolean processing = false;

    private final Port inputPort;
    private final Port outputPort;

    public SmelterStructure(ItemType inputItem, ItemType outputItem) {
        super("Smelter", new Color(178, 34, 34), 2, 2);
        this.inputItem = inputItem;
        this.outputItem = outputItem;

        inputPort  = new Port(Port.PortType.INPUT,  inputItem,  Direction.WEST, 0.5, 5);
        outputPort = new Port(Port.PortType.OUTPUT, outputItem, Direction.EAST, 0.5, 5);
        addPort(inputPort);
        addPort(outputPort);
    }

    @Override
    public void tick() {
        if (!processing) {
            if (inputPort.hasItem() && !outputPort.isFull()) {
                inputPort.takeItem();
                processing = true;
                progressTicks = 0;
            }
        } else {
            progressTicks++;
            if (progressTicks >= SMELT_TICKS) {
                outputPort.addItem(outputItem);
                processing = false;
                progressTicks = 0;
            }
        }
    }

    @Override
    public String getStatusText() {
        String state = processing
            ? String.format("Smelting... %d/%d", progressTicks, SMELT_TICKS)
            : "Idle";
        return String.format("%s → %s | %s | In: %d  Out: %d",
            inputItem.getDisplayName(), outputItem.getDisplayName(),
            state, inputPort.itemCount(), outputPort.itemCount());
    }

    public Port getInputPort()  { return inputPort; }
    public Port getOutputPort() { return outputPort; }
}
