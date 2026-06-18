package main.Shared;

import java.util.ArrayList;

public class GameState {

    public boolean active;
    public boolean allStand;

    // dealer
    public boolean hiddenCard;
    public ArrayList<Card> dHand;
    public int dSum;
    public int dAceCount;

    // Client player
    public ArrayList<Player> players;

    public class Player {
        public String name;
        public ArrayList<Card> pHand;
        public int pSum;
        public int pAceCount;
    }

    public GameState() {

    }

    public static String convertToString(GameState gameState) {
        if (gameState == null)
            return "";
        StringBuilder sb = new StringBuilder();

        // Add basic fields
        sb.append(gameState.active ? "true" : "false");
        sb.append("|");
        sb.append(gameState.allStand ? "true" : "false");
        sb.append("|");
        sb.append(gameState.hiddenCard ? "true" : "false");
        sb.append("|");

        // Add dealer hand
        sb.append("dHand:");
        for (int i = 0; i < gameState.dHand.size(); i++) {
            sb.append(gameState.dHand.get(i).toString());
            if (i < gameState.dHand.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("|");

        // Add dealer sum and ace count
        sb.append(gameState.dSum);
        sb.append("|");
        sb.append(gameState.dAceCount);
        sb.append("|");

        // Add players
        sb.append("players:");
        for (int i = 0; i < gameState.players.size(); i++) {
            Player p = gameState.players.get(i);
            sb.append(p.name);
            sb.append("<");
            for (int j = 0; j < p.pHand.size(); j++) {
                sb.append(p.pHand.get(j).toString());
                if (j < p.pHand.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("<");
            sb.append(p.pSum);
            sb.append("<");
            sb.append(p.pAceCount);
            if (i < gameState.players.size() - 1) {
                sb.append(":");
            }
        }

        return sb.toString();
    }

    public static GameState convertFromString(String data) {
        GameState gameState = new GameState();
        gameState.dHand = new ArrayList<>();
        gameState.players = new ArrayList<>();

        String[] parts = data.split("\\|");

        // Parse basic fields
        gameState.active = Boolean.parseBoolean(parts[0]);
        gameState.allStand = Boolean.parseBoolean(parts[1]);
        gameState.hiddenCard = Boolean.parseBoolean(parts[2]);

        // Parse dealer hand
        String dHandPart = parts[3];
        if (dHandPart.startsWith("dHand:")) {
            String handStr = dHandPart.substring(6);
            if (!handStr.isEmpty()) {
                String[] cards = handStr.split(",");
                for (String card : cards) {
                    gameState.dHand.add(new Card(card));
                }
            }
        }

        // Parse dealer sum and ace count
        gameState.dSum = Integer.parseInt(parts[4]);
        gameState.dAceCount = Integer.parseInt(parts[5]);

        // Parse players
        String playersPart = parts[6];
        if (playersPart.startsWith("players:")) {
            String playersStr = playersPart.substring(8);
            if (!playersStr.isEmpty()) {
                // Split by | to get each player's data
                String[] playerDataParts = playersStr.split(":");
                // Each player has 4 parts: name, hand, sum, aceCount
                for (int i = 0; i < playerDataParts.length; i++) {
                    String[] individualPlayerParts = playerDataParts[i].split("<");
                    Player p = gameState.new Player();
                    p.name = individualPlayerParts[0];

                    // Parse player hand
                    p.pHand = new ArrayList<Card>();
                    String handStr = individualPlayerParts[1];
                    if (!handStr.isEmpty()) {
                        String[] cards = handStr.split(",");
                        for (String card : cards) {
                            p.pHand.add(new Card(card));
                        }
                    }

                    // Parse player sum and ace count
                    p.pSum = Integer.parseInt(individualPlayerParts[2]);
                    p.pAceCount = Integer.parseInt(individualPlayerParts[3]);

                    gameState.players.add(p);

                }
            }
        }

        return gameState;
    }
}
