package com.constructiondemo.model;

public class ConveyorBelt {

    private final Port source;
    private final Port destination;

    public ConveyorBelt(Port source, Port destination) {
        if (source.getType() != Port.PortType.OUTPUT)
            throw new IllegalArgumentException("Source port must be OUTPUT");
        if (destination.getType() != Port.PortType.INPUT)
            throw new IllegalArgumentException("Destination port must be INPUT");
        this.source = source;
        this.destination = destination;
    }

    public void tick() {
        if (!source.isEmpty() && !destination.isFull()) {
            ItemType item = source.peekItem();
            if (destination.canAccept(item)) {
                source.takeItem();
                destination.addItem(item);
            }
        }
    }

    public Port getSource()      { return source; }
    public Port getDestination() { return destination; }

    public Structure getSourceStructure()      { return source.getOwner(); }
    public Structure getDestinationStructure() { return destination.getOwner(); }
}
