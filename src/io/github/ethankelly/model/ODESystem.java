package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.*;

/**
 * Represents a system of differential equations that describe a compartmental model of disease on a given graph. This
 * class contains method to generate the system of equations and return as a string representation or use this system
 * and specified initial conditions to solve the system of equations.
 */
public class ODESystem implements FirstOrderDifferentialEquations {
    /** Contact network. */
    private final Graph g;
    /** Tuples requiring equations in the ODE system. */
    private final List<Tuple> requiredTuples;
    /** Parameters of the model. */
    private final ModelParams modelParams;
    /** Maximum value of t (time), for equation solving. */
    public int tMax;
    /** Number of equations required (i.e. number of tuples). */
    private int dimension;
    /** Mapping of requiredTuples to unique integers. */
    private Map<Tuple, Integer> indicesMapping;
    /** String representation of the system of equations. */
    private List<Equation> equations;

    /**
     * Class constructor.
     *
     * @param g    the graph that underpins our compartmental model.
     * @param tMax the maximum value of time to be considered when calculating solutions
     */
    public ODESystem(Graph g, int tMax, ModelParams modelParams) {
        this.g = g;
        this.tMax = tMax;
        this.modelParams = modelParams;
        this.requiredTuples = new ArrayList<>();
        this.equations = new ArrayList<>();
        this.indicesMapping = new HashMap<>();
    }

    public static void main(String[] args) {
        ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
        SIR.addTransition('S', 'I', 0.6);
        SIR.addTransition('I', 'R', 0.1);

        ODESystem system = new ODESystem(GraphGenerator.getLollipop(), 10, SIR);
        List<Equation> equations = system.findEquations();
        equations.forEach(System.out::println);
    }

    /**
     * Using the states required in the model and the number of vertices in the graph, determines the full
     * list of single termed equations that are involved in describing the model exactly.
     *
     * @return the number of single-probability tuples required by the model.
     */
    public List<Tuple> findSingles() {
        List<Character> statesToGenerateFor = new ArrayList<>(this.getModelParameters().getStates());
        for (Character state : this.getModelParameters().getStates()) {
            int i = this.getModelParameters().getStates().indexOf(state);
            // If we can leave this state without any other vertices being involved,
            // Singles containing the state are not dynamically significant
            if (this.getModelParameters().getToExit()[i] == 0) statesToGenerateFor.remove(state);
        }
        List<Vertex> vertexList = this.getG().getVertices();
        int[] vertices = new int[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) vertices[i] = vertexList.get(i).getLocation();

        // We need an equation for the probability of each vertex being in each state
        List<Tuple> verticesWeNeed = new ArrayList<>();
        for (char c : statesToGenerateFor) {
            for (int v : vertices) {
                verticesWeNeed.add(new Tuple(new Vertex(c, v)));
            }
        }
        Collections.sort(verticesWeNeed);
        return verticesWeNeed;
    }

    /**
     * Creates a mapping between each of the required requiredTuples which we want to generate equations for and unique
     * integers that will be used as indices in the differential equation solving methods in a systematic (i.e. starting
     * from 0) way.
     *
     * @return a mapping of required requiredTuples to unique integers.
     */
    public Map<Tuple, Integer> getIndicesMapping() {
        // If we have already created a mapping, return it;
        // Else, create a mapping and store to class attribute
        if (this.indicesMapping.size() != this.requiredTuples.size()) {
            Map<Tuple, Integer> indices = new HashMap<>(); //  The mapping we will return
            List<Tuple> requiredTuples = this.getTuples(); // The required requiredTuples to assign unique integers to each
            int i = 0; // Counter, to make sure we use a systematic mapping
            for (Tuple list : requiredTuples) indices.put(list, i++);
            this.indicesMapping = indices;
            return indices;
        } else {
            return this.indicesMapping;
        }
    }

    /**
     * @return the requiredTuples for which we are required to generate equations.
     */
    public List<Tuple> getTuples() {
        return this.requiredTuples;
    }

    /**
     * @return the number of equations we are required to generate (i.e. the number of requiredTuples).
     */
    @Override
    public int getDimension() {
        this.dimension = this.getTuples().size();
        return this.dimension;
    }

    /**
     * Computes the current time derivatives of the state vector corresponding to the current system of equations.
     *
     * @param v    the current value of the independent time variable.
     * @param y    the current value of the state vector (as a double array).
     * @param yDot a placeholder array to contain the time derivative of the state vector.
     * @throws MaxCountExceededException  if the number of evaluations of the function is exceeded.
     * @throws DimensionMismatchException if the array dimensions do not match the settings of the system.
     */
    @Override
    public void computeDerivatives(double v, double[] y, double[] yDot) throws MaxCountExceededException, DimensionMismatchException {
        this.getEquations(); // gets mappings of tuples to terms in their differential equations
        for (Equation eqn : equations) {
            int index = indicesMapping.get(eqn.getTuple());
            for (Tuple t : eqn.getTerms().keySet()) {
                for (double rate : eqn.getTerms().get(t)) {
                    yDot[index] += (y[indicesMapping.get(t)] * rate);
                }
            }
        }
    }

    /**
     * Generates the system of equations given the parameters provided for the current model.
     *
     * @return the list of equations in this system.
     */
    public List<Equation> getEquations() {
        if (this.equations == null || this.equations.isEmpty()) this.equations = this.findEquations();
        return this.equations;
    }

    public ModelParams getModelParameters() {
        return this.modelParams;
    }

    /**
     * @return the graph that the current system of differential equations is applied to.
     */
    public Graph getG() {
        return g;
    }

    // Given a list of tuples, generates the equations for each one and returns a list of these equations
    private List<Equation> getEquationsForTuples(List<Tuple> tuples) {
        List<Equation> equations = new ArrayList<>();
        for (Tuple t : tuples) {
            equations.add(new Equation(t, this.getModelParameters(), this.getG()));
        }
        return equations;
    }

    /**
     * @return a string (textual) summary representation of the current system of equations to print to the console.
     */
    @Override
    public String toString() {
        return "ODESystem\n" +
                "GRAPH:" + g +
                "\nREQUIRED TUPLES:\n" + requiredTuples +
                "\nNUMBER OF TUPLES: " + dimension +
                "\nEQUATIONS:" + getEquations();
    }

    private List<Equation> findEquations() {
        // Get equations for singles
        List<Tuple> singles = findSingles();
        // Add singles to list of required tuples
        requiredTuples.addAll(singles);
        // Get equations for singles
        List<Equation> equations = getEquationsForTuples(singles);
        // Get equations for higher-order terms
        equations.addAll(getNextTuples(equations, 2));

        equations.stream().map(Equation::getTuple).forEach(requiredTuples::add);

        return equations;
    }

    private List<Equation> getNextTuples(List<Equation> prevEquations, int length) {
        // Stop recurring if specified length is longer than the number of vertices in the graph
        if (length <= g.getNumVertices()) {
            List<Tuple> nextSizeTuples = new ArrayList<>();
            // Go through previous equations and add any tuples of this
            // length for which we have not yet generated equations.
            for (Equation eq : prevEquations) {
                for (Tuple t : eq.getTerms().keySet()) {
                    if (!nextSizeTuples.contains(t) && t.size() == length) nextSizeTuples.add(t);
                }
            }
            List<Equation> equations = getEquationsForTuples(nextSizeTuples);
            equations.addAll(getNextTuples(equations, length+1));
            return equations;
        } else return new ArrayList<>();
    }
}
