package it.karatekide.pizza.genetic;

import it.karatekide.pizza.model.Cell;
import it.karatekide.pizza.model.Pizza;
import it.karatekide.pizza.model.Slice;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Double.MAX_VALUE;

/**
 * An individual is a solution for the problem.
 * An individual can mate with other individuals, generating a new individual.
 * <p>
 * Each individual contains a set of genes and a reference to the pizza we're
 * considering, in order to calculate the fit of the individual.
 *
 * @author Luca Di Stefano
 */
public class Individual implements Comparable<Individual> {

    @Getter
    private int generation = 0;

    @Getter
    private List<Gene> genes = new ArrayList<>();
    private Pizza pizza;

    /**
     * Creates a new individual of zeroth generation
     *
     * @param pizza the pizza to slice
     */
    Individual(Pizza pizza) {
        this(pizza, 0);
    }

    /**
     * Creates a new individual and initializes its gene pool if
     * we are in the zeroth generation
     *
     * @param pizza      the pizza to slice
     * @param generation the generation we're in (if > 0 the random initialization is skipped)
     */
    private Individual(Pizza pizza, int generation) {
        this.pizza = new Pizza(pizza);
        this.generation = generation;
        if (generation == 0)
            initGenePool();
    }

    /**
     * This says how well the individual fits in the possible solution of the problem
     *
     * @return a score
     */
    double getFit() {
        if (genes.size() == 0)
            return -MAX_VALUE;

        return pizza.getUsedPizza() - overlappingGenes();
    }

    /**
     * Makes this individual mate with another individual, generating an offspring
     *
     * @param mate the other individual
     * @return a new individual
     */
    Individual mate(Individual mate) {
        int g = generation + 1;
        Individual i = new Individual(pizza, g);

        Stream.concat(genes.stream(), mate.genes.stream())
                .filter((gene) -> randomFilter())
                .forEach(i::addParentGene);

        i.mutate();
        i.cleanGenePool();

        return i;
    }

    private void addParentGene(Gene gene) {
        genes.add(gene);
        pizza.cut(gene.getStart(), gene.getEnd());
    }

    /**
     * Initialises the gene pool with a possible solution
     */
    private void initGenePool() {
        while (!pizza.isEmpty()) {
            genes.add(createRandomGene());
        }
        cleanGenePool();
    }

    private void cleanGenePool() {
        genes = genes.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Randomly returns true or false to see if this gene has to be added
     *
     * @return true or false
     */
    private boolean randomFilter() {
        return Math.random() < 0.5;
    }

    /**
     * Adds a random mutation to the gene pool
     */
    private void mutate() {
        if (Math.random() < 0.2 && !pizza.isEmpty())
            genes.add(createRandomGene());
    }

    /**
     * Creates a random gene (slice of pizza), taking a first random non empty cell,
     * and a second one, also at random, but getting a slice that has "sense", which means
     * that fulfills the basic area and min ingredients constraints.
     *
     * @return a random gene
     */
    private Gene createRandomGene() {
        Cell firstCell = pizza.getRandomCell();
        Cell secondCell = pizza.getRandomEndingCell(firstCell);

        if (secondCell == null) {
            pizza.waste(firstCell);
            return null;
        } else {
            Slice s = pizza.cut(firstCell, secondCell);
            return new Gene(s.getStart(), s.getEnd());
        }
    }

    /**
     * Checks if the current solution has two slices that overlap
     *
     * @return true/false
     */
    private boolean hasOverlappingGenes() {
        boolean tmp = false;
        for (Gene g : genes) {
            for (Gene g1 : genes) {
                tmp = tmp || g.overlaps(g1);
            }
        }
        return tmp;
    }

    /**
     * Counts how many genes code for overlapping slices
     *
     * @return a number that will make the score drop
     */
    private double overlappingGenes() {
        double tmp = 0;
        for (Gene g : genes) {
            for (Gene g1 : genes) {
                if (g.overlaps(g1))
                    tmp++;
            }
        }
        return tmp;
    }

    /**
     * Counts how many genes code for slices that do not have the
     * minimum required ingredients
     *
     * @return a number that will make the score drop
     */
    private double noMinIngredients() {
        double tmp = 0;
        for (Gene g : genes) {
            if (!pizza.hasMinIngredients(g.getStart(), g.getEnd()))
                tmp++;
        }
        return tmp;
    }

    @Override
    public int compareTo(Individual o) {
        return Double.compare(getFit(), o.getFit());
    }

    int reverseCompare(Individual o) {
        return Double.compare(o.getFit(), getFit());
    }
}
