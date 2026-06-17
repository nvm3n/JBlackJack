package main.Client;

import main.Shared.*;
import java.util.ArrayList;
import java.util.Random;

public class Game {

    boolean ongoingState = true;

    ArrayList<Card> deck;
    Random random = new Random();

    // dealer
    Card hiddenCard;
    ArrayList<Card> dHand;
    int dSum;
    int dAceCount;

    // Client player
    boolean playerStanding = false;
    ArrayList<Card> pHand;
    int pSum;
    int pAceCount;

    Game() {
        System.out.println("Gameserver launched");
        buildDeck();
        shuffleDeck();
        launchGame();
    }
    
    public void launchGame() {
        //dealerhand
        dHand = new ArrayList<Card>();
        dSum = 0;
        dAceCount = 0;

        hiddenCard = drawcard();
        dAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = drawcard();
        dSum += card.getValue();
        dSum += hiddenCard.getValue();
        dAceCount += card.isAce() ? 1 : 0;
        dHand.add(card);

        System.out.println("Dealer:");
        System.out.println(hiddenCard);
        System.out.println(dHand);
        System.out.println(dSum);
        System.out.println(dAceCount);

        //playerhand
        pHand = new ArrayList<Card>();
        pSum = 0;
        pAceCount = 0;

        for (int i = 0; i < 2; i++){
            card = drawcard();
            pSum += card.getValue();
            pAceCount += card.isAce() ? 1 : 0;
            pHand.add(card);
        }

        System.out.println("Player:");
        System.out.println(pHand);
        System.out.println(pSum);
        System.out.println(pAceCount);
        
        ongoingState = true;
        playerStanding = false;
    }

    public Card drawcard() {
        Card card = deck.remove(deck.size() - 1);
        return card;
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

        System.out.println("Server Deck:");
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

        System.out.println("Server Shuffled:");
        System.out.println(deck);
    }

    public void stand() {
        System.out.println("server recieved clientplayer stand");

        while (dSum < 17) {
            Card card = drawcard();
            dSum += card.getValue();
            dAceCount += card.isAce() ? 1 : 0;
            dHand.add(card);
        }

        playerStanding = true;
        ongoingState = false;
    }

    public void hit() {
        System.out.println("server recieved clientplayer hit");
        if (!playerStanding && ongoingState) {
            Card card = drawcard();
            pSum += card.getValue();
            pAceCount += card.isAce() ? 1 : 0;
            pHand.add(card);
            if (psumreduce() > 21) {
                System.out.println("pSum: " + pSum);
                stand();
            }
        }
    }

    public void next() {
        System.out.println("Server recieved next");
        pHand.clear();
        dHand.clear();
        launchGame();
    }

    public int psumreduce() {
        while (pSum > 21 && pAceCount > 0) {
            pSum -= 10;
            pAceCount -= 1;
            System.out.println("pReduced");
        }
        System.out.println("pSum: " + pSum);
        return pSum;
    }

    public int dsumreduce() {
        while (dSum > 21 && dAceCount > 0) {
            dSum -= 10;
            dAceCount -= 1;
            System.out.println("dReduced");
        }
        System.out.println("dSum: " + dSum);
        return dSum;
    }

    public ArrayList<Card> getPHand() {
        return pHand;
    }

    public ArrayList<Card> getDHand() {
        return dHand;
    }

    public Card getHiddenCard() {
        if (playerStanding == true) {
            System.out.println("server returned hidden card");
            return hiddenCard;
        }
        System.out.println("server returned hidden card");
        return null;
    }

    public Boolean getOngoingState() {
        return ongoingState;
    }

}