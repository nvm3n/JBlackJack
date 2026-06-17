package main.Client;

import main.Shared.*;
import java.util.ArrayList;
import java.util.Random;

public class Game{

    ArrayList<Card> deck;
    Random random = new Random();

    //dealer
    Card hiddenCard;
    ArrayList<Card> dHand;
    int dSum;
    int dAceCount;

    //Client player
    boolean playerStanding;
    ArrayList<Card> pHand;
    int pSum;
    int pAceCount;

    Game(){
        buildDeck();
        shuffleDeck();
    }

    public Card drawcard() {
        Card card = deck.remove(deck.size()-1);
        return card;
    }

    //deck creation
    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] types = {"Hearts", "Spades", "Diamonds", "Clubs"};
        int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};

        for (int IType = 0; IType<types.length; IType++){
            for (int IVal = 0; IVal<values.length; IVal++){
                Card card = new Card(types[IType], values[IVal]);
                deck.add(card);
            }

        }

        System.out.println("Deck:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++){
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("Shuffled:");
        System.out.println(deck);
    }

    public ArrayList<Card> getPHand(){
        return pHand;
    }

    public ArrayList<Card> getDHand(){
        return dHand;
    }

    public Card getHiddenCard(){
        if (playerStanding == true){
            return hiddenCard;
        }
        return null;
    }


    
}