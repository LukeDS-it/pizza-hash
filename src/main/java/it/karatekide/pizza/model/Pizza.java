package it.karatekide.pizza.model;

import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;

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

    private final static String WASTED_PIZZA = "(  #  )";

    @Getter
    private int minIngredient;
    @Getter
    private int maxSize;
    @Getter
    private int waste = 0;

    private double cut = 0;

    private Cell[][] pizza;

    private int lastSlice = 0;

    private String description;

    private Map<Topping, Integer> toppingCount = new HashMap<>();

    /**
     * Instantiates the pizza from the file content
     *
     * @param description the file content with the pizza description and requirements
     */
    public Pizza(String description) {
        init(description);
    }

    /**
     * Creates a duplicate of a pizza from its starting point
     *
     * @param pizza the starting pizza
     */
    public Pizza(Pizza pizza) {
        init(pizza.description);
    }

    private void init(String description) {
        this.description = description;
        toppingCount.put(MUSHROOM, 0);
        toppingCount.put(TOMATO, 0);

        Scanner s = new Scanner(description);
        String[] init = s.nextLine().split(" ");
        int rows = Integer.parseInt(init[0]);
        int cols = Integer.parseInt((init[1]));

        minIngredient = Integer.parseInt(init[2]);
        maxSize = Integer.parseInt(init[3]);

        pizza = new Cell[rows][cols];

        int row = 0;
        while (s.hasNextLine()) {
            String pizzaLine = s.nextLine();
            for (int col = 0; col < pizzaLine.length(); col++) {
                Topping t = (pizzaLine.charAt(col) == 'T' ? TOMATO : MUSHROOM);
                pizza[row][col] = new Cell(row, col, t);
                toppingCount.replace(t, toppingCount.get(t) + 1);
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
     * @param from starting cell (top left corner of selection)
     * @param to ending cell (bottom right corner of selection)
     * @return a slice of pizza
     */
    public Slice cut(Cell from, Cell to) {
        int startX = from.getX() < to.getX() ? from.getX() : to.getX();
        int startY = from.getY() < to.getY() ? from.getY() : to.getY();
        int endX = from.getX() > to.getX() ? from.getX() : to.getX();
        int endY = from.getY() > to.getY() ? from.getY() : to.getY();

        Slice slice = new Slice(new Cell(startX, startY), new Cell(endX, endY));

        for (int row = startX; row <= endX; row++) {
            for (int col = startY; col <= endY; col++) {
                Cell c = pizza[row][col];
                if (!c.isCut()) {
                    toppingCount.replace(c.getTopping(), toppingCount.get(c.getTopping()) - 1);
                    pizza[row][col].setTopping(null);
                    pizza[row][col].setSlice(lastSlice);
                    cut++;
                }
            }
        }
        lastSlice++;

        return slice;
    }

    /**
     * Cuts all slices of pizza
     * @param slices all the slices to cut
     */
    @SafeVarargs
    public final <S extends Slice> void cutAll(S... slices) {
        Arrays.stream(slices).forEach(s -> cut(s.getStart(), s.getEnd()));
    }

    /**
     * Throws away pizza :(
     *
     * @param cell cell that can't fit a solution
     */
    public void waste(Cell cell) {
        pizza[cell.getX()][cell.getY()].setWasted(true);
        waste++;
    }

    /**
     * Returns the total area of the pizza
     *
     * @return total area of the pizza
     */
    private int getSurface() {
        return pizza.length * pizza[0].length;
    }

    /**
     * Returns percentage of pizza used
     *
     * @return 0 < value < 1
     */
    public double getUsedPizza() {
        double surface = getSurface();
        return cut / surface;
    }

    /**
     * Gets a random non-empty cell
     *
     * @return a cell
     */
    public Cell getRandomCell() {
        Cell cell;
        do {
            int row = RandomUtils.nextInt(0, pizza.length);
            int col = RandomUtils.nextInt(0, pizza[0].length);
            cell = pizza[row][col];
        } while (cell.isCut() || cell.isWasted());
        return cell;
    }

    /**
     * Gets a pseudo-random cell, with the following logic:
     *
     * Finds all cells within reach:
     *
     * X (vertical) by using first cell as starting point and going up / down
     * of a random quantity between zero and the max size of a slice
     * (in which case we get a vertical slice of correct max size). If X exceeds pizza boundary,
     * the value is set to the value of that boundary (either 0 or pizza height)
     *
     * Y (horizontal) considering the current area of the slice, so the minimum we
     * can move is 0 and the maximum will be max slice size / current selection size (rounded to
     * previous integer), minus one (e.g max size is 6, previous area is 3, then we can move by
     * 6 / 3 - 1 = 1 column left or right. If max size is 6, previous area is 4, we can move by 6 / 4 - 1 = 0)
     *
     * And then returns a random one. Even cells that are empty are candidates, because with the genetic
     * algorithm a worst fitting individual can still encode a good gene
     *
     * @param firstCell the starting point
     * @return a cell
     */
    public Cell getRandomEndingCell(Cell firstCell) {
        List<Cell> available = new ArrayList<>();

        int rowMin = firstCell.getX() - maxSize;
        rowMin = rowMin < 0 ? 0 : rowMin;

        int rowMax = firstCell.getX() + maxSize;
        rowMax = rowMax > pizza.length ? pizza.length : rowMax;

        for (int i = rowMin; i < rowMax; i++) {
            int selSize = getSelectionSize(firstCell, new Cell(i, firstCell.getY()));
            int absYMove = ((int) Math.round((double) maxSize / selSize));

            int colMin = firstCell.getY() -  absYMove;
            colMin = colMin < 0 ? 0 : colMin;

            int colMax = firstCell.getY() + absYMove;
            colMax = colMax > pizza[0].length ? pizza[0].length : colMax;

            available.addAll(Arrays.asList(pizza[i]).subList(colMin, colMax));
        }
        return available.stream().filter(cell -> hasMinIngredients(firstCell, cell))
                .filter(cell -> getSelectionSize(firstCell, cell) <= maxSize)
                .findAny().orElse(null);
    }

    /**
     * Indicates if there is no pizza left
     *
     * @return is the pizza empty?
     */
    public boolean isEmpty() {
        return getSurface() == cut + waste;
    }

    public int getSelectionSize(Cell start, Cell end) {
        int dX = Math.abs(end.getX() - start.getX()) + 1;
        int dY = Math.abs(end.getY() - start.getY()) + 1;
        return dX * dY;
    }

    public boolean hasMinIngredients(Cell start, Cell end) {
        return getToppingCount(TOMATO, start, end) >= minIngredient && getToppingCount(MUSHROOM, start, end) >= minIngredient;
    }

    /**
     * Calculates if the selection is valid
     *
     * @param start start of the selection
     * @param end end of the selection
     * @return validity of the selection (t/f)
     */
    public boolean isValidSelection(Cell start, Cell end) {
        return getSelectionSize(start, end) < maxSize &&
                !hasEmptyCells(start, end) &&
                getToppingCount(TOMATO, start, end) >= minIngredient &&
                getToppingCount(MUSHROOM, start, end) >= minIngredient;
    }

    /**
     * Calculates if the current selection has empty cells (thus overlaps with other pizza cells)
     *
     * @param from selection start
     * @param to selection end
     * @return boolean
     */
    private boolean hasEmptyCells(Cell from, Cell to) {
        int startX = from.getX() < to.getX() ? from.getX() : to.getX();
        int startY = from.getY() < to.getY() ? from.getY() : to.getY();
        int endX = from.getX() > to.getX() ? from.getX() : to.getX();
        int endY = from.getY() > to.getY() ? from.getY() : to.getY();

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (pizza[x][y].getTopping() == null)
                    return true;
            }
        }
        return false;
    }

    /**
     * Calculates how many toppings there are in a determined slice of pizza
     *
     * @param topping type of topping
     * @param from starting cell of the selection
     * @param to ending cell of the selection
     * @return the number of toppings
     */
    private Integer getToppingCount(Topping topping, Cell from, Cell to) {
        int startX = from.getX() < to.getX() ? from.getX() : to.getX();
        int startY = from.getY() < to.getY() ? from.getY() : to.getY();
        int endX = from.getX() > to.getX() ? from.getX() : to.getX();
        int endY = from.getY() > to.getY() ? from.getY() : to.getY();
        int partial = 0;

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (pizza[x][y].getTopping() == topping)
                    partial++;
            }
        }
        return partial;
    }

    public String getSliceDesc() {
        StringBuilder sb = new StringBuilder("~~~~~~~ HERE IS YOUR PIZZA ~~~~~~~\n");
        for (Cell[] row: pizza) {
            for (Cell cell: row) {
                if (cell.isWasted()) {
                    sb.append(WASTED_PIZZA);
                }
                sb.append(getPizzaChar(cell.getSlice()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Utility method to convert number of slice to a character
     * for the pizza slices map
     *
     * @param lastSlice number of last slice
     * @return string representation of slice
     */
    private String getPizzaChar(int lastSlice) {
        return "(" + String.format("%05d", lastSlice) + ")";
    }

    /**
     * Utility method to convert topping type to a character
     * for the pizza slices map
     *
     * @param t the topping type
     * @return a string representing the topping
     */
    private String getToppingChar(Topping t) {
        switch (t) {
            case MUSHROOM:
                return "(  M  )";
            case TOMATO:
                return "(  T  )";
        }
        return "(     )";
    }
}
