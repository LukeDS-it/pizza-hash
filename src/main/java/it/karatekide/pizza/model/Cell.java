package it.karatekide.pizza.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Cell {
    private int x, y, slice = -1;
    private boolean wasted;

    private Topping topping;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Cell(int x, int y, Topping t) {
        this(x, y);
        topping = t;
    }

    public boolean isCut() {
        return topping == null;
    }
}
