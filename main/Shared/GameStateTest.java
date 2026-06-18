package main.Shared;

import java.util.ArrayList;
import java.util.Random;

public class GameStateTest {

    public static void main(String[] args) {
        // Run the test
        boolean passed = testConvertToStringAndFromString();
        if (passed) {
            System.out.println("Test passed!");
        } else {
            System.out.println("Test failed!");
        }
    }

    public static boolean testConvertToStringAndFromString() {
        // Create a random game state
        GameState original = createRandomGameState();

        // Convert to string
        String serialized = GameState.convertToString(original);

        System.out.println(serialized);

        // Convert back to GameState
        GameState deserialized = GameState.convertFromString(serialized);

        // Compare the two states
        return compareGameStates(original, deserialized);
    }

    private static GameState createRandomGameState() {
        GameState gameState = new GameState();
        gameState.active = true;
        gameState.allStand = false;
        gameState.hiddenCard = true;
        gameState.dHand = new ArrayList<>();
        gameState.players = new ArrayList<>();

        Random random = new Random();

        // Add random cards to dealer hand
        int dealerCardCount = random.nextInt(5) + 1; // 1 to 5 cards
        for (int i = 0; i < dealerCardCount; i++) {
            String type = getRandomCardType();
            int value = getRandomCardValue();
            gameState.dHand.add(new Card(type, value));
        }

        // Calculate dealer sum and ace count
        gameState.dSum = 0;
        gameState.dAceCount = 0;
        for (Card card : gameState.dHand) {
            gameState.dSum += card.getValue();
            if (card.isAce()) {
                gameState.dAceCount++;
            }
        }

        // Add random players
        int playerCount = random.nextInt(3) + 1; // 1 to 3 players
        for (int i = 0; i < playerCount; i++) {
            GameState.Player player = gameState.new Player();
            player.name = "Player" + i;
            player.pHand = new ArrayList<>();
            player.pSum = 0;
            player.pAceCount = 0;

            int playerCardCount = random.nextInt(5) + 1; // 1 to 5 cards
            for (int j = 0; j < playerCardCount; j++) {
                String type = getRandomCardType();
                int value = getRandomCardValue();
                player.pHand.add(new Card(type, value));
                player.pSum += new Card(type, value).getValue();
                if (new Card(type, value).isAce()) {
                    player.pAceCount++;
                }
            }

            gameState.players.add(player);
        }

        return gameState;
    }

    private static String getRandomCardType() {
        String[] types = { "Hearts", "Diamonds", "Clubs", "Spades" };
        Random random = new Random();
        return types[random.nextInt(types.length)];
    }

    private static int getRandomCardValue() {
        Random random = new Random();
        return random.nextInt(13) + 1; // 1 to 13
    }

    private static boolean compareGameStates(GameState gs1, GameState gs2) {
        // Compare basic fields
        if (gs1.active != gs2.active || gs1.allStand != gs2.allStand || gs1.hiddenCard != gs2.hiddenCard) {
            return false;
        }

        // Compare dealer hand
        if (gs1.dHand.size() != gs2.dHand.size()) {
            return false;
        }
        for (int i = 0; i < gs1.dHand.size(); i++) {
            if (!gs1.dHand.get(i).toString().equals(gs2.dHand.get(i).toString())) {
                return false;
            }
        }

        // Compare dealer sum and ace count
        if (gs1.dSum != gs2.dSum || gs1.dAceCount != gs2.dAceCount) {
            return false;
        }

        // Compare players
        if (gs1.players.size() != gs2.players.size()) {
            return false;
        }
        for (int i = 0; i < gs1.players.size(); i++) {
            GameState.Player p1 = gs1.players.get(i);
            GameState.Player p2 = gs2.players.get(i);

            if (!p1.name.equals(p2.name)) {
                return false;
            }

            if (p1.pHand.size() != p2.pHand.size()) {
                return false;
            }
            for (int j = 0; j < p1.pHand.size(); j++) {
                if (!p1.pHand.get(j).toString().equals(p2.pHand.get(j).toString())) {
                    return false;
                }
            }

            if (p1.pSum != p2.pSum || p1.pAceCount != p2.pAceCount) {
                return false;
            }
        }

        return true;
    }
}