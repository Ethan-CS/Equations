package io.github.ethankelly.model;

import io.github.ethankelly.exceptions.UnexpectedStateException;
import io.github.ethankelly.graph.Graph;

import java.util.Arrays;
import java.util.List;

public class Model {
    // Describe how many neighbours are required to enter/exit each state
    private final int[] toEnter;
    private final int[] toExit;

    // List of characters representing states in the model
    private List<Character> states;

    private boolean[][] transitionMatrix;
    private double[][] ratesMatrix;
    private Graph transitionGraph;

    public Model(List<Character> states, int[] toEnter, int[] toExit) {
        this.states = states;
        assert (toEnter.length == states.size()) :
                "Number of states different to the number of entry requirements provided";
        assert (toExit.length == states.size()) :
                "Number of states different to the number of exit requirements provided";
        this.toEnter = toEnter;
        this.toExit = toExit;
        this.transitionMatrix = new boolean[states.size()][states.size()];
        this.transitionGraph = new Graph(transitionMatrix, states);
        this.ratesMatrix = new double[states.size()][states.size()];
    }

    public static void main(String[] args) {
        // For now, every element in transmission matrix is equal to the relevant rate (can change based on context)
        Model m = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);
        System.out.println(m);
    }

    public boolean validStates(List<Character> states) {
        // If there are states in input that aren't in model parameters, the input states are not valid
        if (!this.states.containsAll(states)) return false;
        // Store whether each state in provided states has a direct transition to at least one other state in the model
        boolean[] atLeastOne = new boolean[states.size()];
        for (char c : states) {
            // If we haven't already found a transition to/from this state, check it against each other state
            if (!atLeastOne[states.indexOf(c)]) {
                for (char d : states) {
                    // If states are not the same and there's a transition between them, update array
                    if (c != d && (this.getTransitionMatrix()[this.states.indexOf(c)][this.states.indexOf(d)] ||
                            this.getTransitionMatrix()[this.states.indexOf(d)][this.states.indexOf(c)])) {
                        atLeastOne[states.indexOf(c)] = true;
                        atLeastOne[states.indexOf(d)] = true;
                        break;
                    }
                }
                // c and d must not have a direct transition - enough to make the given states not valid.
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

    public void setStates(List<Character> states) {
        this.states = states;
    }

    public void addTransition(char from, char to, double rate) {
        assert !String.valueOf(from).equals(String.valueOf(to)) :
                "The 'to' and 'from' characters should not have been the same, but were: " + to + " = " + from;

        boolean[][] transitions = this.getTransitionMatrix().clone();
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
            transitions[indexFrom][indexTo] = true;
            rates[indexFrom][indexTo] = rate;
            g.addDirectedEdge(indexFrom, indexTo);
        }
        this.setTransitionMatrix(transitions);
        this.setRatesMatrix(rates);
        this.setTransitionGraph(g);
    }

    public boolean[][] getTransitionMatrix() {
        return transitionMatrix;
    }

    public void setTransitionMatrix(boolean[][] transitionMatrix) {
        this.transitionMatrix = transitionMatrix;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(this.transitionGraph);
        s.append("\n ");
        for (char c : states) s.append(" ").append(c);
        for (int i = 0; i < this.getTransitionMatrix().length; i++) {
            s.append("\n").append(this.getStates().get(i));
            for (int j = 0; j < this.getTransitionMatrix()[0].length; j++) {
                s.append(" ").append(this.getRatesMatrix()[i][j]);
            }
        }
        return String.valueOf(s);
    }
}
