package main.Shared;

public class ClientPacket {
    public String type;
    public String content;

    // possible types: getSessions, joinSession, playTurn, quitSession, test

    public ClientPacket(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public static String convertToString(ClientPacket packet) {
        return packet.type + ";" + packet.content;
    }

    public static ClientPacket convertFromString(String string) {
        System.out.println(string);
        String[] parts = string.split(";");
        for (String s : parts) {
            System.out.println(s);
        }
        if (parts.length == 2) {
            return new ClientPacket(parts[0], parts[1]);
        } else {
            return new ClientPacket(parts[0], "");
        }
    }
}