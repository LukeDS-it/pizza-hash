package it.karatekide.pizza.model;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * Representation of a slice
 *
 * @author Luca Di Stefano
 */
@Getter
@Setter
public class Slice {

    private Cell start, end;

    public Slice(Cell start, Cell end) {
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
        return start.getX() + " " + start.getY() + " " + end.getX() + " " + end.getY() + "\n";
    }

    public String graphic() {
        return "";
    }

    /**
     * Checks if this slice overlaps with another
     * <p>
     * Two slices overlap if they're not the same slice (to avoid comparing with self)
     * and if the top left cell of either slice is not in the middle of the other
     *
     * @param slice the slice to compare
     * @return true if the two slices overlap
     */
    public boolean overlaps(Slice slice) {
        Rectangle myArea = new Rectangle(start.getX(), start.getY(), end.getX()+1-start.getX(), end.getY()+1 - start.getY());
        Rectangle oArea = new Rectangle(slice.start.getX(), slice.start.getY(), slice.end.getX() + 1 - start.getX(), slice.end.getY() + 1 - slice.start.getY());
        return this != slice && myArea.intersects(oArea);
    }
}

