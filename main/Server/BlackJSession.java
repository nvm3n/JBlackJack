package main.Server;
import main.Shared.*;
import java.util.ArrayList;
import java.util.Random;

class BlackJSession {

    boolean launched;

    ArrayList<Card> deck;
    Card hiddenCard;

    Random random = new Random();

    GameState gameState;

    public void update(/* vielleicht erhaltene Pakete */) {
        // TODO Add game logic
    }

    public void launchGame() {
        buildDeck();
        shuffleDeck();

        gameState = new GameState();

        // dealerhand
        gameState.dHand = new ArrayList<Card>();
        gameState.dSum = 0;
        gameState.dAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        gameState.dSum += hiddenCard.getValue();
        gameState.dAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size() - 1);
        gameState.dSum += card.getValue();
        gameState.dAceCount += card.isAce() ? 1 : 0;
        gameState.dHand.add(card);

        // System.out.println("Dealer:");
        // System.out.println(hiddenCard);
        // System.out.println(gameState.dHand);
        // System.out.println(gameState.dSum);
        // System.out.println(gameState.dAceCount);

        // playerhand
        gameState.players = new ArrayList<GameState.Player>();

        for (GameState.Player player : gameState.players) {
            player.pHand = new ArrayList<Card>();
            player.pSum = 0;
            player.pAceCount = 0;

            for (int i = 0; i < 2; i++) {
                card = deck.remove(deck.size() - 1);
                player.pSum += card.getValue();
                player.pAceCount += card.isAce() ? 1 : 0;
                player.pHand.add(card);
            }

            // System.out.println("Player:");
            // System.out.println(player.pHand);
            // System.out.println(player.pSum);
            // System.out.println(player.pAceCount);
        }

        launched = true;

        System.out.println(GameState.convertToString(gameState));
        System.out.println(GameState.convertFromString(GameState.convertToString(gameState)));
    }

    // deck creation
    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] types = { "Hearts", "Spades", "Diamonds", "Clubs" };
        int[] values = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

        for (int IType = 0; IType < types.length; IType++) {
            for (int IVal = 0; IVal < values.length; IVal++) {
                Card card = new Card(types[IType], values[IVal]);
                deck.add(card);
            }

        }

        System.out.println("Deck:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("Shuffled:");
        System.out.println(deck);
    }
}