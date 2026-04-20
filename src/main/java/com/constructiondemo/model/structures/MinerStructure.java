package com.constructiondemo.model.structures;

import com.constructiondemo.model.*;

import java.awt.Color;

public class MinerStructure extends Structure {

    private final ItemType produces;
    private int tickCounter = 0;
    private static final int PRODUCE_EVERY = 3;

    private final Port outputPort;

    public MinerStructure(ItemType produces) {
        super("Miner (" + produces.getDisplayName() + ")",
              new Color(139, 90, 43), 2, 2);
        this.produces = produces;

        outputPort = new Port(Port.PortType.OUTPUT, produces, Direction.EAST, 0.5, 5);
        addPort(outputPort);
    }

    @Override
    public void tick() {
        tickCounter++;
        if (tickCounter >= PRODUCE_EVERY) {
            tickCounter = 0;
            outputPort.addItem(produces);
        }
    }

    @Override
    public String getStatusText() {
        return String.format("Produces: %s | Buffer: %d/%d",
            produces.getDisplayName(), outputPort.itemCount(), outputPort.getCapacity());
    }

    public ItemType getProduces() { return produces; }
    public Port getOutputPort()   { return outputPort; }
}
