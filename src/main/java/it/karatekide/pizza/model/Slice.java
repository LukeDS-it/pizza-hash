package it.karatekide.pizza.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Slice {
    Cell start, end;

    Slice(Cell start, Cell end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return start.x  + " " + start.y + " " + end.x + " " + end.y + "\n";
    }
}
