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
    /** How many neighbours are required to enter each state */
    private final int[] toEnter;
    /** How many neighbours are required to exit each state */
    private final int[] toExit;
    /** List of characters representing states in the model */
    private final List<Character> states;
    /** Array of rates representing transition probabilities $A_{ij}$ for each edge $(i,j)$ */
    private final double[][] ratesMatrix;
    /** Graph representing state transitions in the model */
    private final Graph transitionGraph;
    /** Sub-graph of the transition graph containing only states which require 1 or more other states to induce. */
    private Graph filter;
    /** 2d string array containing symbols for printing string representations of equations, if required. */
    private final String[][] ratesForPrinting;

    /**
     * Class constructor.
     *
     * @param states the characters representing compartmental model states.
     * @param toEnter array storing the number of neighbours required to induce a transition into each state.
     * @param toExit array storing the number of neighbours required to induce a transition out of each state.
     */
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
        this.ratesForPrinting = new String[states.size()][states.size()];
    }

    /**
     * Unit testing.
     *
     * @param args command-line args, ignored.
     */
    public static void main(String[] args) {
        // For now, every element in transmission matrix is equal to the relevant rate (can change based on context)
        ModelParams m = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);
        System.out.println(m.getFilterGraph().getLabels());
    }

    /**
     * Adds a model transition between two states with the rate represented by the specified symbol.
     *
     * @param from the state in which the transition begins.
     * @param to the state in which the transition ends.
     * @param symbol the symbol representing the rate of transition.
     */
    public void addTransition(char from, char to, String symbol) {
        int indexFrom = -1;
        int indexTo = -1;
        for (int i = 0; i < this.getStates().size(); i++) {
            if (this.states.get(i) == from) indexFrom = i;
            if (this.states.get(i) == to) indexTo = i;
        }
        assert !(indexFrom < 0 || indexTo < 0) : "One (or both) of the 'to'/'from' states {" + to + ", " + from
                        + "} could not be found in the array of states.";

        this.ratesForPrinting[indexFrom][indexTo] = symbol;
        this.transitionGraph.addDirectedEdge(indexFrom, indexTo);
    }

    /**
     * @return the graph representing transitions between model states.
     */
    public Graph getTransitionGraph() {
        return transitionGraph;
    }

    /**
     * @return an array with the numbers of states required to exit each state.
     */
    public int[] getToExit() {
        return toExit;
    }

    /**
     * @return an array with the numbers of states required to enter each state.
     */
    public int[] getToEnter() {
        return toEnter;
    }

    /**
     * @return the 2d array containing the rates of transitions between model states.
     */
    public double[][] getRatesMatrix() {
        return ratesMatrix;
    }

    /**
     * @return the 2d array containing symbols representing rates of transitions between model states.
     */
    public String[][] getRatesForPrinting() {
        return this.ratesForPrinting;
    }

    /**
     * @return a list of characters representing all states in the current model.
     */
    public List<Character> getStates() {
        return states;
    }

    /**
     * @return a list of characters representing states in the filter graph of the current model.
     */
    public List<Character> getFilterStates() {
        return this.getFilterGraph().getLabels();
    }

    /**
     * Adds a model transition between two states with the specified rate.
     *
     * @param from the state in which the transition begins.
     * @param to the state in which the transition ends.
     * @param rate the numerical rate of transition.
     */
    public void addTransition(char from, char to, double rate) {
        assert from != to :
                "The 'to' and 'from' characters should not have been the same, but were: " + to + " = " + from;
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
            this.ratesMatrix[indexFrom][indexTo] = rate;
            this.transitionGraph.addDirectedEdge(indexFrom, indexTo, rate);
        }
    }

    /**
     * @return the string representation of the transition graph representing the current model.
     */
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
            // Only include vertices that either need at least another vertex being in another state to enter
            // or if this is a condition of vertices exiting the given state.
            for (Vertex v : contactNetwork.getVertices()) {
                if (this.getToExit()[contactNetwork.getVertices().indexOf(v)] > 0) {
                    filterVertices.add(v);
                    filterStates.add(contactNetwork.getLabels().get(contactNetwork.getVertices().indexOf(v)));
                }
            }
            filter = getTransitionGraph().makeSubGraph(filterVertices);
            filter.setLabels(filterStates);
        }
        return filter;
    }

    public double getRate(char a, char b) {
        int indexOfA = this.getStates().indexOf(a);
        int indexOfB = this.getStates().indexOf(b);
        if (indexOfA == -1 || indexOfB == -1) return 0;
        else return this.getRatesMatrix()[indexOfA][indexOfB];
    }

    public double getRate(char a) {
        int indexOfA = this.getStates().indexOf(a);
        double exit = 0;
        for (int i = 0; i < ratesMatrix.length; i++) {
            exit = ratesMatrix[indexOfA][i];
            if (exit > 0) return exit;
        }
        return exit;
    }

    private List<Character> getStatesOfTransitionGraph() {
        return this.filter.getLabels();
    }
}
