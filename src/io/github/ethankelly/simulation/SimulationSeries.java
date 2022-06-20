package io.github.ethankelly.simulation;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.model.ModelParams;
import io.github.ethankelly.model.ODESystem;
import io.github.ethankelly.model.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationSeries {
    private Graph g;
    private int numInitialInfections;
    private List<List<Map<Simulation.State, List<Vertex>>>> allResults = new ArrayList<>();
    // number of infections at each time step in each simulation
    private List<List<Integer>> numInfected;
    // Field -> Current average (median) of simulation results
    private double[] simulationResults;
    private List<Vertex> initialInfections;
    // Field -> highest 5% of results (part of envelope)?
    // Field -> lowest 5% of results (part of envelope)?

    // Constructor - assign derived solution and initial conditions
    public SimulationSeries(Graph g, int numInitialInfections) {
        this.g = g;
        this.numInitialInfections = numInitialInfections;
        this.numInfected = new ArrayList<>();
    }

    public SimulationSeries(Graph g, List<Vertex> initialInfections) {
        this.g = g;
        this.initialInfections = initialInfections;
        this.numInitialInfections = initialInfections.size();
        this.numInfected = new ArrayList<>();
    }

    // Public method - run specified number of simulations
    public void runSimulations(int numSimulations) {
        for (int i = 0; i < numSimulations; i++) {
            List<Integer> infections;
            Simulation s = new Simulation(g);
            s.randWeights();
            if (initialInfections == null || initialInfections.isEmpty()) s.beginSimulation(numInitialInfections);
            else s.beginSimulation(initialInfections);
            List<Map<Simulation.State, List<Vertex>>> results = s.getResults();
            allResults.add(results);

            infections = results.stream()
                    .map(timeStep -> timeStep.get(Simulation.State.INFECTED).size())
                    .collect(Collectors.toList());

            this.numInfected.add(infections);
        }
    }

    // Main method - run the public method that simulates until results average to analytic solution
    public static void main(String[] args) {
        // Create graph and print details
        Graph g = GraphGenerator.cycle(10);
        System.out.println(g.toStringCSV());
        System.out.println(g.getCCs().size());

        // Simulations
        SimulationSeries ss = new SimulationSeries(g, new ArrayList<>(Collections.singletonList(new Vertex(1))));
        ss.runSimulations(100);

        StringBuilder csv = new StringBuilder();
        for (List<Integer> list : ss.numInfected) {
            csv.append("\n");
            for (int j = 0; j < list.size() - 1; j++) { // to size-1 to avoid adding 0 on end of every row
                csv.append(list.get(j));
                if (j + 2 < list.size()) csv.append(",");
            }
        }
        System.out.println(csv);

        // Equations
        ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);

        ODESystem ode = new ODESystem(g, 100, SIR);
        ode.reduce();
        Map<Tuple, Integer> map = ode.getIndicesMapping();
        double[] y = new double[map.size()];
        double[] yDot = new double[map.size()];

        for (Vertex v : ss.initialInfections) {
            for (Tuple t : map.keySet()) {
                if (t.contains(v)) {
                    y[ode.getIndicesMapping().get(t)] = 0.5;
                }
            }
        }
        ode.computeDerivatives(0, y, yDot);
//        for (int i = 1; i < 25; i++) {
//            // TODO how is this supposed to work??
//            ode.computeDerivatives(i, ode.getyDotResults().get((double) i-1), ode.getyDotResults().get((double) i-1));
//        }
//
//        DecimalFormat df = new DecimalFormat(".00");
//        for (Double d : ode.getyDotResults().keySet()) {
//            System.out.print(d + ",");
//            for (double res : ode.getyDotResults().get(d)) {
//                System.out.print(df.format(res)+",");
//            }
//            System.out.println();
//        }
//    }
    }
}
