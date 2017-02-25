package it.karatekide.pizza.genetic;

import it.karatekide.pizza.model.Cell;
import it.karatekide.pizza.model.Slice;

/**
 * A gene is the representation of a part of a solution, which in our case
 * is represented by a selection of pizza
 *
 * @author Luca Di Stefano
 *
 */
public class Gene extends Slice{

    public Gene(Cell start, Cell end) {
        super(start, end);
    }

}
