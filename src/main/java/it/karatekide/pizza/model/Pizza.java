package it.karatekide.pizza.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static it.karatekide.pizza.model.Topping.MUSHROOM;
import static it.karatekide.pizza.model.Topping.TOMATO;

/**
 * Representation of the whole pizza.
 * It contains all the values specified in the input file such as
 * the minimum ingredients of each type per slice, the max slice size
 * and the map with the topping disposition.
 *
 * It also contains useful methods to count remaining toppings and to
 * produce a graphical representation of the fractionated pizza.
 * (It is suggested not to print the graphical representation if the slices
 * are more than 90 or so, to avoid strange characters being printed).
 *
 * @author Luca Di Stefano
 */
public class Pizza {

    private final static char WASTED_PIZZA = '#';
    private final static int FIRST_CHAR = 48;

    @Getter
    private int minIngredient;
    @Getter
    private int maxSize;
    @Getter
    private int waste = 0;
    @Getter
    private Topping[][] pizza;

    private char[][] sliceDesc;
    private int lastSlice = 0;

    private Map<Topping, Integer> allToppings = new HashMap<>();

    /**
     * Instantiates the pizza from the file content
     *
     * @param description the file content with the pizza description and requirements
     */
    public Pizza(String description) {
        allToppings.put(MUSHROOM, 0);
        allToppings.put(TOMATO, 0);

        Scanner s = new Scanner(description);
        String[] init = s.nextLine().split(" ");
        int rows = Integer.parseInt(init[0]);
        int cols = Integer.parseInt((init[1]));

        minIngredient = Integer.parseInt(init[2]);
        maxSize = Integer.parseInt(init[3]);

        pizza = new Topping[rows][cols];
        sliceDesc = new char[rows][cols];

        int row = 0;
        while (s.hasNextLine()) {
            String pizzaLine = s.nextLine();
            for (int col = 0; col < pizzaLine.length(); col++) {
                Topping t = (pizzaLine.charAt(col) == 'T' ? TOMATO : MUSHROOM);
                pizza[row][col] = t;
                allToppings.replace(t, allToppings.get(t) + 1);
            }
            row++;
        }

        s.close();
    }

    /**
     * Cuts a slice, removing toppings from the map,
     * updates the graphical map representation and
     * returns the slice information
     *
     * @param node the node that simulates the cut
     * @return the slice represented by the node
     */
    Slice cut(Node node) {
        Slice slice = new Slice(new Cell(node.startX, node.startY), new Cell(node.endX, node.endY));
        cut(slice);
        return slice;
    }

    /**
     * Throws away pizza (sigh). Wasted pizza is marked as "#" in
     * the graphical representation, internally it is just a "null" topping.
     *
     * @param row the row indication of wasted pizza
     * @param col the column indication of wasted pizza
     */
    void waste(int row, int col) {
        pizza[row][col] = null;
        sliceDesc[row][col] = WASTED_PIZZA;
        waste++;
    }

    /**
     * Does the actual cutting of the slice, removing toppings from the map,
     * updating the graphical map representation and
     * returning the slice information
     *
     * @param slice the slice to cut
     */
    private void cut(Slice slice) {
        int startX = slice.start.x;
        int endX = slice.end.x;
        int startY = slice.start.y;
        int endY = slice.end.y;

        for (int row = startX; row <= endX; row++) {
            for (int col = startY; col <= endY; col++) {
                Topping t = pizza[row][col];
                if (t != null) {
                    allToppings.replace(t, allToppings.get(t) - 1);
                    pizza[row][col] = null;
                    sliceDesc[row][col] = getPizzaChar(lastSlice);
                }
            }
        }
        lastSlice++;
    }

    /**
     * Gets the first top-left free cell
     *
     * @return a cell indication
     */
    Cell getFirstCell() {
        for (int row = 0; row < pizza.length; row++) {
            for (int col = 0; col < pizza[row].length; col++) {
                if (pizza[row][col] != null)
                    return new Cell(row, col);
            }
        }
        return null;
    }

    /**
     * Calculates how many toppings of a kind are in the pizza
     *
     * @param topping the topping type
     * @return quantity of topping
     */
    private int getToppings(Topping topping) {
        return allToppings.get(topping);
    }

    /**
     * Calculates how many toppings there are in a determined slice of pizza
     *
     * @param topping type of topping
     * @param start starting cell of the selection
     * @param end ending cell of the selection
     * @return the number of toppings
     */
    Integer getToppingCount(Topping topping, Cell start, Cell end) {
        int partial = 0;
        for (int x = start.x; x <= end.x; x++) {
            for (int y = start.y; y <= end.y; y++) {
                if (pizza[x][y] == topping)
                    partial++;
            }
        }
        return partial;
    }

    /**
     * Calculates how many toppings of a type will remain if we remove
     * a particular slice of pizza
     *
     * @param topping type of topping
     * @param start starting cell of the selection
     * @param end ending cell of the selection
     * @return the number of toppings
     */
    int getRemainingToppings(Topping topping, Cell start, Cell end) {
        return getToppings(topping) - getToppingCount(topping, start, end);
    }

    /**
     * Tries to get the node to the right of the specified cell
     *
     * @param c the current cell
     * @return the cell on the right if it exists and it has not been cut,
     * otherwise null
     */
    Cell getRightNode(Cell c) {
        if (pizza[c.x].length > c.y + 1 && pizza[c.x][c.y + 1] != null)
            return new Cell(c.x, c.y + 1);
        return null;
    }

    /**
     * Tries to get the node to the bottom of the specified cell
     *
     * @param c the current cell
     * @return the cell on the bottom if it exists and it has not been cut,
     * otherwise null.
     */
    Cell getBottomNode(Cell c) {
        if (pizza.length > c.x + 1 && pizza[c.x + 1][c.y] != null)
            return new Cell(c.x + 1, c.y);
        return null;
    }

    /**
     * Indicates if there are still ingredients left in the pizza
     *
     * @return boolean
     */
    public boolean isEmpty() {
        boolean tmp = true;

        for (Topping[] pRow : pizza) {
            for (Topping pCol : pRow) {
                tmp = (tmp && pCol == null);
            }
        }

        return tmp;
    }

    /**
     * Gets the graphical representation of the pizza. Please use carefully,
     * beyond 90 slices it can produce gibberish.
     *
     * @return a map indicating how the pizza has been fractionated.
     */
    public String getSliceDesc() {
        StringBuilder sb = new StringBuilder("~~~~~~~ HERE IS YOUR PIZZA ~~~~~~~\n");
        for (char[] rows: sliceDesc) {
            for (char cell: rows) {
                sb.append(cell).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns the total area of the pizza
     *
     * @return total area of the pizza
     */
    public int getSurface() {
        return pizza.length * pizza[0].length;
    }

    /**
     * Returns how many cells have been used (not wasted)
     *
     * @return used cells
     */
    public int getUsed() {
        return getSurface() - waste;
    }

    /**
     * Utility method to convert number of slice to a character
     * for the pizza slices map
     * @param lastSlice number of last slice
     * @return character representation of slice
     */
    private char getPizzaChar(int lastSlice) {
        int candidate = lastSlice + FIRST_CHAR;
        if (candidate <= 64 && candidate >= 58) {
            candidate += 7;
        }
        return (char) (candidate);
    }
}
