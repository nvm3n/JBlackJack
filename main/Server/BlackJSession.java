package main.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import main.Shared.Card;
import main.Shared.ClientPacket;
import main.Shared.GameState;
import main.Shared.PacketContext;
import main.Shared.Queue;

class BlackJSession {

    int id;
    BlackJServer server;
    ArrayList<Integer> connectedPlayerIds;
    Map<Integer, String> connectedPlayerNames;

    boolean launched;

    ArrayList<Card> deck;
    Card hiddenCard;

    Random random = new Random();

    GameState gameState;

    String[] activePlayers;
    boolean[] standing;
    boolean[] next;

    Queue<PacketContext<ClientPacket>> packetQueue;

    BlackJSession(int id, BlackJServer parent) {
        this.id = id;

        server = parent;
        connectedPlayerIds = new ArrayList<Integer>();
        connectedPlayerNames = new HashMap<Integer, String>();
        packetQueue = new Queue<PacketContext<ClientPacket>>();
    }

    public void addPlayer(int id, String name) {
        connectedPlayerIds.add(id);
        connectedPlayerNames.put(connectedPlayerIds.indexOf(id), name);
        server.sendState(id, gameState);
    }

    public void removePlayer(int id) {
        gameState.players.removeIf((GameState.Player player) -> (player.name.equals(connectedPlayerNames.get(id))));
        for (int i = 0; i < activePlayers.length; i++) {
            if (activePlayers[i].equals(connectedPlayerNames.get(id))) {
                activePlayers[i] = null;
                standing[i] = true;
                next[i] = true;
            }
        }
        connectedPlayerNames.remove(id);
        connectedPlayerIds.remove(id);
    }

    public void addPacket(PacketContext<ClientPacket> packet) {
        packetQueue.enqueue(packet);
    }

    public void update() {
        if (launched) {
            boolean change = false;
            while (!packetQueue.isEmpty()) {
                change = true;
                PacketContext<ClientPacket> packetContext = packetQueue.front();
                packetQueue.dequeue();
                switch (packetContext.packet.type) {
                    case "playTurn":
                        playTurn(packetContext.packet.content, packetContext.id);
                        break;
                    default:
                        break;
                }
            }

            if (allTrue(standing)) {
                gameState.allStand = true;
                playDealer();
            }

            if (allTrue(next)) {
                gameState.active = false;
                launched = false;
            }

            if (change) {
                for (int id : connectedPlayerIds) {
                    server.sendState(id, gameState);
                }
            }
        } else {
            if (connectedPlayerIds.size() >= 2) {
                launchGame();
            }
        }
    }

    public Integer[] getConnectedPlayers() {
        return (Integer[]) connectedPlayerIds.toArray();
    }

    public boolean containsPlayer(int id) {
        return connectedPlayerIds.contains(id);
    }

    public void launchGame() {
        buildDeck();
        shuffleDeck();

        activePlayers = (String[]) connectedPlayerNames.values().toArray();
        standing = new boolean[connectedPlayerNames.size()];

        packetQueue = new Queue<PacketContext<ClientPacket>>();

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
        }

        launched = true;

        System.out.println(GameState.convertToString(gameState));
        System.out.println(GameState.convertFromString(GameState.convertToString(gameState)));
    }

    public void playTurn(String turn, int id) {
        if (turn.equals("hit")) {
            GameState.Player player = null;
            for (int i = 0; i < gameState.players.size(); i++) {
                if (gameState.players.get(i).name == connectedPlayerNames.get(id)) {
                    player = gameState.players.get(i);
                }

            }
            if (player != null) {
                Card card = deck.remove(deck.size() - 1);
                player.pSum += card.getValue();
                player.pAceCount += card.isAce() ? 1 : 0;
                player.pHand.add(card);
                if (psumreduce(player) > 21) {
                    turn = "stand";
                }
            }
        }
        if (turn.equals("stand")) {
            String playerName = connectedPlayerNames.get(id);
            for (int i = 0; i < activePlayers.length; i++) {
                if (activePlayers[i].equals(playerName)) {
                    standing[i] = true;
                }
            }
        }
        if (turn.equals("next")) {
            String playerName = connectedPlayerNames.get(id);
            for (int i = 0; i < activePlayers.length; i++) {
                if (activePlayers[i].equals(playerName)) {
                    next[i] = true;
                }
            }
        }
    }

    public void playDealer() {
        Card hCard = hiddenCard;
        gameState.dSum += hCard.getValue();
        gameState.dAceCount += hCard.isAce() ? 1 : 0;
        gameState.dHand.add(hCard);

        while (gameState.dSum < 17) {
            Card card = deck.remove(deck.size() - 1);
            gameState.dSum += card.getValue();
            gameState.dAceCount += card.isAce() ? 1 : 0;
            gameState.dHand.add(card);
        }

    }

    public String getDescriptor() {
        String s = "";
        for (String name : connectedPlayerNames.values()) {
            s += name + " ";
        }
        s += connectedPlayerNames.size() + "/4";
        return s;
    }

    // deck creation
    private void buildDeck() {
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

    private void shuffleDeck() {
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

    private int psumreduce(GameState.Player p) {
        while (p.pSum > 21 && p.pAceCount > 0) {
            p.pSum -= 10;
            p.pAceCount -= 1;
        }
        return p.pSum;
    }

    private boolean allTrue(boolean[] bA) {
        for (boolean b : bA) {
            if (b == false)
                return false;
        }
        return true;
    }
}