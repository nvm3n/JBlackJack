package main.Reference.Client;

import main.Shared.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client {

    Game game = new Game();

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
                if(game.getOngoingState() == false) {
                    try{
                    hiddenCardImg = new ImageIcon(getClass().getResource(game.getHiddenCard().getImgPath())).getImage();                       
                    }catch(Exception e){
                        System.out.println(e);
                    }
                }
                int x = 60;
                System.out.println("dhand xcoord calc h: " + x);
                g.drawImage(hiddenCardImg, 60, 60, cardWidth, cardHeight, null);

                //draw dealer hand
                for (int i = 0; i < game.getDHand().size(); i++){
                    Card card = game.getDHand().get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                    x = 60 + spacing * (i+1);
                    System.out.println("dhand xcoord calc" + i + ": "+ x);
                    g.drawImage(cardImg, x, 60, cardWidth, cardHeight, null);
                }
                //draw clientplayer hand
                for (int i = 0; i < game.getPHand().size(); i++){
                    Card card = game.getPHand().get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                    x = 60 + spacing * i;
                    System.out.println("phand xcoord calc" + i + ": " + x);
                    g.drawImage(cardImg, x , 320, cardWidth, cardHeight, null);
                }

                if(game.getOngoingState() == false){
                    int dSum = game.dsumreduce();
                    int pSum = game.psumreduce();
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

                if(!standButton.isEnabled() == true && game.getOngoingState() == true){
                    gamePanel.repaint();
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
        game.launchGame();
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

    public void stand(){
        System.out.println("clientplayer stand");
        game.stand();
        hitButton.setEnabled(false);
        hitButton.setVisible(false);
        standButton.setEnabled(false);
        standButton.setVisible(false);
        nextButton.setEnabled(true);
        nextButton.setVisible(true);

        gamePanel.repaint();
    }

    public void hit(){
        System.out.println("clientplayer hit");
        game.hit();
        gamePanel.repaint();
    }

    public void next(){
        game.next();
        gamePanel.repaint();
        hitButton.setEnabled(true);
        hitButton.setVisible(true);
        standButton.setEnabled(true);
        standButton.setVisible(true);
        nextButton.setEnabled(false);
        nextButton.setVisible(false);
    }
}

