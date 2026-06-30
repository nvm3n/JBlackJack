package main.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import main.Shared.ClientPacket;
import main.Shared.GameState;
import main.Shared.PacketContext;
import main.Shared.Queue;
import main.Shared.ServerPacket;

public class GameServer {

    ArrayList<BlackJSession> sessions;
    ConnectionHandler<ClientPacket, ServerPacket> gamesServer;

    Map<Integer, String> names;

    GameServer(int Port) {

        gamesServer = new ConnectionHandler<ClientPacket, ServerPacket>(Port, (String s) -> ClientPacket.convertFromString(s),
                (ServerPacket packet) -> ServerPacket.convertToString(packet));
        sessions = new ArrayList<BlackJSession>();
        names = new HashMap<Integer, String>();

        System.out.println("server launched");

        while (true) {
            runServer();
        }
    }

    public void runServer() {
        // possible types: setName, getSessions, joinSession, playTurn, quitSession,
        // test

        // int[] activePlayers = new int[1];
        String sessionInfo = "Current active Sessions:";

        for (BlackJSession session : sessions) {
            sessionInfo += ">" + session.id + ": " + session.getDescriptor();
        }
        if (sessions.size() == 0) {
            sessionInfo = "Currently no active Sessions";
        }

        Queue<PacketContext<ClientPacket>> joinQueue = new Queue<PacketContext<ClientPacket>>();
        Queue<PacketContext<ClientPacket>> turnQueue = new Queue<PacketContext<ClientPacket>>();
        Queue<PacketContext<ClientPacket>> quitQueue = new Queue<PacketContext<ClientPacket>>();

        while (gamesServer.messageAvailable()) {
            PacketContext<ClientPacket> packetContext = gamesServer.getLatestMessage();
            if (packetContext.packet.type.equals("getSessions")) {
                gamesServer.sendMessage(packetContext.id, new ServerPacket("SessionList", sessionInfo));
            } else if (packetContext.packet.type.equals("joinSession")) {
                joinQueue.enqueue(packetContext);
            } else if (packetContext.packet.type.equals("playTurn")) {
                turnQueue.enqueue(packetContext);
            } else if (packetContext.packet.type.equals("quitSession")) {
                quitQueue.enqueue(packetContext);
            } else if (packetContext.packet.type.equals("test")) {
                try {
                    System.out.println("Received testpacket after "
                            + (System.currentTimeMillis() - Long.valueOf(packetContext.packet.content) + "ms"));
                } catch (Exception e) {
                    System.out.println("Failed to determine testpacket ping");
                }
                gamesServer.sendMessage(packetContext.id,
                        new ServerPacket("test", String.valueOf(System.currentTimeMillis())));
            } else if (packetContext.packet.type.equals("setName")) {
                names.put(packetContext.id, packetContext.packet.content);
            }
        }

        while (!joinQueue.isEmpty()) {
            System.out.println("Handling join");
            PacketContext<ClientPacket> packetContext = joinQueue.front();
            joinQueue.dequeue();
            try {
                if (!packetContext.packet.content.equals("new")) {
                    int sessionID = Integer.parseInt(packetContext.packet.content);
                    if (sessions.get(sessionID).getConnectedPlayers().length < 4) {
                        sessions.get(sessionID).addPlayer(packetContext.id, names.get(packetContext.id));
                    }
                } else {
                    BlackJSession newSession = new BlackJSession(sessions.size(), this);
                    sessions.add(newSession);
                    newSession.addPlayer(packetContext.id, names.get(packetContext.id));
                }
            } catch (Exception e) {

            }
        }

        while (!turnQueue.isEmpty()) {
            PacketContext<ClientPacket> packetContext = turnQueue.front();
            turnQueue.dequeue();

            for (BlackJSession session : sessions) {
                if (session.containsPlayer(packetContext.id)) {
                    session.addPacket(packetContext);
                }
            }
        }

        while (!quitQueue.isEmpty()) {
            PacketContext<ClientPacket> packetContext = quitQueue.front();
            quitQueue.dequeue();
            try {
                for (BlackJSession session : sessions) {
                    session.removePlayer(packetContext.id);
                }
            } catch (Exception e) {

            }
        }

        for (BlackJSession session : sessions) {
            session.update();
        }
    }

    public void sendState(int id, GameState state) {
        gamesServer.sendMessage(id, new ServerPacket(state));
    }
}