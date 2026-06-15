package main.Client;
import main.Shared.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class BlackJClient {
    // TODO GANZ GANZ Viel

    // wie du es gelassen hast nur das alles was vom server kommt in den gameState
    // getan wurde und es den client zum connecten gibt, du kannst davon ausgehen
    // das nach jedem zug irgendwann ein paket mit neuem status kommt bevor es
    // weitergeht. mit der methode isConnected vom Gameclient kannst du außerdem
    // sicherstellen das du die verbindung nicht verloren hast. Bis auf den
    // Konstruktor habe ich nichts verändert und es funktioniert noch nicht mit
    // Gamestate, viel Spaß!
    // MFG Lorentz


    GameState gameState;
    ClientConnection<GameState, String> gameClient;

    public boolean hiddenCard;
    public ArrayList<Card> dHand;
    public int dSum;
    public int dAceCount;

    String playerName;

    private Player self;

    public class Player {
        public String name;
        public ArrayList<Card> pHand;
        public int self.pSum;
        public int pAceCount;
    }

    // game window
    int boardWidth = 600;
    int boadHeight = 600;

    int cardWidth = 64 * 2;
    int cardHeight = 96 * 2;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                System.out.println("Drawing paint component");
                int spacing = cardWidth - 85;
                System.out.println("Spacing: " + spacing);

                // draw hidden card
                Image hiddenCardImg = new ImageIcon(getClass().getResource("./cardsprites/Card Back 1.png")).getImage();
                if (!standButton.isEnabled()) {
                    hiddenCardImg = new ImageIcon(getClass().getResource(hiddenCard.getImgPath())).getImage();
                }
                int x = 60;
                System.out.println("dhand xcoord calc h: " + x);
                g.drawImage(hiddenCardImg, 60, 60, cardWidth, cardHeight, null);

                // draw dealer hand
                for (int i = 0; i < dHand.size(); i++) {
                    Card card = dHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                    x = 60 + spacing * (i + 1);
                    System.out.println("dhand xcoord calc" + i + ": " + x);
                    g.drawImage(cardImg, x, 60, cardWidth, cardHeight, null);
                }
                // draw clientplayer hand
                for (int i = 0; i < pHand.size(); i++) {
                    Card card = pHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                    x = 60 + spacing * i;
                    System.out.println("phand xcoord calc" + i + ": " + x);
                    g.drawImage(cardImg, x, 320, cardWidth, cardHeight, null);
                }

                if (!standButton.isEnabled()) {
                    dSum = dsumreduce();
                    self.self.pSum = self.psumreduce();
                    System.out.println("stand: ");
                    System.out.println(dSum);
                    System.out.println(self.self.pSum);

                    String message = "";
                    if (self.pSum > 21) {
                        message = "Dealer wins";
                    } else if (dSum > 21) {
                        message = "Player wins";
                    } else if (self.pSum == dSum) {
                        message = "Push";
                    } else if (self.pSum > dSum) {
                        message = "Player wins";
                    } else {
                        message = "Dealer wins";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 45));
                    g.setColor(new Color(51, 24, 18));
                    g.drawString(message, 170, 300);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel inputPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton standButton = new JButton("Stand");
    JButton nextButton = new JButton("Next Hand");

    public BlackJClient(String ServerIP, int Port) {
        gameClient = new ClientConnection<GameState, String>(ServerIP, Port,
                (String state) -> GameState.convertFromString(state), (String s) -> s);

        frame.setVisible(true);
        frame.setSize(boardWidth, boadHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        // frame.setUndecorated(true);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(6, 69, 7));
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
            public void actionPerformed(ActionEvent e) {
                hit();
            }
        });

        standButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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

    public void stand() {
        System.out.println("clientplayer stand");
        hitButton.setEnabled(false);
        hitButton.setVisible(false);
        standButton.setEnabled(false);
        standButton.setVisible(false);
        nextButton.setEnabled(true);
        nextButton.setVisible(true);

        //TODO: implement client sending stand to server and waiting for response

        gamePanel.repaint();
    }

    public void hit() {
        System.out.println("clientplayer hit");
        
        //TODO: implement client sending hit to server and waiting for response

        gamePanel.repaint();
    }

    public void next() {
        self.pHand.clear();
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
