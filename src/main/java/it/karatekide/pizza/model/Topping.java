package it.karatekide.pizza.model;

public enum Topping {
    MUSHROOM("M"), TOMATO("T");

    private String value;

    private Topping(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
