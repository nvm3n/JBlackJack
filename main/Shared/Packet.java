package main.Shared;

public abstract class Packet {

    Packet() {
    }

    public abstract String convertToString();

    public abstract void convertFromString(String string);

}
