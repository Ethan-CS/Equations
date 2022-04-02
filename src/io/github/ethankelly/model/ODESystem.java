package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a system of differential equations that describe a compartmental model of disease on a given graph. This
 * class contains method to generate the system of equations and return as a string representation or use this system
 * and specified initial conditions to solve the system of equations.
 */
public class ODESystem implements FirstOrderDifferentialEquations {
    /** Contact network. */
    private final Graph g;
    /** Tuples requiring equations in the ODE system. */
    private final RequiredTuples requiredTuples;
    /** Parameters of the model. */
    private final ModelParams modelParamsParameters;
    /** Maximum value of t (time), for equation solving. */
    public int tMax;
    /** Whether we need to consider closure-related tuples, i.e. all susceptible tuples. */
    boolean closures = false;
    /** Number of equations required (i.e. number of tuples). */
    private int dimension;
    /** Mapping of requiredTuples to unique integers. */
    private Map<Tuple, Integer> indicesMapping = new HashMap<>();
    /** String representation of the system of equations. */
    private List<Equation> equations;

    /**
     * Class constructor.
     *
     * @param g    the graph that underpins our compartmental model.
     * @param tMax the maximum value of time to be considered when calculating solutions
     */
    public ODESystem(Graph g, int tMax, ModelParams modelParamsParameters) {
        this.g = g;
        this.requiredTuples = new RequiredTuples(g, modelParamsParameters, false);
        this.dimension = requiredTuples.size();
        this.tMax = tMax;
        this.modelParamsParameters = modelParamsParameters;
        this.equations = new ArrayList<>();
    }

    /**
     * @return the graph that the current system of differential equations is applied to.
     */
    public Graph getG() {
        return g;
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
        this.dimension = this.getTuples().getTuples().size();
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
        if (this.equations == null || this.equations.isEmpty()) {
            RequiredTuples tuples = this.getTuples();
            for (Tuple t : tuples.getTuples()) {
                this.equations.add(new Equation(t, this.getModelParameters(), this.getG()));
            }
        }
        return this.equations;
    }

    public ModelParams getModelParameters() {
        return this.modelParamsParameters;
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
}
