package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.VertexState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code Tuple} class is used to determine and represent the number of differential equations required to fully
 * describe a given compartmental epidemiological model on a particular graph. The class contains methods that determine
 * the total number of equations that would be required to exactly describe the dynamics of a compartmental model with
 * some number of states (for instance, susceptible/infected/recovered - SIR) and a given graph (with a number of
 * vertices connected in some way by a number of edges). The graphs may be randomly generated or hard-coded and the
 * states can be specified when running the model.
 */
@SuppressWarnings("DuplicatedCode")
public class Tuple {
    private List<List<VertexState>> tuples;
    private final Graph graph;
    private final char[] states;
    private boolean closures;

    /**
     * Class constructor - assigns a graph and some array of states to a new Tuple object.
     *  @param graph  the graph of which we are interested in equations.
     * @param states the states that a vertex can be in (e.g. SIP).
     * @param closures whether to include closures in the generated tuples.
     */
    public Tuple(Graph graph, char[] states, boolean closures) {
        this.graph = graph;
        this.states = states;
        this.tuples = this.generateTuples(closures);
        this.closures = closures;
    }

    public List<List<VertexState>> getTuples() {
        return this.tuples;
    }

    @SuppressWarnings("unused")
    public void setTuples(List<List<VertexState>> tuples) {
        this.tuples = tuples;
    }

    /**
     * Given the equations necessary to exactly describe an SIR epidemic on some graph, returns the numbers of n-tuple
     * equations that are required.
     *
     * @param equations the equations to find the numbers of each length.
     * @return An array with the number of equations of each size required to exactly describe the SIR system.
     */
    public int[] findNumbers(List<List<VertexState>> equations) {
        // The array needs as many elements as there are different sizes in the equations list,
        // So initialise to (1 less than) the size of the largest (final) sub-list.
        int[] sizes = new int[equations.get(equations.size() - 1).size()];
        // For each sub-list we come across, increase the element at the array position corresponding
        // to its size.
        equations.forEach(equation -> sizes[equation.size() - 1]++);

        return sizes;
    }

    /**
     * Using the states required in the model and the number of vertices in the graph, this method determines the full
     * list of single termed equations that are involved in describing the model exactly.
     *
     * @return the number of single-probability differential equations required by the model.
     */
    public List<VertexState> findSingles() {
        // Get states, graph and the vertices in the associated graph for the model
        char[] states = this.getStates();
        Arrays.sort(states);
        char[] si = new char[]{'S', 'I'};
        char[] sir = new char[]{'S', 'I', 'R'};
        char[] sip = new char[]{'S', 'I', 'P'};
        char[] sirp = new char[]{'S', 'I', 'R', 'P'};
        Arrays.sort(si);
        Arrays.sort(sir);
        Arrays.sort(sip);
        Arrays.sort(sirp);

        if (Arrays.equals(states, sir)) states = si;
        else if (Arrays.equals(states, sirp)) states = sip;

        Graph graph = this.getGraph();
        int numVertices = graph.getNumVertices();
        int[] vertices = new int[numVertices];
        Arrays.setAll(vertices, i -> i);

        // We need an equation for the probability of each vertex being in each state
        List<VertexState> verticesWeNeed = new ArrayList<>();
        for (char c : states) {
            for (int v : vertices) {
                verticesWeNeed.add(new VertexState(c, v));
            }
        }
        return verticesWeNeed;
    }

    // Recursive helper method to generate the total equations we need
    private void generateTuples(List<VertexState> items,
                                List<VertexState> selected,
                                int index,
                                List<List<VertexState>> result) {
        if (index >= items.size()) {
            result.add(new ArrayList<>(selected));
        } else {
            generateTuples(items, selected, index + 1, result);
            selected.add(items.get(index));
            generateTuples(items, selected, index + 1, result);
            selected.remove(selected.size() - 1);
        }
    }

    /**
     * Using the graph we are determining equations for and the states that a vertex can take, this method finds all of
     * the equations that we need to use to exactly express the system dynamics of the model in question.
     *
     * @return a list of each n-tuple we are required to express to exactly detail the model system dynamics.
     */
    public List<List<VertexState>> generateTuples(boolean closures) {
        List<VertexState> items = this.findSingles();
        List<List<VertexState>> result = new ArrayList<>();
        List<VertexState> selected = new ArrayList<>();
        generateTuples(items, selected, 0, result);

        // The helper method gives us all combinations, so remove the ones that don't
        // constitute valid (necessary) tuples (and sort by length of sub-arrays).
        result.removeIf(row -> !this.isValidTuple(row, closures));
        result.sort(Comparator.comparingInt(List::size));

        return result;
    }

    /**
     * Given a potential tuple, this method checks that the states of each probability are not all the same, that each
     * probability corresponds to a different vertex and that all of the vertices involved are in fact connected. If
     * these three conditions are met, then it is a tuple that we are required to express in the total set of equations
     * describing the system dynamics.
     *
     * @param toAdd the potential tuple we are considering adding to the system dynamics.
     * @return true if the tuple is essential to expressing the system dynamics, false otherwise.
     */
    private boolean isValidTuple(List<VertexState> toAdd, boolean closures) {
        Graph g = this.getGraph();
        return VertexState.areStatesDifferent(toAdd, closures)
               && VertexState.areLocationsDifferent(toAdd)
               && VertexState.areAllConnected(toAdd, g);
    }

    /**
     * @return the graph associated with the model in question.
     */
    public Graph getGraph() {
        return this.graph;
    }

    /**
     * Each vertex in the graph model can be in any of the given states, which might be 'Susceptible,' 'Infected' and
     * 'Recovered' (standard SIR model) for instance.
     *
     * @return the states defined for the model in question.
     */
    public char[] getStates() {
        return this.states;
    }
}
