package main.Shared;

public class Card {

    String type;
    int value;

    public Card(String pType, int pVal) {
        value = pVal;
        type = pType;
    }

    Card(String pString) {
        String[] seperated = pString.split(" ");
        if (seperated.length != 2) {
            throw new ArithmeticException();
        }
        type = seperated[0];
        value = Integer.parseInt(seperated[1]);
    }

    public int getValue() {
        if (value == 1) {
            return 11;
        } else if (value >= 11) {
            return 10;
        } else {
            return value;
        }
    }

    public boolean isAce() {
        return value == 1;
    }

    public String toString() {
        return type + " " + value;
    }

    public String getImgPath() {
        return "./cardsprites/" + toString() + ".png";
    }

}