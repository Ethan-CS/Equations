package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.results.ODEUtils;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.Arrays;
import java.util.HashMap;
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
	/** Number of equations required (i.e. number of tuples). */
	private int dimension;
	/** Maximum value of t (time), for equation solving. */
	public int tMax;
	/** Whether we need to consider closure-related tuples, i.e. all susceptible tuples. */
	boolean closures = false;
	/** Mapping of requiredTuples to unique integers. */
	private Map<Tuple, Integer> indicesMapping = new HashMap<>();
	/** String representation of the system of equations. */
	private String equations;
	/** Parameters of the model. */
	private final ModelParams modelParamsParameters;

	/**
	 * Class constructor.
	 *
	 * @param g        the graph that underpins our compartmental model.
	 * @param tMax     the maximum value of time to be considered when calculating solutions
	 */
	public ODESystem(Graph g, int tMax, ModelParams modelParamsParameters) {
		this.g = g;
		this.requiredTuples = new RequiredTuples(g, modelParamsParameters, false);
		this.dimension = requiredTuples.size();
		this.tMax = tMax;
		this.modelParamsParameters = modelParamsParameters;
	}

	public static void main(String[] args) {
		// SIR Model parameters
		ModelParams SIR = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
		SIR.addTransition('S', 'I', 0.6);
		SIR.addTransition('I', 'R', 0.1);

		ODESystem ode = new ODESystem(GraphGenerator.cycle(3), 1, SIR);
		System.out.println(ode);
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
	 * @return a string representation of the current system of differential equations.
	 */
	public String getEquations() {
		if (this.equations == null || this.equations.length() == 0) {
			StringBuilder s = new StringBuilder();
			RequiredTuples tuples = this.getTuples();
			for (Tuple t : tuples.getTuples()) {
				s.append(ODEUtils.getEquation(this, new double[this.getTuples().size()], new double[this.getTuples().size()], t));
			}
			this.equations = String.valueOf(s);
			return String.valueOf(s);
		} else {
			return this.equations;
		}
	}

	/**
	 * Generates the system of equations given the parameters provided for the current model.
	 *
	 * @param y    double array containing the current value of the state vector
	 * @param yDot placeholder double array to contain the time derivative of the state vector
	 */
	public void getEquations(double[] y, double[] yDot) {
		RequiredTuples tuples = this.getTuples();
		if (this.equations == null || this.equations.length() == 0) {
			StringBuilder s = new StringBuilder();
			for (Tuple t : tuples.getTuples()) {
				s.append(t).append(" = ").append(ODEUtils.getEquation(this, y, yDot, t)).append("\n");
			}
			this.equations = String.valueOf(s);
		} else {
			for (Tuple t : tuples.getTuples()) {
				ODEUtils.getEquation(this, y, yDot, t);
			}
		}
	}

	public ModelParams getModelParameters() {
		return this.modelParamsParameters;
	}

	public boolean isClosures() {
		return this.closures;
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
		this.getEquations(y, yDot);
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
