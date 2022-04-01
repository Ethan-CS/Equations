package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    private void chainRule() {
        for (Vertex v : tuple.getVertices()) {
            Tuple t = tuple.clone();
            t.remove(v);
            derive(v, t);
        }
    }

    private void derive(Vertex vDot, Tuple otherTerms) {
        System.out.println("v-dot: " + vDot + ", other terms: " + otherTerms);
        // Get neighbours of v
        List<Vertex> neighbours = this.graph.getNeighbours(vDot);

        findExitTerms(vDot, otherTerms, neighbours);
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

    private void findExitTerms(Vertex v, List<Vertex> otherTerms, List<Vertex> neighbours) {
        Graph transitionGraph = this.getModelParams().getTransitionGraph();
        Graph filterGraph = this.modelParams.getFilterGraph();

        int vState = modelParams.getFilterStates().indexOf(v.getState());
        // Store a list of states that, if a neighbouring vertex is in them,
        // would mean we leave the state represented by the current tuple.
        Map<Character, Double> exitStates = new HashMap<>();
        // How do we exit this state?
        // Option 1: state needs another vertex to leave, i.e. state is in filter graph
        if (modelParams.getFilterStates().contains(v.getState())) {
            addNeighbourExitTransitions(v, otherTerms, neighbours, vState, exitStates);
        }
        // Option 2: can exit state without interaction with another state, e.g. I->R
        if (this.getModelParams().getToExit()[vState] == 1) {
            exitStates.clear();
            addIsolatedExitTransitions(v, otherTerms, vState, exitStates);
        }
    }

    private void addIsolatedExitTransitions(Vertex v, List<Vertex> otherTerms, int vState, Map<Character, Double> exitStates) {
        Graph transitionGraph = this.getModelParams().getTransitionGraph();

        for (int i = 0; i < transitionGraph.getNumVertices(); i++) {
            if (transitionGraph.hasEdge(vState, i) && getModelParams().getToEnter()[i] == 1) {
                exitStates.put(transitionGraph.getLabels().get(i), modelParams.getRatesMatrix()[vState][i]);
            }
        }
        // Loop through our found exit-inducing states
        for (Character exitState : exitStates.keySet()) {
            if (exitStates.get(exitState) != 0) { // if rate of transition is non-zero
                // Loop through neighbours of v
                // We exit this state regardless of neighbours
                if (!otherTerms.contains(v)) otherTerms.add(v);
                System.out.println("ADDING -" + exitStates.get(exitState) + otherTerms);
                terms.put(new Tuple(otherTerms), -exitStates.get(exitState));
            }
        }
    }

    private void addNeighbourExitTransitions(Vertex v, List<Vertex> otherTerms, List<Vertex> neighbours, int vState, Map<Character, Double> exitStates) {
        Graph filterGraph = this.modelParams.getFilterGraph();

        for (Vertex filterVertex : filterGraph.getVertices()) {
            if (filterGraph.hasDirectedEdge(vState, filterVertex.getLocation())) {
                int otherState = filterVertex.getLocation();
                exitStates.put(modelParams.getFilterStates().get(filterVertex.getLocation()),
                        modelParams.getRatesMatrix()[vState][otherState]);
            }
        }
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
    }

    public Graph getGraph() {
        return graph;
    }

    private void removeUnbiologicalTerms() {

    }
}
