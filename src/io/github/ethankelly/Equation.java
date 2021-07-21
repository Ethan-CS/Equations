package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

import java.util.*;

public class Equation implements FirstOrderDifferentialEquations {
    private Graph g; // Underlying graph
    private char[] states; // States used in the model e.g. SIR
    private List<List<Vertex>> tuples; // The required tuples (found using Tuples class)
    private int dimension; // The number of equations required (i.e. number of tuples we have)
    private Map<List<Vertex>, Integer> indicesMapping = new HashMap<>(); // Mapping of tuples to unique integers
    private double[][] T; // Transmission matrix

    public double beta = 0.5; // Rate of transmission
    public double gamma = 0.5; // Rate of recovery

    public Equation(Graph g, char[] states, boolean closures) {
        this.g = g;
        this.states = states;
        this.tuples = new Tuples(g, states, closures).getTuples();
        this.dimension = tuples.size();
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

    /**
     * Creates a mapping between each of the required tuples which we want to generate equations for and unique integers
     * that will be used as indices in the differential equation solving methods in a systematic (i.e. starting from 0)
     * way.
     *
     * @return a mapping of required tuples to unique integers.
     */
    public Map<List<Vertex>, Integer> getIndicesMapping() {
        // If we have already created a mapping, return it;
        // Else, create a mapping and store to class attribute
        if (this.indicesMapping.size() == 0) {
            Map<List<Vertex>, Integer> indices = new HashMap<>(); //  The mapping we will return
            List<List<Vertex>> tuples = this.getTuples(); // The required tuples to assign unique integers to each
            int i = 0; // Counter, to make sure we use a systematic mapping
            for (List<Vertex> list : tuples) indices.put(list, i++);
            return indices;
        } else {
            return this.indicesMapping;
        }
    }

    /**
     * @return the tuples for which we are required to generate equations.
     */
    public List<List<Vertex>> getTuples() {
        return this.tuples;
    }

    /**
     * @return the number of equations we are required to generate (i.e. the number of tuples).
     */
    @Override
    public int getDimension() {
        return this.dimension;
    }

    @Override
    public void computeDerivatives(double v, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
        List<List<Vertex>> tuples = this.getTuples();
        Map<List<Vertex>, Integer> indices = this.getIndicesMapping();
        // Base case (singles) must be dealt with directly, others can be derived systematically
        for (List<Vertex> tuple : tuples) {
            if(tuple.size() == 1) {
                getSingleEquation(y, yDot, tuple);
            } else {
                for (int i = 0; i < tuple.size(); i++) {
                    List<Vertex> iSingle = Collections.singletonList(tuple.get(i));
                    double yProduct = 1;
                    for (int j = 0; j < tuple.size(); j++) {
                        if (i != j) {
                            List<Vertex> jSingle = Collections.singletonList(tuple.get(j));
                            yProduct = yProduct * y[indices.get(jSingle)];
                        }
                    }
                    yDot[indices.get(tuple)] += yDot[indices.get(iSingle)] * yProduct;
                }

            }
        }

    }

    private void getSingleEquation(double[] y, double[] yDot, List<Vertex> tuple) {
        Vertex single = tuple.get(0);
        int i = single.getLocation();
        // STATE IN SINGLE IS S
        if (single.getState() == 'S') {
            for (int j = 0; j < this.T.length; j++) {
                List<Vertex> S_iI_j = new ArrayList<>();
                S_iI_j.add(new Vertex('S', i));
                S_iI_j.add(new Vertex('I', j));
                System.out.println(S_iI_j);
                // TODO this isn't working because we need a dedicated, comparable Tuple class rather than using List<Vertex> to represent them directly
                try {
                    yDot[this.getIndicesMapping().get(tuple)] +=
                            - T[i][j] * y[this.getIndicesMapping().get(S_iI_j)];
                } catch (NullPointerException e) {
                    List<Vertex> I_jS_i = new ArrayList<>();
                    I_jS_i.add(new Vertex('I', j));
                    I_jS_i.add(new Vertex('S', i));
                    System.out.println("Nope! Trying this instead: " + I_jS_i);
                    yDot[this.getIndicesMapping().get(tuple)] +=
                            - T[i][j] * y[this.getIndicesMapping().get(I_jS_i)];
                }
            }
        // STATE IN SINGLE IS I
        } else if (single.getLocation() == 'I') {
            for (int j = 0; j < this.T.length; j++) {
                List<Vertex> S_iI_j = new ArrayList<>();
                S_iI_j.add(new Vertex('S', i));
                S_iI_j.add(new Vertex('I', j));

                List<Vertex> I_j = new ArrayList<>();
                I_j.add(new Vertex('I', j));

                yDot[this.getIndicesMapping().get(tuple)] += T[i][j] * y[this.getIndicesMapping().get(S_iI_j)]
                        - (gamma * y[this.getIndicesMapping().get(I_j)]);
            }
        }
    }

    public static void main(String[] args) {
        Equation triangle = new Equation(GraphGenerator.getTriangle(), new char[]{'S','I','R'}, false);
        System.out.println(triangle.getIndicesMapping());
        FirstOrderIntegrator integrator = new DormandPrince853Integrator(1.0e-8, 100.0, 1.0e-10,1.0e-10);
        double[] y = new double[triangle.getDimension()];
        Arrays.fill(y, 1);
        integrator.integrate(triangle, 0, y, 2, y);
        System.out.println("Final state:\n" + Arrays.toString(y));
    }
}
