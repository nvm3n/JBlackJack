package main.Client;

public class Launch {

    public static void main(String[] args) throws Exception {
        BlackJSession blackjsession = new BlackJSession();

        blackjsession.launchGame();

        System.out.println("client launched");

    }
}