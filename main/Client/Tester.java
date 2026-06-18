package main.Client;

import main.Shared.ClientPacket;
import main.Shared.GameState;
import main.Shared.ServerPacket;

public class Tester {
    public static void main(String[] args) {
        ClientConnection<ServerPacket, ClientPacket> clientConnection = new ClientConnection<ServerPacket, ClientPacket>(
                "127.0.0.1", 80, (String s) -> ServerPacket.convertFromString(s),
                (ClientPacket packet) -> ClientPacket.convertToString(packet));

        System.out.println("Created Connections");

        // possible types: setName, getSessions(r), joinSession(s), playTurn(s),
        // quitSession,
        // test

        // r bedeutet antwort, s bedeutet state kann danach gesendet werden.

        // immer message nehmen mit take falls es eine antwort gibt um sicherzustellen
        // das nicht die vorherige als antwort genutzt wird

        clientConnection.send("test;" + System.currentTimeMillis());
        System.out.println("Send Test");
        clientConnection.takeLastMessage();

        clientConnection.send("setName;" + "Lorentz");
        System.out.println("Set Name");

        clientConnection.send("getSessions;");
        System.out.println("Requested Session list");
        while (!clientConnection.messageAvailable()) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {

            }
        }
        System.out.println("List:" + clientConnection.takeLastMessage().list);
        clientConnection.send("joinSession;new");
        while (!clientConnection.messageAvailable()) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {

            }
        }
        System.out.println(GameState.convertToString(clientConnection.takeLastMessage().state));
        clientConnection.send("playTurn;hit");
    }
}
