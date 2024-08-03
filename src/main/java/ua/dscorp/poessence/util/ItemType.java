package ua.dscorp.poessence.util;

public enum ItemType {
    ESSENCE("Essence", "item"),
    FOSSIL("Fossil", "item"),
    FRAGMENT("Fragment", "currency"),
    CURRENCY("Currency", "currency"),
    SCARAB("Scarab", "item");

    private final String name;
    private final String type;

    ItemType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
