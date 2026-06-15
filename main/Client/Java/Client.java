package main.Client.Java;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Client {

    private class Card{
        String type;
        int value;

        Card(String pType, int pVal) {
            value = pVal;
            type = pType;
        }

        public int getValue(){
            if (value == 1){
                return 11;
            }else if(value >= 11){
                return 10;
            }else{
                return value;
            }
        }

        public boolean isAce(){
            return value == 1;
        }

        public String toString() {
            return type + " " + value;
        }

        public String getImgPath() {
            return "./cardsprites/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random();


    //dealer
    Card hiddenCard;
    ArrayList<Card> dHand;
    int dSum;
    int dAceCount;

    //Client player
    ArrayList<Card> pHand;
    int pSum;
    int pAceCount;

    //game window
    int boardWidth = 600;
    int boadHeight = 600;

    int cardWidth = 64*2;
    int cardHeight = 96*2;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel(){
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            
            try {
                System.out.println("Drawing paint component");
                int spacing = cardWidth - 85;
                System.out.println("Spacing: " + spacing);

                //draw hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cardsprites/Card Back 1.png")).getImage();
                if(!standButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImgPath())).getImage();
                }
                int x = 60;
                System.out.println("dhand xcoord calc h: " + x);
                g.drawImage(hiddenCardImg, 60, 60, cardWidth, cardHeight, null);

                //draw dealer hand
                for (int i = 0; i < dHand.size(); i++){
                    Card card = dHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                    x = 60 + spacing * (i+1);
                    System.out.println("dhand xcoord calc" + i + ": "+ x);
                    g.drawImage(cardImg, x, 60, cardWidth, cardHeight, null);
                }
                //draw clientplayer hand
                for (int i = 0; i < pHand.size(); i++){
                    Card card = pHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                    x = 60 + spacing * i;
                    System.out.println("phand xcoord calc" + i + ": " + x);
                    g.drawImage(cardImg, x , 320, cardWidth, cardHeight, null);
                }

                if(!standButton.isEnabled()){
                    dSum = dsumreduce();
                    pSum = psumreduce();
                    System.out.println("stand: ");
                    System.out.println(dSum);
                    System.out.println(pSum);
                    
                    String message = "";
                    if (pSum > 21){
                        message = "Dealer wins";
                    }else if(dSum > 21){
                        message = "Player wins";
                    }else if(pSum == dSum){
                        message = "Push";
                    }else if(pSum > dSum){
                        message = "Player wins";
                    }else{
                        message = "Dealer wins";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 45));
                    g.setColor(new Color(51, 24, 18));
                    g.drawString(message, 170, 300);
                }



            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel inputPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton standButton = new JButton("Stand");
    JButton nextButton = new JButton("Next Hand");




    Client() {
        launchGame();
        frame.setVisible(true);
        frame.setSize(boardWidth, boadHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //frame.setUndecorated(true);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(6, 69,7));
        frame.add(gamePanel);

        hitButton.setFocusable(false);
        hitButton.setVisible(true);
        inputPanel.add(hitButton);

        standButton.setFocusable(false);
        standButton.setVisible(true);
        inputPanel.add(standButton);
        
        nextButton.setFocusable(false);
        nextButton.setEnabled(false);
        nextButton.setVisible(false);
        inputPanel.add(nextButton);

        frame.add(inputPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hit();
            }
        });

        standButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                stand();
            }
        });

        nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});

        gamePanel.repaint();
    }

    public void launchGame() {
        buildDeck();
        shuffleDeck();


        //dealerhand
        dHand = new ArrayList<Card>();
        dSum = 0;
        dAceCount = 0;

        hiddenCard = deck.remove(deck.size()-1);
        dSum += hiddenCard.getValue();
        dAceCount += hiddenCard.isAce() ? 1 : 0;

        Card card = deck.remove(deck.size()-1);
        dSum += card.getValue();
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
            card = deck.remove(deck.size()-1);
            pSum += card.getValue();
            pAceCount += card.isAce() ? 1 :0;
            pHand.add(card);
        }

        System.out.println("Player:");
        System.out.println(pHand);
        System.out.println(pSum);
        System.out.println(pAceCount);
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
    
    public int psumreduce(){
        while (pSum > 21 && pAceCount > 0){
            pSum -= 10;
            pAceCount -= 1;
            System.out.println("pReduced");
        }
        System.out.println("pSum: " + pSum);
        return pSum;
    }

    public int dsumreduce(){
        while (dSum > 21 && dAceCount > 0){
            dSum -= 10;
            dAceCount -= 1;
            System.out.println("dReduced");
        }
        System.out.println("dSum: " + dSum);
        return dSum;
    }

    public void stand(){
        System.out.println("clientplayer stand");
        hitButton.setEnabled(false);
        hitButton.setVisible(false);
        standButton.setEnabled(false);
        standButton.setVisible(false);
        nextButton.setEnabled(true);
        nextButton.setVisible(true);
                
        while (dSum < 17 ){
            Card card = deck.remove(deck.size()-1);
            dSum += card.getValue();
            dAceCount += card.isAce()?1 : 0;
            dHand.add(card);
        }

        gamePanel.repaint();
    }

    public void hit(){
        System.out.println("clientplayer hit");
        Card card = deck.remove(deck.size()-1);
        pSum += card.getValue();
        pAceCount += card.isAce()? 1 : 0;
        pHand.add(card);
        if (psumreduce() > 21){
            System.out.println("pSum: " + pSum);
            stand();
        }

        gamePanel.repaint();
    }

    public void next(){
        pHand.clear();
		dHand.clear();
		gamePanel.repaint();
		hitButton.setEnabled(true);
        hitButton.setVisible(true);
		standButton.setEnabled(true);
        standButton.setVisible(true);
		nextButton.setEnabled(false);
        nextButton.setVisible(false);
		launchGame();
    }

}

