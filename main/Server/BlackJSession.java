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
    GameServer server;
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

    BlackJSession(int id, GameServer parent) {
        this.id = id;

        server = parent;
        connectedPlayerIds = new ArrayList<Integer>();
        connectedPlayerNames = new HashMap<Integer, String>();
        packetQueue = new Queue<PacketContext<ClientPacket>>();
    }

    public void addPlayer(int id, String name) {
        connectedPlayerIds.add(id);
        connectedPlayerNames.put(id, name);
        if (gameState != null) {
            server.sendState(id, gameState);
        }
    }

    public void removePlayer(int id) {
        String playerName = connectedPlayerNames.get(id);
        if (playerName != null) {
            if (gameState != null && gameState.players != null) {
                gameState.players.removeIf((GameState.Player player) -> (player.name.equals(playerName)));
            }
            if (activePlayers != null) {
                for (int i = 0; i < activePlayers.length; i++) {
                    if (activePlayers[i] != null && activePlayers[i].equals(playerName)) {
                        activePlayers[i] = null;
                        standing[i] = true;
                        next[i] = true;
                    }
                }
            }
            // If removing this player would leave too few players, immediately end the game
            if (connectedPlayerIds.size() <= 2 && gameState != null) {
                System.out.println("[" + this.id + "] Player " + id + " left - ending game for remaining players");
                gameState.active = false;
                launched = false;
                // Send final state to remaining players (not the one leaving)
                for (int pid : connectedPlayerIds) {
                    if (pid != id) {
                        server.sendState(pid, gameState);
                    }
                }
                // Clear state - prevents leaving player from re-entering game view
                gameState = null;
            }
        }
        connectedPlayerNames.remove(id);
        connectedPlayerIds.remove((Integer) id);
    }

    public void addPacket(PacketContext<ClientPacket> packet) {
        packetQueue.enqueue(packet);
    }

    public void update() {
        if (launched) {
            // GAME IS ACTIVE - process player packets
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

            // Check if all players have stood - if so, dealer plays
            // IMPORTANT: Only trigger once while hidden card is still hidden
            // Without this guard, the tight server loop calls playDealer()
            // thousands of times per second, adding the hidden card value
            // to dSum over and over = massive sum explosion
            if (allTrue(standing) && gameState.hiddenCard) {
                gameState.allStand = true;
                playDealer();
            }

            // Check if all players clicked "next" - game round is over
            if (allTrue(next)) {
                System.out.println("[" + id + "] All players clicked next - ending round");
                gameState.active = false;
                launched = false;
                // Send final state to clients before resetting
                for (int id : connectedPlayerIds) {
                    server.sendState(id, gameState);
                }
                // Reset gameState so auto-launch triggers on next update cycle
                gameState = null;
                change = false; // Already sent final state, don't send again
            }

            if (change) {
                for (int id : connectedPlayerIds) {
                    server.sendState(id, gameState);
                }
            }
        } else {
            // NO ACTIVE GAME - check if we should auto-launch
            // Only auto-launch if: players exist AND no gameState (means previous game fully ended)
            // Prevents infinite relaunch loop after a game ends
            if (connectedPlayerIds.size() >= 1 && gameState == null) {
                System.out.println("[" + id + "] Auto-launching new game (players: " + connectedPlayerIds.size() + ")");
                launchGame();
            }
        }
    }

    public Integer[] getConnectedPlayers() {
        return connectedPlayerIds.toArray(new Integer[0]);
    }

    public boolean containsPlayer(int id) {
        return connectedPlayerIds.contains(id);
    }

    public void launchGame() {
        System.out.println("[" + id + "] === LAUNCHING NEW GAME === players: " + connectedPlayerIds.size());
        buildDeck();
        shuffleDeck();

        activePlayers = connectedPlayerNames.values().toArray(new String[0]);
        standing = new boolean[activePlayers.length];
        next = new boolean[activePlayers.length];

        packetQueue = new Queue<PacketContext<ClientPacket>>();

        gameState = new GameState();
        gameState.active = true;
        gameState.allStand = false;
        gameState.hiddenCard = true;

        // dealerhand
        gameState.dHand = new ArrayList<Card>();
        gameState.dSum = 0;
        gameState.dAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        // NOTE: hidden card value is NOT added to dSum yet!
        // It will be counted once when playDealer() reveals it

        Card card = deck.remove(deck.size() - 1);
        gameState.dSum += card.getValue();
        gameState.dAceCount += card.isAce() ? 1 : 0;
        gameState.dHand.add(card);

        // playerhand - create players from connectedPlayerNames
        gameState.players = new ArrayList<GameState.Player>();

        for (String name : connectedPlayerNames.values()) {
            GameState.Player player = gameState.new Player();
            player.name = name;
            player.pHand = new ArrayList<Card>();
            player.pSum = 0;
            player.pAceCount = 0;

            for (int i = 0; i < 2; i++) {
                card = deck.remove(deck.size() - 1);
                player.pSum += card.getValue();
                player.pAceCount += card.isAce() ? 1 : 0;
                player.pHand.add(card);
            }

            gameState.players.add(player);
        }

        launched = true;

        // Print a readable summary of the initial deal
        System.out.println("[" + id + "] === NEW ROUND DEALT ===");
        System.out.println("  Dealer: [??, " + gameState.dHand.get(0) + "] (hidden card: " + hiddenCard + ")");
        for (GameState.Player p : gameState.players) {
            System.out.println("  " + p.name + ": " + p.pHand + " sum=" + p.pSum);
        }
        System.out.println("[" + id + "] =======================");

        // Send initial state to all players
        for (int id : connectedPlayerIds) {
            server.sendState(id, gameState);
        }
    }

    public void playTurn(String turn, int id) {
        String playerName = connectedPlayerNames.get(id);
        if (playerName == null) {
            System.out.println("[" + this.id + "] Unknown player " + id + " tried to play turn");
            return;
        }
        if (turn.equals("hit")) {
            GameState.Player player = null;
            for (int i = 0; i < gameState.players.size(); i++) {
                if (gameState.players.get(i).name.equals(playerName)) {
                    player = gameState.players.get(i);
                }
            }
            // Reject hit if player is already standing
            if (player != null && !player.standing) {
                Card card = deck.remove(deck.size() - 1);
                player.pSum += card.getValue();
                player.pAceCount += card.isAce() ? 1 : 0;
                player.pHand.add(card);
                if (psumreduce(player) > 21) {
                    player.standing = true;
                    for (int i = 0; i < activePlayers.length; i++) {
                        if (activePlayers[i] != null && activePlayers[i].equals(playerName)) {
                            standing[i] = true;
                        }
                    }
                }
            }
        } else if (turn.equals("stand")) {
            // Mark player as standing in gameState
            for (int i = 0; i < gameState.players.size(); i++) {
                if (gameState.players.get(i).name.equals(playerName)) {
                    gameState.players.get(i).standing = true;
                }
            }
            for (int i = 0; i < activePlayers.length; i++) {
                if (activePlayers[i] != null && activePlayers[i].equals(playerName)) {
                    standing[i] = true;
                }
            }
        } else if (turn.equals("next")) {
            for (int i = 0; i < activePlayers.length; i++) {
                if (activePlayers[i] != null && activePlayers[i].equals(playerName)) {
                    next[i] = true;
                }
            }
        }
    }

    public void playDealer() {
        gameState.hiddenCard = false;
        Card hCard = hiddenCard;
        gameState.dSum += hCard.getValue();
        gameState.dAceCount += hCard.isAce() ? 1 : 0;
        gameState.dHand.add(0, hCard);

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

        System.out.println("  > Built deck: " + deck.size() + " cards");
    }

    private void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("  > Deck shuffled (" + deck.size() + " cards)");
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