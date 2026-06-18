package main.Client;

import java.util.function.Function;

public class ClientConnection<T, R> extends Client {

    private T lastMessage;

    private Function<String, T> converter;
    private Function<R, String> converterBack;

    public ClientConnection(String pServerIP, int pServerPort, Function<String, T> converter,
            Function<R, String> converterBack) {
        super(pServerIP, pServerPort);

        this.converter = converter;
        this.converterBack = converterBack;
    }

    // sendet das paket and den server, hier der gewählte zug, datenstruktur kannst
    // du dir ja noch aussuchen
    public void sendMessage(R pMessage) {
        send(converterBack.apply(pMessage));
    }

    // gibt letzte nachricht
    public T getLastMessage() {
        return lastMessage;
    }

    // gibt nachricht und entfernt sie, so kannst du sicherstellen das du was neues
    // hast
    public T takeLastMessage() {
        T message = lastMessage;
        lastMessage = null;
        return message;
    }

    // überprüft ob was da ist
    public boolean messageAvailable() {
        return lastMessage != null;
    }

    // intern von client geerbt, nicht wichtig
    public void processMessage(String pMessage) {
        lastMessage = converter.apply(pMessage);
    }

}
