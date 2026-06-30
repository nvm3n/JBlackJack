package main.Client;

import main.Shared.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

public class GameClient {

    Game game;
    String playerName;
    boolean inGame = false;
    Thread statePoller;

    // game window
    int boardWidth = 600;
    int boadHeight = 600;

    int cardWidth = 64 * 2;
    int cardHeight = 96 * 2;

    JFrame frame = new JFrame("Black Jack");
    JPanel mainPanel = new JPanel(new CardLayout());
    JPanel gamePanel;
    JPanel lobbyPanel;

    JPanel inputPanel = new JPanel();
    JTable sessionTable;
    DefaultTableModel sessionTableModel;
    JButton hitButton = new JButton("Hit");
    JButton standButton = new JButton("Stand");
    JButton nextButton = new JButton("Next Hand");
    JButton leaveButton = new JButton("Leave Session");
    Timer sessionTimer;
    String lastSessionList = "";

    GameClient(String serverIP, int port, String name) {
        this.playerName = name;
        game = new Game(serverIP, port);

        frame.setSize(boardWidth, boadHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create lobby panel
        buildLobbyPanel();

        // Create game panel
        buildGamePanel();

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);
        inputPanel.setVisible(false);

        // Connect and set name
        game.setName(playerName);

        // Request initial session list
        game.requestSessions();

        // Start state polling thread
        startStatePoller();

        // Make frame visible AFTER all components are added
        frame.setVisible(true);
        frame.revalidate();
    }

    private void buildLobbyPanel() {
        lobbyPanel = new JPanel();
        lobbyPanel.setLayout(new BoxLayout(lobbyPanel, BoxLayout.Y_AXIS));
        lobbyPanel.setBackground(new Color(6, 69, 7));

        JLabel titleLabel = new JLabel("JBlackJack v0.0.1");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        lobbyPanel.add(titleLabel);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel nameLabel = new JLabel("Player: " + playerName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lobbyPanel.add(nameLabel);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel sessionsLabel = new JLabel("Active Sessions:");
        sessionsLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        sessionsLabel.setForeground(Color.WHITE);
        sessionsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lobbyPanel.add(sessionsLabel);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Create table model and table
        sessionTableModel = new DefaultTableModel(new String[]{"ID", "Players", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sessionTable = new JTable(sessionTableModel);
        sessionTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        sessionTable.setBackground(new Color(10, 80, 10));
        sessionTable.setForeground(Color.WHITE);
        sessionTable.setGridColor(new Color(6, 69, 7));
        sessionTable.setRowHeight(24);
        sessionTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        sessionTable.getTableHeader().setBackground(new Color(6, 69, 7));
        sessionTable.getTableHeader().setForeground(Color.WHITE);
        sessionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(sessionTable);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        lobbyPanel.add(scrollPane);
        lobbyPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(6, 69, 7));

        JButton refreshButton = new JButton("Refresh Sessions");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.requestSessions();
            }
        });
        buttonPanel.add(refreshButton);

        JButton joinButton = new JButton("Join Session");
        joinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = sessionTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) sessionTableModel.getValueAt(selectedRow, 0);
                    game.joinSession(id);
                } else {
                    String input = JOptionPane.showInputDialog(frame, "Enter session ID to join:");
                    if (input != null && !input.isEmpty()) {
                        try {
                            int id = Integer.parseInt(input);
                            game.joinSession(id);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(frame, "Invalid session ID");
                        }
                    }
                }
            }
        });
        buttonPanel.add(joinButton);

        JButton createButton = new JButton("Create New Session");
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.createSession();
            }
        });
        buttonPanel.add(createButton);

        lobbyPanel.add(buttonPanel);

        // Periodically request session list from server
        sessionTimer = new Timer(2000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                game.requestSessions();
            }
        });
        sessionTimer.start();

        mainPanel.add(lobbyPanel, "lobby");
    }

    private void buildGamePanel() {
        gamePanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);

                try {
                    int spacing = cardWidth - 85;

                    if (game.getGameState() == null) {
                        g.setFont(new Font("Arial", Font.PLAIN, 30));
                        g.setColor(Color.WHITE);
                        g.drawString("Waiting for players...", 150, 300);
                        return;
                    }

                    // Draw hidden card (or revealed if game is over)
                    Image hiddenCardImg;
                    boolean hideCard = true;
                    if (game.getGameState() != null) {
                        hideCard = game.getGameState().hiddenCard;
                    }
                    if (!hideCard && game.getHiddenCard() != null) {
                        hiddenCardImg = new ImageIcon(getClass().getResource(game.getHiddenCard().getImgPath()))
                                .getImage();
                    } else {
                        hiddenCardImg = new ImageIcon(getClass().getResource("./cardsprites/Card Back 1.png")).getImage();
                    }

                    // Get actual panel width for proper centering
                    int panelWidth = getWidth();
                    
                    // Draw dealer hand (centered on x-axis)
                    ArrayList<Card> dHand = game.getDHand();
                    // If hidden card was revealed, it's already drawn at position 0
                    // Skip index 0 in dHand to avoid drawing it twice
                    int startIndex = (!hideCard && dHand.size() > 0) ? 1 : 0;
                    int dealerVisibleCards = 1 + (dHand.size() - startIndex);
                    int dealerTotalWidth = cardWidth + (dealerVisibleCards - 1) * spacing;
                    int dealerStartX = (panelWidth - dealerTotalWidth) / 2;
                    g.drawImage(hiddenCardImg, dealerStartX, 60, cardWidth, cardHeight, null);
                    for (int i = startIndex; i < dHand.size(); i++) {
                        Card card = dHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                        g.drawImage(cardImg, dealerStartX + spacing * (i + 1 - startIndex), 60, cardWidth, cardHeight, null);
                    }

                    // Draw player hand (centered on x-axis)
                    ArrayList<Card> pHand = game.getPHand();
                    int playerTotalWidth = cardWidth + (pHand.size() - 1) * spacing;
                    int playerStartX = (panelWidth - playerTotalWidth) / 2;
                    for (int i = 0; i < pHand.size(); i++) {
                        Card card = pHand.get(i);
                        Image cardImg = new ImageIcon(getClass().getResource(card.getImgPath())).getImage();
                        g.drawImage(cardImg, playerStartX + spacing * i, 320, cardWidth, cardHeight, null);
                    }

                } catch (Exception e) {
                    System.err.println("Paint error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(6, 69, 7));

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

        leaveButton.setFocusable(false);
        leaveButton.setVisible(true);
        inputPanel.add(leaveButton);

        leaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                leaveSession();
            }
        });

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

        mainPanel.add(gamePanel, "game");
    }

    private void startStatePoller() {
        statePoller = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }

                    boolean stateChanged = game.pollState();

                    if (stateChanged && game.getGameState() != null && !inGame) {
                        // We received a GameState - switch to game view
                        inGame = true;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                sessionTimer.stop();
                                CardLayout cl = (CardLayout) mainPanel.getLayout();
                                cl.show(mainPanel, "game");
                                inputPanel.setVisible(true);
                                gamePanel.repaint();
                            }
                        });
                    }

                    if (inGame && stateChanged) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                gamePanel.repaint();
                                // Update button states based on server-provided player standing status
                                if (game.getGameState() != null && game.getGameState().players != null
                                        && game.getGameState().players.size() > 0) {
                                    boolean playerStanding = game.getGameState().players.get(0).standing;
                                    if (!game.getGameState().active) {
                                        // Game is over - show next button
                                        hitButton.setEnabled(false);
                                        hitButton.setVisible(false);
                                        standButton.setEnabled(false);
                                        standButton.setVisible(false);
                                        nextButton.setEnabled(true);
                                        nextButton.setVisible(true);
                                    } else if (playerStanding) {
                                        // This player has stood - show next button
                                        hitButton.setEnabled(false);
                                        hitButton.setVisible(false);
                                        standButton.setEnabled(false);
                                        standButton.setVisible(false);
                                        nextButton.setEnabled(true);
                                        nextButton.setVisible(true);
                                    } else {
                                        // Player's turn - show hit/stand
                                        hitButton.setEnabled(true);
                                        hitButton.setVisible(true);
                                        standButton.setEnabled(true);
                                        standButton.setVisible(true);
                                        nextButton.setEnabled(false);
                                        nextButton.setVisible(false);
                                    }
                                }
                            }
                        });
                    }

                    // Update session list display when received
                    if (!inGame && stateChanged && game.getSessionList() != null) {
                        final String list = game.getSessionList();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                updateSessionTable(list);
                            }
                        });
                    }
                }
            }
        });
        statePoller.start();
    }

    private void updateSessionTable(String list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // Only rebuild if the data actually changed, to preserve row selection
        if (list.equals(lastSessionList)) {
            return;
        }
        lastSessionList = list;

        // Save selected session ID to restore after rebuild
        int selectedId = -1;
        int selectedRow = sessionTable.getSelectedRow();
        if (selectedRow >= 0) {
            selectedId = (int) sessionTableModel.getValueAt(selectedRow, 0);
        }

        // Clear existing rows
        sessionTableModel.setRowCount(0);

        // Format: "Current active Sessions:>0: Player1 1/4>1: Player2 Player3 2/4"
        // or: "Currently no active Sessions"
        String[] parts = list.split(">");

        // parts[0] is the header text, skip it
        for (int i = 1; i < parts.length; i++) {
            String entry = parts[i].trim();
            if (entry.isEmpty()) continue;

            // Format: "id: playerNames count/max"
            int colonIdx = entry.indexOf(": ");
            if (colonIdx < 0) continue;

            String idStr = entry.substring(0, colonIdx);
            String rest = entry.substring(colonIdx + 2).trim();

            // rest is like "Player1 1/4" or "Player2 Player3 2/4"
            // Find the last space before the count/max pattern
            int lastSpace = rest.lastIndexOf(" ");
            if (lastSpace < 0) continue;

            String players = rest.substring(0, lastSpace);
            String status = rest.substring(lastSpace + 1).trim();

            try {
                int id = Integer.parseInt(idStr);
                sessionTableModel.addRow(new Object[]{id, players, status});
            } catch (NumberFormatException e) {
                // Skip malformed entries
            }
        }

        // Restore selection if the previously selected session still exists
        if (selectedId >= 0) {
            for (int row = 0; row < sessionTableModel.getRowCount(); row++) {
                if ((int) sessionTableModel.getValueAt(row, 0) == selectedId) {
                    sessionTable.setRowSelectionInterval(row, row);
                    break;
                }
            }
        }
    }

    public void stand() {
        System.out.println("clientplayer stand");
        game.stand();
        hitButton.setEnabled(false);
        hitButton.setVisible(false);
        standButton.setEnabled(false);
        standButton.setVisible(false);
        nextButton.setEnabled(true);
        nextButton.setVisible(true);
    }

    public void hit() {
        System.out.println("clientplayer hit");
        game.hit();
    }

    public void leaveSession() {
        System.out.println("client leaving session");
        game.quitSession();
        inGame = false;
        game.gameState = null;
        game.sessionList = ""; // Also clear session list to prevent stale data
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                sessionTimer.start();
                CardLayout cl = (CardLayout) mainPanel.getLayout();
                cl.show(mainPanel, "lobby");
                inputPanel.setVisible(false);
                // Reset button states for next game
                hitButton.setEnabled(true);
                hitButton.setVisible(true);
                standButton.setEnabled(true);
                standButton.setVisible(true);
                nextButton.setEnabled(false);
                nextButton.setVisible(false);
                game.requestSessions();
            }
        });
    }

    public void next() {
        game.next();
        hitButton.setEnabled(true);
        hitButton.setVisible(true);
        standButton.setEnabled(true);
        standButton.setVisible(true);
        nextButton.setEnabled(false);
        nextButton.setVisible(false);
    }

    //WIESO hab ich den schmarn mit swing gemacht ._.

}