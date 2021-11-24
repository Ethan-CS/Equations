package io.github.ethankelly.model;

import io.github.ethankelly.exceptions.UnexpectedStateException;
import io.github.ethankelly.graph.Graph;

import java.util.Arrays;
import java.util.List;

/**
 * Stores data representing the parameters of a compartmental model of disease.
 */
public class Model {
    // Describe how many neighbours are required to enter/exit each state
    private final int[] toEnter;
    private final int[] toExit;

    // List of characters representing states in the model
    private List<Character> states;

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
        this.transitionGraph = new Graph(new boolean[states.size()][states.size()], states);
        this.ratesMatrix = new double[states.size()][states.size()];
    }

    public static void main(String[] args) {
        // For now, every element in transmission matrix is equal to the relevant rate (can change based on context)
        Model m = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        m.addTransition('S', 'I', 0.6);
        m.addTransition('I', 'R', 0.1);
        System.out.println(m);
    }

    public boolean validStates(List<Character> states, boolean closures) {
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
                    if (c != d && (this.getTransitionGraph().hasEdge(getStates().indexOf(c), getStates().indexOf(d)))) {
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
        StringBuilder s = new StringBuilder();
        s.append(this.transitionGraph);
        s.append("\n");
        return String.valueOf(s);
    }
}
