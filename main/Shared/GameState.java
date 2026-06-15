package main.Shared;
import java.util.ArrayList;

public class GameState {

    // dealer
    public boolean hiddenCard;
    public ArrayList<Card> dHand;
    public int dSum;
    public int dAceCount;

    // Client player
    public ArrayList<Player> players;

    public class Player {
        String name;
        ArrayList<Card> pHand;
        int pSum;
        int pAceCount;
    }

    GameState() {

    }

    // TODO Test string conversion and reversion

    public static String convertToString(GameState gameState) {
        StringBuilder sb = new StringBuilder();

        // Dealer data
        sb.append("DEALER:").append(gameState.hiddenCard ? "1" : "0").append(":");
        sb.append(gameState.dSum).append(":").append(gameState.dAceCount).append(":");
        for (Card card : gameState.dHand) {
            sb.append(card.toString()).append(",");
        }
        sb.append("|");

        // Player data
        for (Player player : gameState.players) {
            sb.append("PLAYER:").append(player.name).append(":");
            sb.append(player.pSum).append(":").append(player.pAceCount).append(":");
            for (Card card : player.pHand) {
                sb.append(card.toString()).append(",");
            }
            sb.append("|");
        }

        return sb.toString();
    }

    public static GameState convertFromString(String data) {
        GameState gameState = new GameState();
        gameState.players = new ArrayList<>();

        String[] sections = data.split("\\|");
        for (String section : sections) {
            if (section.startsWith("DEALER:")) {
                String[] dealerData = section.substring(7).split(":");
                gameState.hiddenCard = dealerData[0].equals("1");
                gameState.dSum = Integer.parseInt(dealerData[1]);
                gameState.dAceCount = Integer.parseInt(dealerData[2]);
                String[] cards = dealerData[3].split(",");
                gameState.dHand = new ArrayList<>();
                for (String card : cards) {
                    if (!card.isEmpty()) {
                        gameState.dHand.add(new Card(card)); // Assuming Card has a constructor that takes a string
                    }
                }
            } else if (section.startsWith("PLAYER:")) {
                String[] playerData = section.substring(7).split(":");
                Player player = gameState.new Player();
                player.name = playerData[0];
                player.pSum = Integer.parseInt(playerData[1]);
                player.pAceCount = Integer.parseInt(playerData[2]);
                String[] cards = playerData[3].split(",");
                player.pHand = new ArrayList<>();
                for (String card : cards) {
                    if (!card.isEmpty()) {
                        player.pHand.add(new Card(card)); // Assuming Card has a constructor that takes a string
                    }
                }
                gameState.players.add(player);
            }
        }

        return gameState;
    }
}
