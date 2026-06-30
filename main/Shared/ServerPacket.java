package main.Shared;

public class ServerPacket {
    public String type;
    public GameState state;
    public String list;

    // possible types: GameState, SessionList, test

    public ServerPacket(String type, String list) {
        this.type = type;
        this.list = list;
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
        String[] parts = string.split(";");
        ServerPacket packet;
        if (parts[0].equals("GameState")) {
            packet = new ServerPacket(GameState.convertFromString(parts[1]));
            // Print a readable game state summary
            if (packet.state != null) {
                System.out.println("--- Game State ---");
                System.out.println("  active=" + packet.state.active + " | allStand=" + packet.state.allStand + " | hiddenCard=" + packet.state.hiddenCard);
                System.out.print("  Dealer: [");
                if (packet.state.hiddenCard) {
                    System.out.print("??");
                } else if (packet.state.dHand.size() > 0) {
                    System.out.print(packet.state.dHand.get(0));
                }
                for (int i = 1; i < packet.state.dHand.size(); i++) {
                    System.out.print(", " + packet.state.dHand.get(i));
                }
                System.out.println("] sum=" + packet.state.dSum);
                for (GameState.Player p : packet.state.players) {
                    System.out.print("  " + p.name + ": [");
                    for (int i = 0; i < p.pHand.size(); i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(p.pHand.get(i));
                    }
                    System.out.println("] sum=" + p.pSum + " standing=" + p.standing);
                }
                System.out.println("------------------");
            }
        } else if (parts.length < 2) {
            packet = new ServerPacket(parts[0], "");
        } else {
            packet = new ServerPacket(parts[0], parts[1]);
        }

        return packet;
    }
}