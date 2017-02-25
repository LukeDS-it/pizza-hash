package it.karatekide.pizza.genetic;

import it.karatekide.pizza.model.Pizza;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * The world simulates the population growth
 */
public class World {

    private List<Individual> population;
    private double tolerance, maxLife;
    private long maxPop;
    private Pizza pizza;

    /**
     * Creates a simulation of a world where the population cuts pizza :)
     *
     * @param pizza              the pizza to cut
     * @param startingPopulation base pool of individuals
     * @param tolerance          the result we want to obtain
     * @param maxLife            how many seconds we give the world to complete before we give up
     */
    public World(Pizza pizza, int startingPopulation, double tolerance, double maxLife) {
        this(pizza, startingPopulation, (long) startingPopulation, tolerance, maxLife);
    }

    public World(Pizza pizza, int startingPopulation, long maxPop, double tolerance, double maxLife) {
        this.tolerance = tolerance;
        this.maxLife = maxLife;
        this.maxPop = maxPop;
        this.pizza = pizza;

        System.out.println("Creating population...");
        population = IntStream.range(0, startingPopulation)
                .mapToObj(i -> new Individual(pizza))
                .sorted(Individual::reverseCompare)
                .collect(toList());
    }

    public String getResult() {
        System.out.println("The world has started!");
        double start = System.currentTimeMillis();
        double end = System.currentTimeMillis();
        Individual fittest = population.get(0);
        double fitness = fittest.getFit();

        while (fitness < tolerance && end - start < maxLife) {
            System.out.println("Fittest individual is " + fitness + " in generation " + fittest.getGeneration());
            liveGeneration();
            fittest = population.get(0);
            fitness = fittest.getFit();
            end = System.currentTimeMillis();
        }
        System.out.println("The world has ended.");
        System.out.println("Fittest individual was " + fitness + " in generation " + fittest.getGeneration());
        pizza.cutAll(fittest.getGenes().toArray(new Gene[fittest.getGenes().size()]));
        return pizza.getSliceDesc();
    }

    private void liveGeneration() {
        List<Individual> children = new ArrayList<>();

        // FIXME find a better way
        for (int i = 0; i < population.size(); i = i + 2) {
            Individual first = population.get(i);
            Individual second = population.get(i + 1);
            Individual child = first.mate(second);
            children.add(child);
        }

        population.addAll(children);
        population = population.stream()
                .sorted(Individual::reverseCompare).limit(maxPop).collect(toList());

        if (population.size() % 2 != 0) {
            population.remove(population.size() - 1); // Least fit individual dies alone
        }
    }

}
