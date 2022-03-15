package io.github.ethankelly.experiments;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.model.ModelParams;
import io.github.ethankelly.model.RequiredTuples;

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

        Integer[][] results = new Integer[(int) (pMax*100)][iterations];
        Double[] prob = new Double [(int) (pMax*100)];
        DecimalFormat twoDecimalPlaces = new DecimalFormat("0.00");
        DecimalFormat upToTwoDecimalPlaces = new DecimalFormat("#.##");

        try (PrintWriter writer = new PrintWriter("data.csv")) {
            StringBuilder sb = new StringBuilder();
            sb.append("experiment_number,probability,result\n");
            float p = (float) 0.01;
            while (p<=pMax+0.001) {
                System.out.println("Probability: " + p);
                int col = (int) (p*100-1); // Get the index of current probability
                prob[col] = Double.parseDouble(twoDecimalPlaces.format(p));
                for (int i = 0; i < iterations; i++) {
                    Graph er = GraphGenerator.erdosRenyi(numVertices,p);
                    RequiredTuples rq = new RequiredTuples(er, m, false);
                    int numTuples = rq.genTuples().size();
                    results[col][i] = numTuples;
                    sb.append((i+1)).append(",").append(twoDecimalPlaces.format(p))
                            .append(",").append(numTuples).append("\n");
                }
                p=Float.parseFloat(twoDecimalPlaces.format(p+0.01));
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }

        for (int i = 0; i < results.length; i++) {
            System.out.println(twoDecimalPlaces.format(prob[i]) + ": " + Arrays.toString(results[i]));
        }
    }
}
