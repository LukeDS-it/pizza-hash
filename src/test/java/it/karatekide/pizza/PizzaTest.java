package it.karatekide.pizza;

import it.karatekide.pizza.model.Pizza;

import it.karatekide.pizza.model.Slice;
import it.karatekide.pizza.model.Slicer;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PizzaTest {

    private final static String FILE_EXAMPLE = "example", FILE_SMALL = "small",
            FILE_MEDIUM = "medium", FILE_BIG = "big";

    @Test
    public void testExample() throws Exception {
        test(FILE_EXAMPLE);
    }

    @Test
    public void testSmall() throws Exception {
        test(FILE_SMALL);
    }

    @Test
    public void testMedium() throws Exception {
        test(FILE_MEDIUM);
    }

    @Test
    public void testBig() throws Exception {
        test(FILE_BIG);
    }

    private void test(String baseFile) throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        URL url = cl.getResource("");
        if (url != null) {
            File inFile = new File(url.getFile() + baseFile + ".in");
            String pizzaMap = FileUtils.readFileToString(inFile, "UTF-8");
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
            double percent = (double) Math.round(((double) used * 10000 / total)) / 100;
            System.out.println("Used: " + percent + "%");
            System.out.println("Th. Score: " + used);
            System.out.println("Waste: " + pizza.getWaste());

            File outFile = new File(url.getFile() + baseFile + ".out");
            FileUtils.writeStringToFile(outFile, sb.toString(), "UTF-8");
        }
    }


}
