package main.Client;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

public class Launch {

    public static void main(String[] args) throws Exception {
        String serverIP = "127.0.0.1";
        String privateIP;
        int port = 13150;
        String playerName;

        try {
            privateIP = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        if (args.length >= 1) {
            serverIP = args[0];
        } else {
            System.out.println("Defaulting to serverIP: " + serverIP);
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, using default: " + port);
            }
        } else {
            System.out.println("Defaulting to port" + port);
        }
        if (args.length >= 3) {
            playerName = args[2];
        } else {
            playerName = privateIP;
        }
        String input = JOptionPane.showInputDialog(null,
                "Enter your player name:",
                "Player Name",
                JOptionPane.QUESTION_MESSAGE);
        if (input != null && !input.trim().isEmpty()) {
            playerName = input.trim();
        } else if (input == null) {
            System.out.println("Name input cancelled, using default: " + playerName);
        }

        GameClient client = new GameClient(serverIP, port, playerName);
        System.out.println("client launched");
    }
}
