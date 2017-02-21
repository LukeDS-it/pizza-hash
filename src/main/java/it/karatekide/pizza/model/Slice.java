package it.karatekide.pizza.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Representation of a slice
 *
 * @author Luca Di Stefano
 */
@Getter @Setter
public class Slice {
    Cell start, end;

    Slice(Cell start, Cell end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the string representation of the slice in the format required
     * for the submission
     *
     * @return <code>X Y x y</code> where upper case is starting point, lower case is ending point.
     */
    @Override
    public String toString() {
        return start.x  + " " + start.y + " " + end.x + " " + end.y + "\n";
    }
}
