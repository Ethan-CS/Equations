package io.github.ethankelly.simulation;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulationSeries {

    private Graph g;

    private int initialInfections;

    private List<List<Map<Simulation.State, List<Vertex>>>> allResults = new ArrayList<>();
    // number of infections at each time step in each simulation
    private List<List<Integer>> numInfected;

    // Field -> Analytically derived solution
    private double[] exactSolution;
    // Field -> Current average (median) of simulation results
    private double[] simulationResults;
    // Field -> highest 5% of results (part of envelope)
    // Field -> lowest 5% of results (part of envelope)

    // Constructor - assign derived solution, margin of error and initial conditions
    public SimulationSeries(Graph g, int initialInfections) {
        this.g = g;
        this.initialInfections = initialInfections;
        this.numInfected = new ArrayList<>();
    }
    // Public method - run specified number of simulations
    public void runSimulations(int numSimulations) {
        for (int i = 0; i < numSimulations; i++) {
            List<Integer> infections;
            Simulation s = new Simulation(g);
            s.randWeights();
            s.beginSimulation(initialInfections);
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
        Graph er = GraphGenerator.erdosRenyi(50, 0.3);
        SimulationSeries ss = new SimulationSeries(er, 3);
        ss.runSimulations(100);

        StringBuilder csv = new StringBuilder();
        for (List<Integer> list : ss.numInfected) {
            csv.append("\n");
            for (int j = 0; j < list.size()-1; j++) { // to size-1 to avoid adding 0 on end of every row
                csv.append(list.get(j));
                if (j+2 < list.size()) csv.append(",");
            }
        }
        System.out.println(csv);
    }
}
