package it.karatekide.pizza.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static it.karatekide.pizza.model.Topping.MUSHROOM;
import static it.karatekide.pizza.model.Topping.TOMATO;

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

    Slice cut(Node node) {
        Slice slice = new Slice(new Cell(node.startX, node.startY), new Cell(node.endX, node.endY));
        cut(slice);
        return slice;
    }

    void waste(int row, int col) {
        pizza[row][col] = null;
        sliceDesc[row][col] = WASTED_PIZZA;
        waste++;
    }

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

    private char getPizzaChar(int lastSlice) {
        int candidate = lastSlice + FIRST_CHAR;
        if (candidate <= 64 && candidate >= 58) {
            candidate += 7;
        }
        return (char) (candidate);
    }

    Cell getFirstCell() {
        for (int row = 0; row < pizza.length; row++) {
            for (int col = 0; col < pizza[row].length; col++) {
                if (pizza[row][col] != null)
                    return new Cell(row, col);
            }
        }
        return null;
    }

    private int getToppings(Topping topping) {
        return allToppings.get(topping);
    }

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

    int getRemainingToppings(Topping topping, Cell start, Cell end) {
        return getToppings(topping) - getToppingCount(topping, start, end);
    }

    Cell getRightNode(Cell c) {
        if (pizza[c.x].length > c.y + 1 && pizza[c.x][c.y + 1] != null)
            return new Cell(c.x, c.y + 1);
        return null;
    }

    Cell getBottomNode(Cell c) {
        if (pizza.length > c.x + 1 && pizza[c.x + 1][c.y] != null)
            return new Cell(c.x + 1, c.y);
        return null;
    }

    public boolean isEmpty() {
        boolean tmp = true;

        for (Topping[] pRow : pizza) {
            for (Topping pCol : pRow) {
                tmp = (tmp && pCol == null);
            }
        }

        return tmp;
    }

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

    public int getSurface() {
        return pizza.length * pizza[0].length;
    }

    public int getUsed() {
        return getSurface() - waste;
    }
}
