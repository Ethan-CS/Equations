package io.github.ethankelly.model;

import io.github.ethankelly.exceptions.UnexpectedStateException;
import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.Vertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Stores data representing the parameters of a compartmental model of disease.
 */
public class ModelParams {
    /** How many neighbours are required to enter/exit each state */
    private final int[] toEnter;
    /** How many neighbours are required to enter/exit each state */
    private final int[] toExit;
    /** List of characters representing states in the model */
    private final List<Character> states;
    /** Array of rates representing transition probabilities $A_{ij}$ for each edge $(i,j)$ */
    private double[][] ratesMatrix;
    /** Graph representing state transitions in the model */
    private Graph transitionGraph;
    /** Sub-graph of the transition graph containing only states which require 1 or more other states to induce. */
    private Graph filter;

    public ModelParams(List<Character> states, int[] toEnter, int[] toExit) {
        this.states = states;
        assert (toEnter.length == states.size()) :
                "Number of states different to the number of entry requirements provided";
        assert (toExit.length == states.size()) :
                "Number of states different to the number of exit requirements provided";
        this.toEnter = toEnter;
        this.toExit = toExit;
        this.transitionGraph = new Graph(new boolean[states.size()][states.size()], states);
        this.ratesMatrix = new double[states.size()][states.size()];
    }

    public static void main(String[] args) {
        // For now, every element in transmission matrix is equal to the relevant rate (can change based on context)
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);
        System.out.println(m);
    }

    public boolean validStates(List<Character> states, boolean closures) {
        // TODO this may be incorrect but also we should take a different approach to generation anyway -
        //  build up system rather than tear down full list of all possible combinations

        // If there are states in input that aren't in model parameters, the input states are not valid
        if (!this.states.containsAll(states) || this.states.equals(List.of('S')) && closures) return true;
        // Store whether each state in provided states has a direct transition to at least one other state in the model
        boolean[] atLeastOne = new boolean[states.size()];
        if (closures) atLeastOne[this.states.indexOf('S')] = true;
        for (char c : states) {
            // If we haven't already found a transition to/from this state, check it against each other state
            if (!atLeastOne[states.indexOf(c)]) {
                for (char d : states) {
                    // If states are not the same and there's a transition between them, update array
                    if (c != d && (this.getTransitionGraph().hasEdge(getStates().indexOf(c), getStates().indexOf(d))) &&
                        (toEnter[states.indexOf(c)]>1 || toEnter[states.indexOf(d)]>1 ||
                         toExit[states.indexOf(c)]>1 || toExit[states.indexOf(d)]>1)) {
                        atLeastOne[states.indexOf(c)] = true;
                        atLeastOne[states.indexOf(d)] = true;
                        break;
                    }
                }
                // c and d don't have a direct transition - enough to make the given states not valid.
                if (!atLeastOne[states.indexOf(c)]) return false;
            }
        }
        return true;
    }

    public Graph getTransitionGraph() {
        return transitionGraph;
    }

    public void setTransitionGraph(Graph newGraph) {
        this.transitionGraph = newGraph;
    }

    public int[] getToExit() {
        return toExit;
    }

    public int[] getToEnter() {
        return toEnter;
    }

    public double[][] getRatesMatrix() {
        return ratesMatrix;
    }

    public void setRatesMatrix(double[][] ratesMatrix) {
        this.ratesMatrix = ratesMatrix;
    }

    public List<Character> getStates() {
        return states;
    }

    public void addTransition(char from, char to, double rate) {
        assert !String.valueOf(from).equals(String.valueOf(to)) :
                "The 'to' and 'from' characters should not have been the same, but were: " + to + " = " + from;

        double[][] rates = this.getRatesMatrix().clone();
        Graph g = this.getTransitionGraph().clone();

        int indexFrom = -1;
        int indexTo = -1;
        for (int i = 0; i < this.getStates().size(); i++) {
            if (this.getStates().get(i) == from) indexFrom = i;
            if (this.getStates().get(i) == to) indexTo = i;
        }

        if (indexFrom < 0 || indexTo < 0) {
            try {
                throw new UnexpectedStateException("One (or both) of the 'to'/'from' states {" + to + ", " + from
                        + "} could not be found in the array of states.");
            } catch (UnexpectedStateException e) {
                e.printStackTrace();
            }
        } else {
            rates[indexFrom][indexTo] = rate;
            g.addDirectedEdge(indexFrom, indexTo);
        }
        this.setRatesMatrix(rates);
        this.setTransitionGraph(g);
    }

    @Override
    public String toString() {
        return this.transitionGraph + "\n";
    }

    /**
     * A filter graph is a sub-graph of the transition graph with only the edges between states that require at least
     * one other vertex being in another state to occur. Any states where entry or exit requires fewer than 2 states
     * in adjacent vertices to induce are not included in the filter graph.
     *
     * @return the filter graph of the transition graph.
     */
    public Graph getFilterGraph() {
        if (filter == null) {
            Graph contactNetwork = this.getTransitionGraph();
            List<Vertex> filterVertices = new ArrayList<>();
            List<Character> filterStates = new ArrayList<>();
            for (Vertex v : contactNetwork.getVertices()) {
                if (this.getToEnter()[contactNetwork.getVertices().indexOf(v)] == 2 ||
                        this.getToExit()[contactNetwork.getVertices().indexOf(v)] == 2) {
                    filterVertices.add(v);
                    filterStates.add(contactNetwork.getLabels().get(contactNetwork.getVertices()
                            .indexOf(v)));
                }
            }
            filter = getTransitionGraph().makeSubGraph(filterVertices);
            filter.setLabels(filterStates);

            System.out.println("ORIGINAL");
            System.out.println(this.getTransitionGraph());
            System.out.println("FILTER");
            System.out.println(filter);
        }
        return filter;
    }
}
