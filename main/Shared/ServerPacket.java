package main.Shared;

public class ServerPacket {
    public String type;
    public GameState state;
    public String list;

    // possible types: GameState, SessionList, test

    public ServerPacket(String type, String list) {
        this.type = type;
        this.list = list.replaceAll(">", "\n");
        this.state = null;
    }

    public ServerPacket(GameState state) {
        this.type = "GameState";
        this.list = null;
        this.state = state;
    }

    public static String convertToString(ServerPacket packet) {
        if (packet.type.equals("GameState")) {
            if (packet.state == null)
                return packet.type + ";";
            return packet.type + ";" + GameState.convertToString(packet.state);
        } else {
            return packet.type + ";" + packet.list;
        }
    }

    public static ServerPacket convertFromString(String string) {
        System.out.println(string);
        String[] parts = string.split(";");
        // System.out.println(parts.length);
        // for (String s : parts) {
        // System.out.println(s);
        // }
        ServerPacket packet;
        if (parts[0] == "GameState") {
            packet = new ServerPacket(GameState.convertFromString(parts[1]));
        } else if (parts.length < 2) {
            // System.out.println(parts.length);
            packet = new ServerPacket(parts[0], "");
        } else {
            packet = new ServerPacket(parts[0], parts[1]);
        }

        return packet;
    }
}