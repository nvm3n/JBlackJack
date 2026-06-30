package main.Server;

public class Launch {

    public static void main(String[] args) throws Exception {
        int port = 13150;

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
                System.out.println(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, using default: " + port);
            }
        }else {
            System.out.println("Defaulting to port " + port);
        }

        new GameServer(port);
    }
}