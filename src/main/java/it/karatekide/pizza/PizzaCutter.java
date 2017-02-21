package it.karatekide.pizza;

import it.karatekide.pizza.model.Pizza;
import it.karatekide.pizza.model.Slice;
import it.karatekide.pizza.model.Slicer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;

public class PizzaCutter {

    private final static String dirName = "C:/Users/distefanol/Downloads/";

    public static void main(String... args) throws IOException {
        String baseFile = "big";
        String inFile = dirName + separator + baseFile + ".in";
        String pizzaMap = FileUtils.readFileToString(new File(inFile), "UTF-8");

        Pizza pizza = new Pizza(pizzaMap);
        List<Slice> slices = new ArrayList<>();
        while (!pizza.isEmpty()) {
            Slicer slicer = new Slicer(pizza);
            Slice s = slicer.getSlice();
            if (s != null)
                slices.add(s);
        }

        StringBuilder sb = new StringBuilder()
                .append(slices.size())
                .append("\n");
        slices.forEach(sb::append);

        if (slices.size() < 90) {
            System.out.println(pizza.getSliceDesc());
        }

        System.out.println("Slices: " + slices.size());
        int used = pizza.getUsed();
        int total = pizza.getSurface();
        double percent = (double) Math.round(((double) used*10000/total)) / 100;
        System.out.println("Used: " + percent + "%");
        System.out.println("Th. Score: " + used);
        System.out.println("Waste: " + pizza.getWaste());

        String outFile = dirName + separator + baseFile + ".out";
        FileUtils.writeStringToFile(new File(outFile), sb.toString(), "UTF-8");
    }

}
