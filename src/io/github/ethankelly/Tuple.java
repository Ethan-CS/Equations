package io.github.ethankelly;

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
public class Tuple {
    private List<List<Vertex>> tuples;
    private final Graph graph;
    private final char[] states;

    public static final String ALPHA = "\u03B1";
    public static final String BETA = "\u03B2";
    public static final String GAMMA = "\u03B3";
    public static final String ZETA = "\u03B6";
    public static final String LANGLE = "\u3008";
    public static final String RANGLE = "\u3009";

    /**
     * Class constructor - assigns a graph and some array of states to a new Tuple object.
     *
     * @param graph  the graph of which we are interested in equations.
     * @param states the states that a vertex can be in (e.g. SIP).
     */
    public Tuple(Graph graph, char[] states) {
        this.graph = graph;
        this.states = states;
        this.tuples = this.generateTuples();
    }

    public List<List<Vertex>> getTuples() {
        return tuples;
    }

    public void setTuples(List<List<Vertex>> tuples) {
        this.tuples = tuples;
    }

    /**
     * Given the equations necessary to exactly describe an SIR epidemic on some graph, returns the numbers of n-tuple
     * equations that are required.
     *
     * @param equations the equations to find the numbers of each length.
     * @return An array with the number of equations of each size required to exactly describe the SIR system.
     */
    public int[] findNumbers(List<List<Vertex>> equations) {
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
    public List<Vertex> findSingles() {
        // Get states, graph and the vertices in the associated graph for the model
        char[] states = this.getStates();
        Graph graph = this.getGraph();
        int numVertices = graph.getNumVertices();
        int[] vertices = new int[numVertices];
        Arrays.setAll(vertices, i -> i);

        // We need an equation for the probability of each vertex being in each state
        List<Vertex> verticesWeNeed = new ArrayList<>();
        for (char c : states) {
            for (int v : vertices) {
                verticesWeNeed.add(new Vertex(c, v));
            }
        }
        return verticesWeNeed;
    }

    // Recursive helper method to generate the total equations we need
    private void generateTuples(List<Vertex> items,
                                List<Vertex> selected,
                                int index,
                                List<List<Vertex>> result) {
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
    public List<List<Vertex>> generateTuples() {
        List<Vertex> items = this.findSingles();
        List<List<Vertex>> result = new ArrayList<>();
        List<Vertex> selected = new ArrayList<>();
        generateTuples(items, selected, 0, result);

        // The helper method gives us all combinations, so remove the ones that don't
        // constitute valid (necessary) tuples (and sort by length of sub-arrays).
        result.removeIf(row -> !this.isValidTuple(row));
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
    private boolean isValidTuple(List<Vertex> toAdd) {
        Graph g = this.getGraph();
        return Vertex.areStatesDifferent(toAdd)
                && Vertex.areLocationsDifferent(toAdd)
                && Vertex.areAllConnected(toAdd, g);
    }

    /**
     * @return the graph associated with the model in question.
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Each vertex in the graph model can be in any of the given states, which might be 'Susceptible,' 'Infected' and
     * 'Recovered' (standard SIR model) for instance.
     *
     * @return the states defined for the model in question.
     */
    public char[] getStates() {
        return states;
    }

    public List<String> generateEquation() {
        List<String> equations = new ArrayList<>();
        char[] states = this.getStates();
        Arrays.sort(states);
        char[] si = new char[]{'S', 'I'};
        char[] sir = new char[]{'S', 'I', 'R'};
        char[] sirp = new char[]{'S', 'I', 'R', 'P'};
        Arrays.sort(si);
        Arrays.sort(sir);
        Arrays.sort(sirp);
        int numVertices = this.getGraph().getNumVertices();
        int[][] t = this.getGraph().getTransmissionMatrix();

        if (Arrays.equals(states, sir)) states = si;

        for (List<Vertex> tuple : this.getTuples()) {
            StringBuilder eqn = new StringBuilder(); // The String representation of the equation of the tuple
            eqn.append(tuple.toString()).append(" = ");
            for (Vertex vertex : tuple) {
                switch (Character.toUpperCase(vertex.getState())) {
                    case 'S' -> {
                        if (Arrays.equals(states, sirp))
                            eqn.append(ALPHA).append(LANGLE).append("P").append(vertex.getLocation()).append(RANGLE);
                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                eqn.append("-");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(BETA).append(LANGLE).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(RANGLE);
                            }
                        }
                        if (Arrays.equals(states, sirp))
                            eqn.append("-").append(ZETA).append(LANGLE)
                                    .append("S").append(vertex.getLocation()).append(RANGLE);
                    }
                    case 'I' -> {
                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                if (eqn.length() > (tuple.toString() + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                                    eqn.append("+");
                                eqn.append(" ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(GAMMA).append(LANGLE).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(RANGLE);
                            }
                        }
                        eqn.append(" - ").append(GAMMA).
                                append(LANGLE).append("I").append(vertex.getLocation()).append(RANGLE);
                    }
                }
            }
            equations.add(String.valueOf(eqn));
        }

        return equations;
    }

    /**
     * Unit testing for the {@code Equations} class and contained methods.
     *
     * @param args the command-line arguments.
     */
    public static void main(String[] args) throws FileNotFoundException {
        // Creating a File object that represents the disk file and assign to output stream
        PrintStream o = new PrintStream("TriangleLollipopToast.txt");
        System.setOut(o);

        char[] states = new char[]{'S', 'I','R'};
        System.out.println();

        // TRIANGLE
        Graph triangle = new Graph(3);
        triangle.addEdge(0, 1);
        triangle.addEdge(1, 2);
        triangle.addEdge(2, 0);
        Tuple triangleNE = new Tuple(triangle, states);

        System.out.println(" *** TRIANGLE GRAPH ***\n\n" + triangle + "\nEquations required for Triangle graph:\n");
//        triangleNE.getTuples().forEach(System.out::println);
        System.out.println("\nNumbers of equations of each size: "
                + Arrays.toString(triangleNE.findNumbers(triangleNE.getTuples())) + "\n\n");
        triangleNE.generateEquation().forEach(System.out::println);

        // LOLLIPOP
        Graph lollipop = new Graph(4);
        lollipop.addEdge(0, 1);
        lollipop.addEdge(0, 2);
        lollipop.addEdge(0, 3);
        lollipop.addEdge(2, 3);
        Tuple lollipopNE = new Tuple(lollipop, states);

        System.out.println(" *** LOLLIPOP GRAPH ***\n\n" + lollipop + "\nEquations required for Lollipop graph:\n");
//        lollipopNE.getTuples().forEach(System.out::println);
        System.out.println("\nNumbers of equations of each size: "
                + Arrays.toString(lollipopNE.findNumbers(lollipopNE.getTuples())) + "\n\n");
        lollipopNE.generateEquation().forEach(System.out::println);

        // TOAST
        Graph toast = new Graph(4);
        toast.addEdge(0, 1);
        toast.addEdge(0, 2);
        toast.addEdge(0, 3);
        toast.addEdge(1, 2);
        toast.addEdge(2, 3);
        Tuple toastNE = new Tuple(toast, states);

        System.out.println(" *** TOAST GRAPH ***\n\n" + toast + "\nEquations required for Toast graph:\n");
//        toastNE.getTuples().forEach(System.out::println);
        System.out.println("\nNumbers of equations of each size: "
                + Arrays.toString(toastNE.findNumbers(toastNE.getTuples())) + "\n\n");
        toastNE.generateEquation().forEach(System.out::println);

        // Creating another File object that represents the disk file and assign to output stream
        PrintStream er = new PrintStream("ErdosRenyiTest.txt");
        System.setOut(er);

        // ERDOS-RENYI
        Graph erdosRenyi = GraphGenerator.erdosRenyi(5, 0.5);
        Tuple erdosRenyiNE = new Tuple(erdosRenyi, states);

        System.out.println(" *** ERDOS-RENYI GRAPH ***\n\n" + erdosRenyi
                + "\nEquations required for Erdos-Renyi graph:\n");
        erdosRenyiNE.getTuples().forEach(System.out::println);
        System.out.println("\nNumbers of equations of each size: "
                + Arrays.toString(erdosRenyiNE.findNumbers(erdosRenyiNE.getTuples())) + "\n\n");

    }

}
