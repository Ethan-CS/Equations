package io.github.ethankelly.simulation;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Rand;
import io.github.ethankelly.graph.Vertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulation {
    private final Graph g;
    private Map<State, List<Vertex>> stateMap;
    private Map<Vertex, Integer> recovery;
    private Map<Vertex, Double> probability;
    private Map<Integer, Map<State, List<Vertex>>> results;
    private Map<Integer, Map<State, Double>> resultsSummary;

    public Graph getG() {
        return g;
    }

    public Map<State, List<Vertex>> getStateMap() {
        return stateMap;
    }

    public Map<Vertex, Integer> getRecovery() {
        return recovery;
    }

    public Map<Vertex, Double> getProbability() {
        return probability;
    }

    public Map<Integer, Map<State, List<Vertex>>> getResults() {
        return results;
    }

    public Map<Integer, Map<State, Double>> getResultsSummary() {
        return resultsSummary;
    }

    enum State {
        SUSCEPTIBLE,
        INFECTED,
        RECOVERED
    }

    public Simulation(Graph g) {
        this.g = g;

        recovery = new HashMap<>();
        probability = new HashMap<>();
        stateMap = new HashMap<>();
        results = new HashMap<>();
        resultsSummary = new HashMap<>();

        getStateMap().put(State.SUSCEPTIBLE, g.getVertices());
        getStateMap().put(State.INFECTED, new ArrayList<>());
        getStateMap().put(State.RECOVERED, new ArrayList<>());

        for (Vertex v : g.getVertices()) {
            getRecovery().put(v, Rand.uniform(3,8));
            getProbability().put(v, Rand.uniform());
        }
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation(GraphGenerator.cycle(5));
        System.out.println(sim.getG());
        sim.beginSimulation(2);
        System.out.println("Beginning of simulation\n" + sim);
        System.out.println(sim.getG());
        sim.simulate();
        System.out.println("End of simulation\n" + sim);
        System.out.println("Results\n" + sim.getResults() + "\n" + sim.getResultsSummary());
    }

    public void beginSimulation(int numInitialInfected) {
        for (int i = 0; i < numInitialInfected; i++) {
            int count = 0;
            while (count < getG().getNumVertices()) {
                int infect = Rand.uniform(0, getG().getNumVertices());
                Vertex toInfect = new Vertex(infect);
                if (getStateMap().get(State.SUSCEPTIBLE).contains(toInfect)) {
                    // graph field is fine here
                    infect(toInfect);
                    // now, graph field is empty??
                    break;
                }
                count++;
            }
        }
//        simulate();
    }

    public void beginSimulation(List<Vertex> infected) {
        infected.forEach(this::infect);
        simulate();
    }

    public void simulate() {
        int t = 0;
        do {
            t++;
            // Maintain lists of vertices to move from susceptible to infected and infected to recovered
            List<Vertex> toInfect = new ArrayList<>(), toRecover = new ArrayList<>();
            // Loop through all infected vertices
            for (Vertex v : getStateMap().get(State.INFECTED)) {
                // Loop through each of neighbours of infected vertex
                for (Vertex w : getG().getNeighbours(v)) {
                    // Does it recover?
                    if (getRecovery().get(v) < 0) {
                        toRecover.add(v);
                        break;
                    } else {
                        getRecovery().put(v, getRecovery().get(v) - 1);
                    }
                    // Does it infect anything?
                    if (infects(v, w)) toInfect.add(w);
                }
            }
            // Infections and recoveries
            for (Vertex v : toInfect) infect(v);
            for (Vertex v : toRecover) recover(v);

            // Deep copy of current state map field value
            HashMap<State, List<Vertex>> copy = new HashMap<>();
            getStateMap().forEach((key, value) -> copy.put(key, new ArrayList<>(value)));
            getResults().put(t, copy);

            // Write a summary of results - percent in S, I and R mapped to current time value
            Map<State, Double> summary = new HashMap<>();
            summary.put(State.SUSCEPTIBLE, 100*((double) copy.get(State.SUSCEPTIBLE).size() / g.getNumVertices()));
            summary.put(State.INFECTED, 100*((double) copy.get(State.INFECTED).size() / g.getNumVertices()));
            summary.put(State.RECOVERED, 100*((double) copy.get(State.RECOVERED).size() / g.getNumVertices()));
            // Store in results summary
            getResultsSummary().put(t, summary);
        } while (getStateMap().get(State.INFECTED).size() != 0);    }

    public void infect(Vertex v) {
        List<Vertex> susceptible = new ArrayList<>(getStateMap().get(State.SUSCEPTIBLE));
        boolean removed = susceptible.remove(v);
        this.getStateMap().replace(State.SUSCEPTIBLE, susceptible);
        if (removed) this.getStateMap().get(State.INFECTED).add(v);
    }

    public void recover(Vertex v) {
        List<Vertex> infected = new ArrayList<>(getStateMap().get(State.INFECTED));
        boolean removed = infected.remove(v);
        this.getStateMap().replace(State.INFECTED, infected);
        if (removed) this.getStateMap().get(State.RECOVERED).add(v);
    }

    private boolean infects(Vertex infected, Vertex susceptible) {
        return getStateMap().get(State.SUSCEPTIBLE).contains(susceptible) &&
                getStateMap().get(State.INFECTED).contains(infected) &&
                Rand.uniform() <= getProbability().get(infected);
    }

    /**
     * Returns a textual representation of a simulation as an adjacency list to be printed to the standard output.
     *
     * @return a string representation of the graph.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (State key : getStateMap().keySet()) {
            s.append(key).append(" -> ");
            for (Vertex v : getStateMap().get(key)) {
                s.append(v).append(" ");
            }
            s.append("\n");
        }
        s.deleteCharAt(s.length() - 1);
        return s.toString();
    }

}
