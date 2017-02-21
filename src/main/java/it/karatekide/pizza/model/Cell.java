package it.karatekide.pizza.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Cell {

    int x, y;
    boolean visited;
    Topping topping;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
