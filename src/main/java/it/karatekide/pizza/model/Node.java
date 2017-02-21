package it.karatekide.pizza.model;

import static it.karatekide.pizza.model.Topping.MUSHROOM;
import static it.karatekide.pizza.model.Topping.TOMATO;
import static java.lang.Double.MAX_VALUE;

public class Node implements Comparable<Node> {

    private static final Node NULL_NODE = null;
    private static final Cell NULL_CELL = null;

    int startX, startY, endX, endY;
    private double weight;
    private Slicer slicer;
    private Node previous, right, bottom;

    Node(Cell start, Slicer slicer) {
        this(start, NULL_CELL, slicer, NULL_NODE);
    }

    private Node(Cell start, Cell end, Slicer slicer, Node previous) {
        startX = start.x;
        startY = start.y;
        if (end == null)
            end = start;
        endX = end.x;
        endY = end.y;

        this.slicer = slicer;
        this.previous = previous;
        weight = getWeightInternal();

        Cell rightCell = slicer.getPizza().getRightNode(end);
        if (rightCell != null && getNodeArea(start, rightCell) <= slicer.getPizza().getMaxSize())
            right = new Node(start, rightCell, slicer, this);

        Cell bottomCell = slicer.getPizza().getBottomNode(end);
        if (bottomCell != null && getNodeArea(start, bottomCell) <= slicer.getPizza().getMaxSize())
            bottom = new Node(start, bottomCell, slicer, this);
    }

    boolean isFeasible() {
        return getWeight() != -MAX_VALUE;
    }

    double getWeight() {
        return weight;
    }

    private double getWeightInternal() {
        if (previous == null)
            return -MAX_VALUE; // First node is always unfeasible
        if (getNodeArea(startCell(), endCell()) > slicer.getPizza().getMaxSize())
            return -MAX_VALUE; // Unfeasible solution
        if (!minIngredientReached(TOMATO) || !minIngredientReached(MUSHROOM))
            return -MAX_VALUE; // Unfeasible solution

        return (previous != null && previous.isFeasible() ? previous.getWeight() : 0) + getOwnWeight();
    }

    private double getOwnWeight() {
        double tmp = 0;


        if (!previous.minIngredientReached(TOMATO) || !previous.minIngredientReached(MUSHROOM)) {
            // If in the previous slice I didn't have enough ingredients
            // And I'm increasing the ingredients I need
            if (!previous.minIngredientReached(TOMATO)) {
                if (getToppingCount(TOMATO) > previous.getToppingCount(TOMATO))
                    tmp++;
            }
            if (!previous.minIngredientReached(MUSHROOM)) {
                if (getToppingCount(MUSHROOM) > previous.getToppingCount(MUSHROOM))
                    tmp++;
            }
        }
        // If I have reached the min ingredients
        if (minIngredientReached(TOMATO) && minIngredientReached(MUSHROOM)){

            double tomatoDiff = getRemaining(TOMATO) - previous.getRemaining(TOMATO);
            double mushDiff = getRemaining(MUSHROOM) - previous.getRemaining(MUSHROOM);

            double tProportion = tomatoDiff / previous.getRemaining(TOMATO);
            double mProportion = mushDiff / previous.getRemaining(MUSHROOM);

            // If I was in surplus of tomatoes
            if (previous.getRemaining(TOMATO) > previous.getRemaining(MUSHROOM)) {
                if (tomatoDiff < 0) // Removing tomatoes is preferred
                    tmp -= tProportion;
                if (mushDiff < 0)
                    tmp += mProportion;
            }

            // If I was in surplus of mushrooms
            if (previous.getRemaining(MUSHROOM) > previous.getRemaining(TOMATO)) {
                if (mushDiff > 0) // Removing mushrooms is preferred
                    tmp += mProportion;
                if (tomatoDiff > 0)
                    tmp -= tProportion;
            }
        }

        return tmp;
    }

    private int getRemaining(Topping t) {
        return slicer.getPizza().getRemainingToppings(t, startCell(), endCell());
    }

    private int getToppingCount(Topping t) {
        return slicer.getPizza().getToppingCount(t, startCell(), endCell());
    }

    private boolean minIngredientReached(Topping t) {
        return getToppingCount(t) >= slicer.getPizza().getMinIngredient();
    }

    private int getNodeArea(Cell start, Cell end) {
        return Math.abs(start.x - (end.x + 1)) * Math.abs(start.y - (end.y + 1));
    }

    private Cell startCell() {
        return new Cell(startX, startY);
    }

    private Cell endCell() {
        return new Cell(endX, endY);
    }

    @Override
    public int compareTo(Node o) {
        return Double.valueOf(getWeight()).compareTo(o.getWeight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;

        Node node = (Node) o;

        return startX == node.startX && startY == node.startY && endX == node.endX && endY == node.endY;
    }

    @Override
    public int hashCode() {
        int result = startX;
        result = 31 * result + startY;
        result = 31 * result + endX;
        result = 31 * result + endY;
        return result;
    }

    Node getRight() {
        return right;
    }

    Node getBottom() {
        return bottom;
    }
}
