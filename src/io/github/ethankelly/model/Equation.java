package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;

import java.util.*;

public class Equation {
    private final Tuple tuple;
    private final Map<Tuple, Double> terms;
    private final ModelParams modelParams;
    private final Graph graph;

    public Equation(Tuple tuple, ModelParams modelParams, Graph graph) {
        this.tuple = tuple;
        this.terms = new HashMap<>();
        this.modelParams = modelParams;
        this.graph = graph;
        findTerms();
    }

    public void findTerms() {
        chainRule();
        removeUnbiologicalTerms();
    }

    private void chainRule() {
        for (Vertex v : tuple.getVertices()) {
            Tuple t = tuple.clone();
            t.remove(v);
            derive(v, t);
        }
    }

    private void derive(Vertex vDot, Tuple otherTerms) {
        findTerms(vDot, otherTerms);
        /*
         Get neighbours and filter graph
         Apply adjacent pairs of states in filter to tuples of v-dot and neighbours
           (add other terms back in)
         Add terms constituting valid tuples, mapped to relevant transition rates (beta/tau, gamma)
            + -> leads into state represented by tuple field
            - -> leads out of this state
        */
    }

    public ModelParams getModelParams() {
        return modelParams;
    }

    private void findTerms(Vertex v, List<Vertex> otherTerms) {
        int vState = modelParams.getFilterStates().indexOf(v.getState());

        // How do we exit this state?
        // Option 1: state needs another vertex to leave, i.e. state is in filter graph
        if (modelParams.getFilterStates().contains(v.getState())) {
            addNeighbourExitTransitions(v, otherTerms, vState);
            addNeighbourEntryTransitions(v, otherTerms, vState);
        }
        // Option 2: can exit state without interaction with another state, e.g. I->R
        if (this.getModelParams().getToExit()[vState] == 1) {
            addIsolatedExitTransitions(v, otherTerms, vState);
            addIsolatedEntryTransitions(v, otherTerms, vState);
        }

//        removeUnbiologicalTerms();
    }

    private void addIsolatedEntryTransitions(Vertex v, List<Vertex> otherTerms, int vState) {
        // Store a list of states that, if a neighbouring vertex is in them,
        // would mean we leave the state represented by the current tuple.
        Map<Character, Double> entryStates = new HashMap<>();
        getIsolatedEntryStates(vState, entryStates);
        // Loop through our found exit-inducing states
        for (Character entryState : entryStates.keySet()) {
            if (entryStates.get(entryState) != 0) { // if rate of transition is non-zero
                Vertex vComp = new Vertex(entryState, v.getLocation());
                otherTerms.add(vComp);// We enter this state regardless of neighbours

                System.out.println("ADDING +" + entryStates.get(entryState) + otherTerms);
                terms.put(new Tuple(otherTerms), entryStates.get(entryState));

                otherTerms.remove(vComp);
            }
        }
    }

    private void getIsolatedEntryStates(int vState, Map<Character, Double> entryStates) {
        for (int i = 0; i < this.getModelParams().getTransitionGraph().getNumVertices(); i++) {
            if (this.getModelParams().getTransitionGraph().hasDirectedEdge(i, vState) && getModelParams().getToEnter()[i] == 1) {
                entryStates.put(this.getModelParams().getTransitionGraph().getLabels().get(i), modelParams.getRatesMatrix()[i][vState]);
            }
        }
    }

    private void addIsolatedExitTransitions(Vertex v, List<Vertex> otherTerms, int vState) {
        // Store a list of states that, if a neighbouring vertex is in them,
        // would mean we leave the state represented by the current tuple.
        Map<Character, Double> exitStates = new HashMap<>();
        getIsolatedExitStates(vState, exitStates);
        // Loop through our found exit-inducing states
        for (Character exitState : exitStates.keySet()) {
            if (exitStates.get(exitState) != 0) { // if rate of transition is non-zero
                // We exit this state regardless of neighbours
                if (!otherTerms.contains(v)) otherTerms.add(v);
                System.out.println("ADDING -" + exitStates.get(exitState) + otherTerms);
                terms.put(new Tuple(otherTerms), -exitStates.get(exitState));
            }
        }
    }

    private void getIsolatedExitStates(int vState, Map<Character, Double> exitStates) {
        for (int i = 0; i < this.getModelParams().getTransitionGraph().getNumVertices(); i++) {
            if (this.getModelParams().getTransitionGraph().hasEdge(vState, i) && getModelParams().getToEnter()[i] == 1) {
                exitStates.put(this.getModelParams().getTransitionGraph().getLabels().get(i), modelParams.getRatesMatrix()[vState][i]);
            }
        }
    }

    private void addNeighbourEntryTransitions(Vertex v, List<Vertex> otherTerms,
                                              int vState) {
        // Store a list of states that, if a neighbouring vertex is in them,
        // would mean we enter the state represented by the current tuple.
        Map<Character, Double> entryStates = new HashMap<>();

        getNeighbourEntryStates(vState, entryStates);
        // Get neighbours of current vertex v
        List<Vertex> neighbours = this.graph.getNeighbours(v);
        // Loop through our found entry-inducing states
        for (Character entryState : entryStates.keySet()) {
            if (entryStates.get(entryState) != 0) { // if rate of transition is non-zero
                // vComp is v in the state it would be before transition to its current state
                Vertex vComp = new Vertex(entryState, v.getLocation());
                otherTerms.add(vComp);
                // Loop through neighbours of v
                for (Vertex w : neighbours) {
                    if (!otherTerms.contains(w)) {
                        // We can enter this state if any neighbours are in the entry-inducing state
                        Vertex entryVertex = new Vertex(v.getState(), w.getLocation());
                        boolean added = false; // So we know whether to remove the vertex later
                        if (!otherTerms.contains(entryVertex)) {
                            otherTerms.add(entryVertex);
                            added = true;
                        }

                        System.out.println("ADDING +" + entryStates.get(entryState) + otherTerms);
                        terms.put(new Tuple(otherTerms), entryStates.get(entryState));

                        if (added) otherTerms.remove(entryVertex);
                    }
                }
                otherTerms.remove(vComp);
            }
        }
    }

    private void getNeighbourEntryStates(int vState, Map<Character, Double> entryStates) {
        for (Vertex filterVertex : this.modelParams.getFilterGraph().getVertices()) {
            if (this.modelParams.getFilterGraph().hasDirectedEdge(filterVertex.getLocation(), vState)) {
                entryStates.put(modelParams.getFilterStates().get(filterVertex.getLocation()),
                        modelParams.getRatesMatrix()[filterVertex.getLocation()][vState]);
            }
        }
    }

    private void addNeighbourExitTransitions(Vertex v, List<Vertex> otherTerms, int vState) {
        // Store a list of states that, if a neighbouring vertex is in them,
        // would mean we leave the state represented by the current tuple.
        Map<Character, Double> exitStates = new HashMap<>();

        getNeighbourExitStates(vState, exitStates);

        // Get neighbours of current vertex v
        List<Vertex> neighbours = this.graph.getNeighbours(v);
        // Loop through our found exit-inducing states
        for (Character exitState : exitStates.keySet()) { // if rate of transition is non-zero
            if (exitStates.get(exitState) != 0) {
                // Loop through neighbours of v
                for (Vertex w : neighbours) {
                    // We could exit this state if any neighbours are in the exit-inducing state
                    Vertex exitVertex = new Vertex(exitState, w.getLocation());
                    boolean added = false; // So we know whether to remove the vertex later
                    if (!otherTerms.contains(exitVertex)) {
                        otherTerms.add(exitVertex);
                        added = true;
                    }
                    if (!otherTerms.contains(v)) otherTerms.add(v);

                    System.out.println("ADDING -" + exitStates.get(exitState) + otherTerms);
                    terms.put(new Tuple(otherTerms), -exitStates.get(exitState));

                    if (added) otherTerms.remove(exitVertex);
                }
            }
        }
    }

    private void getNeighbourExitStates(int vState, Map<Character, Double> exitStates) {
        for (Vertex filterVertex : this.modelParams.getFilterGraph().getVertices()) {
            if (this.modelParams.getFilterGraph().hasDirectedEdge(vState, filterVertex.getLocation())) {
                exitStates.put(modelParams.getFilterStates().get(filterVertex.getLocation()),
                        modelParams.getRatesMatrix()[vState][filterVertex.getLocation()]);
            }
        }
    }

    public static void main(String[] args) {
        // Tuple
        Tuple t = new Tuple(List.of(new Vertex('I', 0), new Vertex('S', 1)));

        // Model Parameters
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);

        // Graph (lollipop)
        Graph g = GraphGenerator.getLollipop();

        // Generate equation
        Equation eqn = new Equation(t, m, g);

        System.out.println(eqn);
    }

    public Graph getGraph() {
        return graph;
    }

    private void removeUnbiologicalTerms() {
        List<Tuple> unbiological = new ArrayList<>();
        for (Tuple t : terms.keySet()) {
            if (!t.locationsAreDifferent()) unbiological.add(t);
        }
        unbiological.forEach(terms::remove);
    }

    @Override
    public String toString() {
        System.out.println(terms);
        StringBuilder s = new StringBuilder();
        s.append(tuple).append(" = ");
        for (Tuple t : terms.keySet()) {
            s.append(terms.get(t)).append(t).append(" ");
        }
        return String.valueOf(s);
    }
}
