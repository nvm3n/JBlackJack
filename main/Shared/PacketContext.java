package main.Shared;

public class PacketContext<T> {

    public T packet;
    public int id;

    public PacketContext(T packet, int id) {
        this.packet = packet;
        this.id = id;
    }
}