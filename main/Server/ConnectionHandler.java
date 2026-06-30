package main.Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

import main.Shared.PacketContext;
import main.Shared.Queue;

public class ConnectionHandler<T, R> extends ServerConnectionHandler {

    private ArrayList<String> clientIPs;
    private Map<Integer, Integer> clientPorts;

    private Queue<PacketContext<T>> receivedMessages;
    private Queue<Integer> closedConnectionIds;
    private Function<String, T> converter;
    private Function<R, String> converterBack;

    public ConnectionHandler(int pPort, Function<String, T> converter, Function<R, String> converterBack) {
        super(pPort);

        clientIPs = new ArrayList<String>();
        clientPorts = new HashMap<Integer, Integer>();
        receivedMessages = new Queue<PacketContext<T>>();

        this.converter = converter;
        this.converterBack = converterBack;
    }

    public void sendMessage(int id, R message) {
        try {
            System.out.println(converterBack.apply(message));
            send(clientIPs.get(id), clientPorts.get(id), converterBack.apply(message));
        } catch (Exception e) {
            System.out.println("converting outgoing message failed " + e);
        }
    }

    public PacketContext<T> getLatestMessage() {
        PacketContext<T> message = receivedMessages.front();
        receivedMessages.dequeue();
        return message;
    }

    public boolean messageAvailable() {
        return !receivedMessages.isEmpty();
    }

    public void closeConnection(int id) {

        closeConnection(clientIPs.get(id), clientPorts.get(id));
        clientIPs.remove(id);
        clientPorts.remove(id);
        closedConnectionIds.enqueue(id);
    }

    public void removeClosedConnections() {
        for (String n : clientIPs) {
            int id = clientIPs.indexOf(n);
            if (!isConnectedTo(n, clientPorts.get(id))) {
                closeConnection(id);
            }
        }
    }

    public Integer[] getClosedConnections() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        while (!closedConnectionIds.isEmpty()) {
            ids.add(closedConnectionIds.front());
            closedConnectionIds.dequeue();
        }
        return ids.toArray(new Integer[0]);
    }

    public void processNewConnection(String pClientIP, int pClientPort) {
        clientIPs.add(pClientIP);
        int id = clientIPs.indexOf(pClientIP);
        clientPorts.put(id, pClientPort);
    }

    public void processMessage(String pClientIP, int pClientPort, String pMessage) {
        int id = clientIPs.indexOf(pClientIP);
        try {
            receivedMessages.enqueue(new PacketContext<T>(converter.apply(pMessage), id));
        } catch (Exception e) {
            System.out.println("converting incoming message failed");
        }
    }

    public void processClosingConnection(String pClientIP, int pClientPort) {
    }

}