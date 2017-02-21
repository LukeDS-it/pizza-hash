package it.karatekide.pizza.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Slicer {

    private List<Node> nodes;
    private Node root;
    private Pizza pizza;

    public Slicer(Pizza pizza) {
        this.pizza = pizza;
        nodes = new ArrayList<>();
        root = new Node(pizza.getFirstCell(), this);
    }

    Pizza getPizza() {
        return pizza;
    }

    public Slice getSlice() {
        processNode(root);
        Optional<Node> candidate = nodes.stream().filter(Node::isFeasible).max(Node::compareTo);

        if (candidate.isPresent() && candidate.get() != root) {
            return pizza.cut(candidate.get());
        }
        pizza.waste(root.startX, root.startY);
        return null;
    }

    private void processNode(Node node) {
        if (node != null) {
            Optional<Node> old = nodes.stream().filter(n -> n.equals(node)).findFirst();
            if (old.isPresent()) { // If I already visited the node
                Node oldNode = old.get();
                // The weight of the node is the worse weight between the two
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
