package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.symbols.Greek;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

import java.util.*;
import java.util.stream.Collectors;

public class ODESystem implements FirstOrderDifferentialEquations {
    private final Graph g; // Underlying graph
    private final char[] states; // States used in the model e.g. SIR
    private final RequiredTuples requiredTuples; // The required requiredTuples (found using Tuples class)
    private final int dimension; // The number of equations required (i.e. number of requiredTuples we have)
    private Map<Tuple, Integer> indicesMapping = new HashMap<>(); // Mapping of requiredTuples to unique integers
    private final double[][] T; // Transmission matrix
    private String equations;

    public double beta = 0.5; // Rate of transmission
    public double gamma = 0.5; // Rate of recovery

    public ODESystem(Graph g, char[] states, boolean closures) {
        this.g = g;
        this.states = states;
        this.requiredTuples = new RequiredTuples(g, states, closures);
        this.dimension = requiredTuples.getTuples().size();
        this.T = new double[g.getNumVertices()][g.getNumVertices()];
        // For now, every element in transmission matrix is equal to beta (can change based on context)
        // But we do not allow self-transmission, so diagonal values are all zero
        for (int i = 0; i < T.length; i++) {
            for (int j = 0; j < T.length; j++) {
                if (i == j) T[i][j] = 0;
                else T[i][j] = beta;
            }
        }
    }

    public String getEquation(double[] y, double[] yDot, Tuple tuple, Graph graph, boolean closures) {
        StringBuilder s = new StringBuilder();
        for (Vertex v : tuple.getVertices()) {
            int i = v.getLocation();
            List<Vertex> extraTerms = tuple.getVertices().stream().filter(w -> !w.equals(v)).collect(Collectors.toList());

            // Susceptible
            if (v.getState() == 'S') {
                // Loop through all vertices in graph
                for (int j = 0; j < graph.getNumVertices(); j++) {
                    List<Vertex> verticesToAdd = new ArrayList<>(Arrays.asList(new Vertex('S', i), new Vertex('I', j)));
                    Vertex w = new Vertex(j);
                    if (graph.hasEdge(v.getLocation(), w.getLocation())) {
                        // Add any of the remaining terms back in that aren't the current term
                        if (!extraTerms.isEmpty()) {
                            extraTerms.stream().filter(extraTerm -> !verticesToAdd.contains(extraTerm)).forEach(verticesToAdd::add);
                        }
                        // Make a new tuple from the list of vertices and add to the string builder
                        Tuple t = new Tuple(verticesToAdd);
                        if (t.isValidTuple(graph, closures)) {
                            s.append("- ").append(Greek.TAU.uni()).append(t);
                            yDot[this.getIndicesMapping().get(tuple)] +=
                                    -T[i][j] * y[this.getIndicesMapping().get(t)];
                        }
                    }
                }
            // Infected
            } else if (v.getState() == 'I') {
                // Loop through all vertices in graph
                for (int j = 0; j < graph.getNumVertices(); j++) {
                    List<Vertex> verticesToAdd = new ArrayList<>(Arrays.asList(new Vertex('S', i), new Vertex('I', j)));
                    if (graph.hasEdge(v.getLocation(), j)) {
                        // Add any of the remaining terms back in that aren't the current term
                        if (!extraTerms.isEmpty()) {
                            extraTerms.stream().filter(extraTerm -> !verticesToAdd.contains(extraTerm)).forEach(verticesToAdd::add);
                        }
                        Tuple t = new Tuple(verticesToAdd);
                        if (t.isValidTuple(graph, closures)) {
                            if (s.length() > 0 && s.charAt(s.length() - 1) != '+') s.append("+ ");
                            s.append(Greek.TAU.uni()).append(t);
                            yDot[this.getIndicesMapping().get(tuple)] +=
                                    T[i][j] * y[this.getIndicesMapping().get(t)];
                        }
                    }
                }
                List<Vertex> verticesToAdd = new ArrayList<>();
                verticesToAdd.add(new Vertex('I', i));
                if (!extraTerms.isEmpty()) {
                    extraTerms.stream().filter(extraTerm -> !verticesToAdd.contains(extraTerm)).forEach(verticesToAdd::add);
                }
                Tuple t = new Tuple(verticesToAdd);
                if (t.size() == 1 || t.isValidTuple(graph, closures))
                    s.append("- ").append(Greek.GAMMA.uni()).append(t);
                yDot[this.getIndicesMapping().get(tuple)] +=
                        - (gamma * y[this.getIndicesMapping().get(t)]);
            }
        }
        this.equations = String.valueOf(s);
        return String.valueOf(s);
    }

    public static void main(String[] args) {
        ODESystem triangle = new ODESystem(GraphGenerator.getTriangle(), new char[]{'S', 'I', 'R'}, false);
        System.out.println(triangle.getIndicesMapping());
        FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.5);
                // DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10, 1.0e-10);
        double[] y = new double[triangle.getDimension()];
        Arrays.fill(y, 1);
        integrator.integrate(triangle, 0, y, 2, y);
        System.out.println("Final state:\n" + Arrays.toString(y));
        ODESystem eq = new ODESystem(GraphGenerator.getTriangle(), new char[]{'S','I','R'}, false);
        System.out.println(eq.getEquations());
    }

    /**
     * Creates a mapping between each of the required requiredTuples which we want to generate equations for and unique integers
     * that will be used as indices in the differential equation solving methods in a systematic (i.e. starting from 0)
     * way.
     *
     * @return a mapping of required requiredTuples to unique integers.
     */
    public Map<Tuple, Integer> getIndicesMapping() {
        // If we have already created a mapping, return it;
        // Else, create a mapping and store to class attribute
        if (this.indicesMapping.size() != this.requiredTuples.getTuples().size()) {
            Map<Tuple, Integer> indices = new HashMap<>(); //  The mapping we will return
            RequiredTuples requiredTuples = this.getTuples(); // The required requiredTuples to assign unique integers to each
            int i = 0; // Counter, to make sure we use a systematic mapping
            for (Tuple list : requiredTuples.getTuples()) indices.put(list, i++);
            this.indicesMapping = indices;
            return indices;
        } else {
            return this.indicesMapping;
        }
    }

    /**
     * @return the requiredTuples for which we are required to generate equations.
     */
    public RequiredTuples getTuples() {
        return this.requiredTuples;
    }

    /**
     * @return the number of equations we are required to generate (i.e. the number of requiredTuples).
     */
    @Override
    public int getDimension() {
        return this.dimension;
    }

    public String getEquations() {
        if (this.equations == null || this.equations.length() == 0) {
            StringBuilder s = new StringBuilder();
            RequiredTuples tuples = this.getTuples();
            for (Tuple t : tuples.getTuples()) {
                s.append(t).append(" = ").append(getEquation(new double[this.getTuples().size()], new double[this.getTuples().size()], t, this.g, false)).append("\n");
            }
            this.equations = String.valueOf(s);
            return String.valueOf(s);
        } else {
            return this.equations;
        }
    }

    @Override
    public void computeDerivatives(double v, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
        RequiredTuples requiredTuples = this.getTuples();
        Map<Tuple, Integer> indices = this.getIndicesMapping();
        // Base case (singles) must be dealt with directly, others can be derived systematically
        for (Tuple tuple : requiredTuples.getTuples()) {
            getEquation(y, yDot, tuple, this.g, false);
        }

    }

    private void getSingleEquation(double[] y, double[] yDot, Tuple tuple) {
        Vertex single = tuple.get(0);
//        for (Vertex single : tuple.getVertices()) {
            int i = single.getLocation();
            // STATE IN SINGLE IS S
            if (single.getState() == 'S') {
                for (int j = 0; j < this.g.getNumVertices(); j++) {
                    if (i != j) {
                        Tuple S_iI_j = new Tuple(Arrays.asList(new Vertex('S', i), new Vertex('I', j)));
                        try {
                            yDot[this.getIndicesMapping().get(tuple)] +=
                                    -T[i][j] * y[this.getIndicesMapping().get(new Tuple(S_iI_j))];
                        } catch (NullPointerException e) {
                            System.err.println("Couldn't find a mapping for " + S_iI_j + "\n" + e);
                        }
                    }
                }
                // STATE IN SINGLE IS I
            } else if (single.getLocation() == 'I') {
                for (int j = 0; j < this.T.length; j++) {
                    Tuple S_iI_j = new Tuple(Arrays.asList(new Vertex('S', i), new Vertex('I', j)));
                    Tuple I_j = new Tuple(new Vertex('I', j));

                    yDot[this.getIndicesMapping().get(tuple)] += T[i][j] * y[this.getIndicesMapping().get(S_iI_j)]
                            - (gamma * y[this.getIndicesMapping().get(I_j)]);
                }
            }
        }
}
