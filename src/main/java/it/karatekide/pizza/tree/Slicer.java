package it.karatekide.pizza.tree;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The slicer is the class that handles the pizza slicing
 * and keeps track of the simulation tree
 *
 * @author Luca Di Stefano
 */
public class Slicer {

    private List<Node> nodes;
    private Node root;

    @Getter
    private Pizza pizza;

    /**
     * Creates a new pizza slicer and instantiates the root node
     * (that will recursively instantiate the whole hierarchy)
     * and creates the new node list.
     * @param pizza the whole pizza reference
     */
    public Slicer(Pizza pizza) {
        this.pizza = pizza;
        nodes = new ArrayList<>();
        root = new Node(pizza.getFirstCell(), this);
    }

    /**
     * Tries to cut a slice and if successful returns it, otherwise
     * it marks the starting cell as wasted, because it means it can
     * not be used in any other best solution.
     *
     * To cut a slice it first processes all the nodes of the tree, then
     * deletes all unfeasible nodes and gets the one that has the highest
     * weight (filtering seems redundant, but it actually removes a lot of
     * comparisons).
     *
     * @return a slice, if found, or null.
     */
    public Slice getSlice() {
        processNode(root);
        Optional<Node> candidate = nodes.stream().filter(Node::isFeasible).max(Node::compareTo);

        if (candidate.isPresent() && candidate.get() != root) {
            return pizza.cut(candidate.get());
        }
        pizza.waste(root.startX, root.startY);
        return null;
    }

    /**
     * Processes the current node.
     * If the node is not null (condition of loop exit)
     * we check if we already have a similar solution in our list.
     * This can actually happen, for example if we go from cell 0 to right and down
     * or from 0 down and right.
     * If we didn't already view a similar solution, we add the current one,
     * otherwise we replace the currently existing solution with the
     * worst of the two. This is done to ensure that only the best possible solution
     * is inside the node list.
     *
     * The proceeds to process right and bottom nodes (thus if one of the two is null
     * the cycle ends and proceeds on).
     *
     * @param node the root node
     */
    private void processNode(Node node) {
        if (node != null) {
            Optional<Node> old = nodes.stream().filter(n -> n.equals(node)).findFirst();
            if (old.isPresent()) {
                Node oldNode = old.get();
                if (oldNode.getWeight() != node.getWeight() && oldNode.getWeight() > node.getWeight()) {
                    nodes.remove(oldNode);
                    nodes.add(node);
                }
            } else {
                nodes.add(node);
            }
            processNode(node.getRight());
            processNode(node.getBottom());
        }
    }
}
