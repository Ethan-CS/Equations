package io.github.ethankelly.experiments;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.model.ModelParams;
import io.github.ethankelly.model.ODESystem;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        int iterations = 50;
        int numVertices = 15;
        double pMax = 0.20;

        Integer[][] regResults = new Integer[(int) (pMax*100)][iterations];
        Integer[][] redResults = new Integer[(int) (pMax*100)][iterations];
        Double[] prob = new Double [(int) (pMax*100)];
        DecimalFormat twoDecimalPlaces = new DecimalFormat("0.00");
        DecimalFormat upToTwoDecimalPlaces = new DecimalFormat("#.##");

        StringBuilder reg = new StringBuilder();
        StringBuilder red = new StringBuilder();
        reg.append("experiment_number,probability,result\n");
        red.append("experiment_number,probability,result\n");
        float p = (float) 0.01;
        while (p<=pMax+0.001) {
            System.out.println("Probability: " + p);
            int col = (int) (p*100-1); // Get the index of current probability
            prob[col] = Double.parseDouble(twoDecimalPlaces.format(p));
            for (int i = 0; i < iterations; i++) {
                // Generate an ER graph
                Graph er = GraphGenerator.erdosRenyi(numVertices,p);

                // Regular system
                ODESystem system = new ODESystem(er.clone(), 10, m);
                regResults[col][i] = system.getTuples().size();
                reg.append((i+1)).append(",").append(twoDecimalPlaces.format(p))
                        .append(",").append(system.getTuples().size()).append("\n");

                // Reduced system
                ODESystem reducedSystem = new ODESystem(er.clone(), 10, m);
                reducedSystem.reduce();
                redResults[col][i] = reducedSystem.getTuples().size();
                red.append((i+1)).append(",").append(twoDecimalPlaces.format(p))
                        .append(",").append(reducedSystem.getTuples().size()).append("\n");
            }
            p=Float.parseFloat(twoDecimalPlaces.format(p+0.01));
        }

        try (PrintWriter regWriter = new PrintWriter("data-reg.csv")) {
            regWriter.write(reg.toString());
            try (PrintWriter redWriter = new PrintWriter("data-red.csv")) {
                redWriter.write(red.toString());
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }

        for (int i = 0; i < regResults.length; i++) {
            System.out.println(twoDecimalPlaces.format(prob[i]) + ": " + Arrays.toString(regResults[i]));
            System.out.println(twoDecimalPlaces.format(prob[i]) + ": " + Arrays.toString(redResults[i]));
        }
    }
}
