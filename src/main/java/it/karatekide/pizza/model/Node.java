package it.karatekide.pizza.model;

import static it.karatekide.pizza.model.Topping.MUSHROOM;
import static it.karatekide.pizza.model.Topping.TOMATO;
import static java.lang.Double.MAX_VALUE;

/**
 * A node is the representation of a possible slice of pizza
 * that is being cut. It has a starting and an ending vertex
 * and those represent the start and end of a rectangular
 * selection.
 *
 * Each node has its own weight, that indicates how well
 * the current slice fits in a possible solution.
 *
 * The Node also contains a pointer reference to:
 * <ul>
 *     <li>The previous state, in order to know the difference between states</li>
 *     <li>
 *         The "right" state that represents what happens if we move our selection to the cell on the right of the current
 *     </li>
 *     <li>
 *         The "bottom" state that represents what happens if we move our selection to the cell on the bottom of the current
 *     </li>
 * </ul>
 *
 * @author Luca Di Stefano
 */
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

    /**
     * In the constructor the basic fields are assigned and the weight is calculated,
     * then the referring left and bottom nodes are built recursively until
     * there is any sense in doing so.
     *
     * @param start the starting cell.
     * @param end the ending cell. If it is null, we consider that it's the same as start.
     * @param slicer the main object that is doing the slicing
     * @param previous pointer to the previous element in the tree
     */
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
        if (rightCell != null && getSliceArea(start, rightCell) <= slicer.getPizza().getMaxSize())
            right = new Node(start, rightCell, slicer, this);

        Cell bottomCell = slicer.getPizza().getBottomNode(end);
        if (bottomCell != null && getSliceArea(start, bottomCell) <= slicer.getPizza().getMaxSize())
            bottom = new Node(start, bottomCell, slicer, this);
    }

    /**
     * Builds the weight based on the following parameters:
     * <ul>
     *     <li>If it is the first node, weight is minus infinite</li>
     *     <li>If the area of the slice is greater than the required max area, weight is minus infinite</li>
     *     <li>If the slice is not fulfilling the minimum number of ingredients required, weight is minus infinite</li>
     *     <li>
     *         If all previous cases are not true, it returns the previous node's weight plus its own calculated weight.
     *         This is because we consider a solution that adds cells always better than a solution with less cells.
     *     </li>
     * </ul>
     *
     * @return the calculated weight, that can be either "minus infinite" as in -Double.MAX_VALUE or a double
     */
    private double getWeightInternal() {
        if (previous == null)
            return -MAX_VALUE;
        if (getSliceArea(startCell(), endCell()) > slicer.getPizza().getMaxSize())
            return -MAX_VALUE; // Unfeasible solution
        if (!minIngredientReached(TOMATO) || !minIngredientReached(MUSHROOM))
            return -MAX_VALUE; // Unfeasible solution

        return (previous != null && previous.isFeasible() ? previous.getWeight() : 0) + getOwnWeight();
    }

    /**
     * Calculates the specific weight of the node, with the following logic:
     * <ul>
     *     <li>
     *         If in the previous slice we didn't have enough ingredients, and in this iteration the ingredients
     *         have increased, we add a standard weight of "1" for each ingredient that has been added and
     *         wasn't previously enough.<br />
     *         e.g.:
     *         <code>
     *             Min ingredients = 2<br />
     *             Previous situation: T = 1, M = 1<br />
     *             Current Situation: T = 2, M = 2<br />
     *         </code>
     *         Weight of the node will be increased by 2.
     *     </li>
     *     <li>
     *         After calculating the ingredients increase, if we have enough ingredients of every kind we need
     *         to establish if we're actually moving towards a better solution or not.<br />
     *         In this case, the better solution will be the case in which we're adding to a slice more ingredients
     *         of the type that there is abundance of.<br />
     *         To do this, we calculate the ratio of tomatoes and mushrooms that we removed from the general pool
     *         with the following formula:<br />
     *         <code>[current(x) - previous(x)]/previous(x)</code><br />
     *         where "x" is the ingredient we're calculating the ratio of change (derivative).
     *         Then we see two separate cases:
     *         <ul>
     *             <li>The case in which we were previously having a surplus of tomatoes</li>
     *             <li>The case in which we were previously having a surplus of mushrooms</li>
     *         </ul>
     *
     *         Taking as example the first case, removing tomatoes is preferred and will be rewarded increasing
     *         the score with the ratio of tomatoes removed (note that the ratio is always negative since we always
     *         remove tomatoes or, at the very least, we don't remove any).<br />
     *         On the contrary, removing mushrooms will cause a lack of mushrooms for later slices, and will
     *         therefore be penalized, decreasing the score by the ratio of mushrooms removed.<br />
     *         This implies that the more mushrooms we remove, the worst the solution is.<br />
     *         <strong>Please note that</strong> sometimes even when we have few mushrooms we need to remove some
     *         of them in order to reach the minimum quantity of ingredients required. But this case is already
     *         considered in the previous section that <strong>DOES NOT EXCLUDE</strong> this one, thus removing
     *         mushrooms even if we don't have enough, is a price to pay to complete the slice correctly.
     *         The weight of this particular case will be 1 - x where x < 1, so there will be always a weight increase.
     *
     *         The opposite reasoning is done with having more mushrooms.<br />
     *     </li>
     *     <li>
     *         To avoid waste, if we're going in the only possible direction, we must increase the
     *         score.<br />
     *         e.g. if we're having the following
     *         <code>
     *             T<br />
     *             M<br />
     *             M<br />
     *             M<br />
     *             M<br />
     *             M<br />
     *         </code>
     *         even if we're going down and reducing the
     *         quantity of mushrooms, it's the only thing we can do, so we're actually avoiding
     *         waste and going towards the best possible solution.
     *     </li>
     * </ul>
     *
     * @return the calculated weight for the node, that can be any value except "minus infinite"
     */
    private double getOwnWeight() {
        double tmp = 0;


        if (!previous.minIngredientReached(TOMATO) || !previous.minIngredientReached(MUSHROOM)) {
            if (!previous.minIngredientReached(TOMATO)) {
                if (getToppingCount(TOMATO) > previous.getToppingCount(TOMATO))
                    tmp++;
            }
            if (!previous.minIngredientReached(MUSHROOM)) {
                if (getToppingCount(MUSHROOM) > previous.getToppingCount(MUSHROOM))
                    tmp++;
            }
        }

        if (minIngredientReached(TOMATO) && minIngredientReached(MUSHROOM)){

            double tomatoDiff = getRemaining(TOMATO) - previous.getRemaining(TOMATO);
            double mushDiff = getRemaining(MUSHROOM) - previous.getRemaining(MUSHROOM);

            double tRatio = tomatoDiff / previous.getRemaining(TOMATO);
            double mRatio = mushDiff / previous.getRemaining(MUSHROOM);

            if (previous.getRemaining(TOMATO) > previous.getRemaining(MUSHROOM)) {
                if (tomatoDiff < 0)
                    tmp -= tRatio;
                if (mushDiff < 0)
                    tmp += mRatio;
            }

            if (previous.getRemaining(MUSHROOM) > previous.getRemaining(TOMATO)) {
                if (mushDiff < 0)
                    tmp += mRatio;
                if (tomatoDiff < 0)
                    tmp -= tRatio;
            }

            if (slicer.getPizza().getRightNode(endCell()) == null || slicer.getPizza().getBottomNode(endCell()) == null) {
                tmp++;
            }
        }

        return tmp;
    }

    /**
     * Calculates the toppings that would remain after the slice has been cut
     *
     * @param t the topping type
     * @return the number of toppings
     */
    private int getRemaining(Topping t) {
        return slicer.getPizza().getRemainingToppings(t, startCell(), endCell());
    }

    /**
     * Calculates the topping count for the current slice of pizza
     *
     * @param t the topping type
     * @return toppings in current selection
     */
    private int getToppingCount(Topping t) {
        return slicer.getPizza().getToppingCount(t, startCell(), endCell());
    }

    /**
     * Indicates whether we've reached the minimum quantity of needed topping of type t
     *
     * @param t the topping type
     * @return if we have enough toppings of type t in the current slice simulation
     */
    private boolean minIngredientReached(Topping t) {
        return getToppingCount(t) >= slicer.getPizza().getMinIngredient();
    }

    /**
     * Calculates the area of the slice in the current simulation
     *
     * @param start the starting cell
     * @param end the ending cell
     * @return area of the slice
     */
    private int getSliceArea(Cell start, Cell end) {
        return Math.abs(start.x - (end.x + 1)) * Math.abs(start.y - (end.y + 1));
    }

    /**
     * Gives an immediate indication of the feasibility of the solution
     *
     * @return the current <code>weight == Double.MAX_VALUE</code>
     */
    boolean isFeasible() {
        return getWeight() != -MAX_VALUE;
    }

    /**
     * Gets the weight of the current node
     *
     * @return the weight
     */
    double getWeight() {
        return weight;
    }

    /**
     * Gets node of the tree that represents the selection moving to the right of the current cell
     *
     * @return a slice simulation
     */
    Node getRight() {
        return right;
    }

    /**
     * Gets node of the tree that represents the selection moving to the bottom of the current cell
     *
     * @return a slice simulation
     */
    Node getBottom() {
        return bottom;
    }

    /**
     * Utility method to get the starting cell
     *
     * @return a pizza cell
     */
    private Cell startCell() {
        return new Cell(startX, startY);
    }

    /**
     * Utility method to get the ending cell
     *
     * @return a pizza cell
     */
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
}
