package it.karatekide.pizza.tree;

import lombok.Getter;
import lombok.Setter;

/**
 * A cell represents a pair of coordinates,
 * x represents the ROW the cell is in, and y
 * represents the column.
 *
 * @author Luca Di Stefano
 */
@Getter
@Setter
class Cell {

    int x, y;

    Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

}
