package main.Client;

import main.Shared.*;
import java.util.ArrayList;

public class Game {

    ClientConnection<ServerPacket, ClientPacket> gameClient;
    GameState gameState;
    String sessionList;
    int playerId = -1;
    String playerName;

    Game(String ServerIP, int Port) {
        System.out.println("Game launched");
        gameClient = new ClientConnection<ServerPacket, ClientPacket>(ServerIP, Port,
                (String s) -> ServerPacket.convertFromString(s),
                (ClientPacket p) -> ClientPacket.convertToString(p));
        gameState = null;
        sessionList = "";
    }

    public void setName(String name) {
        this.playerName = name;
        gameClient.sendMessage(new ClientPacket("setName", name));
    }

    public void requestSessions() {
        gameClient.sendMessage(new ClientPacket("getSessions", ""));
    }

    public void joinSession(int sessionId) {
        gameState = null; // Reset to ensure state poller triggers on first GameState
        gameClient.sendMessage(new ClientPacket("joinSession", String.valueOf(sessionId)));
    }

    public void createSession() {
        gameState = null; // Reset to ensure state poller triggers on first GameState
        gameClient.sendMessage(new ClientPacket("joinSession", "new"));
    }

    public void hit() {
        System.out.println("client sending hit");
        gameClient.sendMessage(new ClientPacket("playTurn", "hit"));
    }

    public void stand() {
        System.out.println("client sending stand");
        gameClient.sendMessage(new ClientPacket("playTurn", "stand"));
    }

    public void next() {
        System.out.println("client sending next");
        gameClient.sendMessage(new ClientPacket("playTurn", "next"));
    }

    public void quitSession() {
        gameClient.sendMessage(new ClientPacket("quitSession", ""));
    }

    public boolean pollState() {
        boolean changed = false;
        ServerPacket lastGameStatePacket = null;
        ServerPacket lastSessionListPacket = null;
        
        // Collect all available packets, keeping only the last of each type
        while (gameClient.messageAvailable()) {
            ServerPacket packet = gameClient.takeLastMessage();
            if (packet.type.equals("GameState")) {
                lastGameStatePacket = packet;
            } else if (packet.type.equals("SessionList")) {
                lastSessionListPacket = packet;
            }
        }
        
        // Process only the latest GameState
        if (lastGameStatePacket != null && lastGameStatePacket.state != null) {
            gameState = lastGameStatePacket.state;
            changed = true;
        }
        
        // Process session list
        if (lastSessionListPacket != null) {
            sessionList = lastSessionListPacket.list;
            changed = true;
        }
        
        return changed;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getSessionList() {
        return sessionList;
    }

    public boolean isConnected() {
        return gameClient.isConnected();
    }

    // The following methods are kept for backward compatibility with GameClient rendering
    // but they now read from the server-provided GameState

    public ArrayList<Card> getPHand() {
        if (gameState == null || gameState.players == null || gameState.players.size() == 0)
            return new ArrayList<Card>();
        return gameState.players.get(0).pHand;
    }

    public ArrayList<Card> getDHand() {
        if (gameState == null)
            return new ArrayList<Card>();
        return gameState.dHand;
    }

    public Card getHiddenCard() {
        // hiddenCard is a boolean in GameState indicating if the card is still hidden
        // We return null when hidden, and the actual card is the first in dHand when revealed
        if (gameState == null || gameState.hiddenCard)
            return null;
        if (gameState.dHand.size() > 0) {
            return gameState.dHand.get(0);
        }
        return null;
    }

    public Boolean getOngoingState() {
        if (gameState == null)
            return false;
        return gameState.active;
    }

    public int dsumreduce() {
        if (gameState == null)
            return 0;
        int sum = gameState.dSum;
        int aceCount = gameState.dAceCount;
        while (sum > 21 && aceCount > 0) {
            sum -= 10;
            aceCount -= 1;
        }
        return sum;
    }

    public int psumreduce() {
        if (gameState == null || gameState.players == null || gameState.players.size() == 0)
            return 0;
        int sum = gameState.players.get(0).pSum;
        int aceCount = gameState.players.get(0).pAceCount;
        while (sum > 21 && aceCount > 0) {
            sum -= 10;
            aceCount -= 1;
        }
        return sum;
    }
}