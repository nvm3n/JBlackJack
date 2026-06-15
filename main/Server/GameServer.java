package main.Server;
import main.Shared.*;
import java.util.ArrayList;
import java.util.function.*;

public class GameServer<T, R> extends Server {

    private ArrayList<String> clientIPs;
    private ArrayList<Integer> clientPorts;
    private ArrayList<T> lastReceivedMessages;
    private Queue<Integer> closedConnectionIds;
    private Function<String, T> converter;
    private Function<R, String> converterBack;

    public GameServer(int pPort, Function<String, T> converter, Function<R, String> converterBack) {
        super(pPort);

        clientIPs = new ArrayList<String>();
        clientPorts = new ArrayList<Integer>();
        lastReceivedMessages = new ArrayList<T>();

        this.converter = converter;
        this.converterBack = converterBack;
    }

    public void sendMessage(int id, R message) {
        send(clientIPs.get(id), clientPorts.get(id), converterBack.apply(message));
    }

    public T getLatestMessage(int id) {
        return lastReceivedMessages.get(id);
    }

    public void closeConnection(int id) {

        closeConnection(clientIPs.get(id), clientPorts.get(id));
        clientIPs.remove(id);
        clientPorts.remove(id);
        lastReceivedMessages.remove(id);
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
        return (Integer[]) ids.toArray();
    }

    public void processNewConnection(String pClientIP, int pClientPort) {
        clientIPs.add(pClientIP);
        int id = clientIPs.indexOf(pClientIP);
        clientPorts.add(id, pClientPort);

    }

    public void processMessage(String pClientIP, int pClientPort, String pMessage) {
        int id = clientIPs.indexOf(pClientIP);
        lastReceivedMessages.set(id, converter.apply(pMessage));
    }

    public void processClosingConnection(String pClientIP, int pClientPort) {
    }

}
