package io.github.ethankelly.simulation;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Rand;
import io.github.ethankelly.graph.Vertex;

import java.util.*;

/**
 * Models the spread of disease through simulated, averaged behaviour on a contact network.
 */
public class Simulation {
    /** The underlying transmission graph. */
    private final Graph g;
    /** Mapping of model states to lists of vertices in those states. */
    private final Map<State, List<Vertex>> stateMap;
    /** Mapping of individual vertices to integer-valued recovery times. */
    private final Map<Vertex, Integer> recovery;
    /** Mapping of time-steps to the state map at that time-step in the simulation. */
    private final List<Map<State, List<Vertex>>> results;
    /** Mapping of time-steps to the percentage of the transmission graph in each model state at that time. */
    private final List<Map<State, Double>> resultsSummary;

    private final double weightBound = 0.5;

    private final double poissonLambda = 3;

    /**
     * Enumerated class containing model states.
     */
    enum State {
        SUSCEPTIBLE,
        INFECTED,
        RECOVERED
    }

    public Simulation(Graph g) {
        this.g = g;

        recovery = new HashMap<>();
        stateMap = new HashMap<>();
        results = new ArrayList<>();
        resultsSummary = new ArrayList<>();

        // All vertices start as susceptible, initialise other state map entries
        getStateMap().put(State.SUSCEPTIBLE, g.getVertices());
        getStateMap().put(State.INFECTED, new ArrayList<>());
        getStateMap().put(State.RECOVERED, new ArrayList<>());

        // Uniformly random probability of infection and recovery for each individual
        for (Vertex v : g.getVertices()) {
            getRecovery().put(v, Rand.poisson(poissonLambda));
        }
    }

    public Map<State, List<Vertex>> getStateMap() {
        return stateMap;
    }

    public Map<Vertex, Integer> getRecovery() {
        return recovery;
    }

    public static void main(String[] args) {
        Graph g = GraphGenerator.cycle(5);
        Simulation sim = new Simulation(g);
        sim.randWeights();

        System.out.println(sim.getG());

        sim.beginSimulation(2);

        String results = printResults(sim.getResults());

        System.out.println("Results\n" + results + "\n" + sim.getResultsSummary());
    }

    public static String printResults(List<Map<Simulation.State, List<Vertex>>> res) {
        StringBuilder results = new StringBuilder();
        for (Map<State, List<Vertex>> time : res) {
            results.append("t=").append(res.indexOf(time)).append(": ");
            for (State s : time.keySet()) {
                results.append(s).append(": ").append(time.get(s)).append(", ");
            }
            results.append("\n");
        }
        return String.valueOf(results);
    }

    public void randWeights() {
        Random r = new Random();
        for (int i = 0; i < g.getNumVertices(); i++) {
            for (int j = 0; j < g.getNumVertices(); j++) {
                if (g.hasDirectedEdge(i, j)) g.addWeight(i, j, Rand.uniform(0, weightBound));
            }
        }
    }

    public Graph getG() {
        return g;
    }

    public void beginSimulation(List<Vertex> infected) {
        infected.forEach(this::infect);
        beginSimulation();
    }

    public void beginSimulation(int numInitialInfected) {
        // Uniformly randomly select vertices to begin infection
        for (int i = 0; i < numInitialInfected; i++) {
            int count = 0;
            while (count < getG().getNumVertices()) {
                int infect = Rand.uniform(0, getG().getNumVertices());
                Vertex toInfect = new Vertex(infect);
                if (getStateMap().get(State.SUSCEPTIBLE).contains(toInfect)) {
                    infect(toInfect);
                    break;
                }
                count++;
            }
        }
        beginSimulation();
    }

    private void beginSimulation() {
        // Deep copy of current state map field value for initial state of simulation
        HashMap<State, List<Vertex>> copy = new HashMap<>();
        getStateMap().forEach((key, value) -> copy.put(key, new ArrayList<>(value)));
        getResults().add(copy);
        // Run the simulations until no more dynamically impactful vertices
        simulate();
    }

    public List<Map<State, List<Vertex>>> getResults() {
        return results;
    }

    public List<Map<State, Double>> getResultsSummary() {
        return resultsSummary;
    }

    // Infect the specified vertex
    private void infect(Vertex v) {
        List<Vertex> susceptible = new ArrayList<>(getStateMap().get(State.SUSCEPTIBLE));
        boolean removed = susceptible.remove(v);
        this.getStateMap().replace(State.SUSCEPTIBLE, susceptible);
        if (removed) this.getStateMap().get(State.INFECTED).add(v);
    }

    // Run the simulation until nothing more can happen (i.e. no further active infections).
    public void simulate() {
        int t = 0;
        do {
            t++;
            // Maintain lists of vertices to move from susceptible to infected and infected to recovered
            List<Vertex> toInfect = new ArrayList<>(), toRecover = new ArrayList<>();
            // Loop through all infected vertices
            for (Vertex v : getStateMap().get(State.INFECTED)) {
                // Does it recover?
                if (getRecovery().get(v) < 0) {
                    toRecover.add(v);
                    break;
                } else {
                    getRecovery().put(v, getRecovery().get(v) - 1);
                }
                // Loop through each of neighbours of infected vertex
                for (Vertex w : getG().getNeighbours(v)) {
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
            getResults().add(copy);

            // Write a summary of results - percent in S, I and R mapped to current time value
            Map<State, Double> summary = new HashMap<>();
            summary.put(State.SUSCEPTIBLE, 100 * ((double) copy.get(State.SUSCEPTIBLE).size() / g.getNumVertices()));
            summary.put(State.INFECTED, 100 * ((double) copy.get(State.INFECTED).size() / g.getNumVertices()));
            summary.put(State.RECOVERED, 100 * ((double) copy.get(State.RECOVERED).size() / g.getNumVertices()));
            // Store in results summary
            getResultsSummary().add(summary);
        } while (getStateMap().get(State.INFECTED).size() != 0);
    }

    private boolean infects(Vertex infected, Vertex susceptible) {
        return getStateMap().get(State.SUSCEPTIBLE).contains(susceptible) &&
                getStateMap().get(State.INFECTED).contains(infected) &&
                Rand.uniform() < getG().getAdjList().get(infected.getLocation()).get(susceptible);
    }

    private void recover(Vertex v) {
        List<Vertex> infected = new ArrayList<>(getStateMap().get(State.INFECTED));
        boolean removed = infected.remove(v);
        this.getStateMap().replace(State.INFECTED, infected);
        if (removed) this.getStateMap().get(State.RECOVERED).add(v);
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
