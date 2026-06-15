package main.Shared;

public class Card {

    String type;
    int value;

    public Card(String pType, int pVal) {
        value = pVal;
        type = pType;
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
