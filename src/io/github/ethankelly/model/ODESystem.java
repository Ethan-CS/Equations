package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.Vertex;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a system of differential equations that describe a compartmental model of disease on a given graph. This
 * class contains method to generate the system of equations and return as a string representation or use this system
 * and specified initial conditions to solve the system of equations.
 */
public class ODESystem implements FirstOrderDifferentialEquations {
	/** Underlying graph. */
	private final Graph g;
	/** Tuples we need to generate equations for. */
	private final RequiredTuples requiredTuples;
	/** Number of equations required (same as number of tuples). */
	private int dimension;
	/** Maximum value of t, for equation solving. */
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

	/**
	 * @return the graph that the current system of differential equations is applied to.
	 */
	public Graph getG() {
		return g;
	}

	/**
	 * Returns the String representation of the specified Tuple in the context of a wider ODE system and uses the
	 * initial conditions provided to update the calculated equations ready for solving for the Tuple provided.
	 *
	 * @param y     array representing initial conditions of the system.
	 * @param yDot  array that will be written to based on the generation of the system of equations.
	 * @param tuple the tuple we wish to generate an equation for.
	 * @return the string representation of the equation for the specified tuple that the method has generated.
	 */
	public String getEquation(double[] y, double[] yDot, Tuple tuple) {
		StringBuilder s = new StringBuilder();
		s.append("\n").append(tuple).append("=");
		tuple.getVertices().forEach(v -> getTerms(y, yDot, tuple, s, v));

		return String.valueOf(s);
	}

	private void getTerms(double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		// Chain rule - v is derived, others re-entered as ordinary terms
		// So we store a list of the remaining terms to enter back in later
		getEntryTerms(y, yDot, tuple, s, v);
		getExitTerms(y, yDot, tuple, s, v);
	}

	private void getExitTerms(double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		int indexOfState = this.getModelParameters().getStates().indexOf(v.getState());
		List<Vertex> otherTerms = getOtherTerms(tuple, v);
		otherTerms.add(v);
		// How do we exit this state?
		if (getModelParameters().getToExit()[indexOfState] == 2) { // State needs another vertex to leave
			// Store a list of states that, if a neighbouring vertex is in them,
			// would mean we leave the state represented by the current tuple.
			Map<Character, Double> otherStates
					= IntStream.range(0, this.getModelParameters().getStates().size())
					.filter(col -> this.getModelParameters().getTransitionGraph().hasEdge(indexOfState, col))
					.boxed()
					.collect(Collectors.toMap(col ->
							this.getModelParameters().getStates().get(col),
							col -> this.getModelParameters().getRatesMatrix()[indexOfState][col],
							(a, b) -> b));

			// Loop through our found exit-inducing states
			for (Character state : otherStates.keySet()) {
				// Loop through all vertices in the graph, skipping any non-neighbours of v
				for (int i = 0; i < this.getG().getNumVertices(); i++) {
					if (this.getG().hasEdge(i, v.getLocation())) {
						Vertex w = new Vertex(state, i);
						if (!otherTerms.contains(w)) otherTerms.add(w);
						addExitTuple(y, yDot, tuple, s, otherTerms, otherStates.get(state));
						otherTerms.remove(w);
					}
				}
			}
			otherTerms.remove(v);
		} else if (getModelParameters().getToExit()[indexOfState] == 1) { // Can exit by itself
			double rateOfTransition = 0; // The rate at which this transition occurs
			for (int col = 0; col < this.getModelParameters().getStates().size(); col++) {
				if (this.getModelParameters().getTransitionGraph().hasEdge(indexOfState, col) &&
				    this.getModelParameters().getToEnter()[col] == 1) {
					rateOfTransition = this.getModelParameters().getRatesMatrix()[indexOfState][col];
				}
			}
			addExitTuple(y, yDot, tuple, s, otherTerms, rateOfTransition);
		}
	}

	private void getEntryTerms(double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		int indexOfState = this.getModelParameters().getStates().indexOf(v.getState());
		List<Vertex> otherTerms = getOtherTerms(tuple, v);
		char otherState= 0; // The other state we are required to consider
		double rateOfTransition = 0; // The rate at which this transition occurs

		if (getModelParameters().getToEnter()[indexOfState] != 0) {
			for (int row = 0; row < this.getModelParameters().getStates().size(); row++) {
				if (this.getModelParameters().getTransitionGraph().hasEdge(row, indexOfState)) {
					// TODO more than one possible candidate?
					otherState = this.getModelParameters().getStates().get(row);
					rateOfTransition = this.getModelParameters().getRatesMatrix()[row][indexOfState];
				}
			}
			Vertex vComp = new Vertex(otherState, v.getLocation());
			otherTerms.add(vComp);
			// How do we enter this state?
			if (getModelParameters().getToEnter()[indexOfState] == 2) {
				// This state requires another vertex to enter, so we need to consider pair terms
				for (int i = 0; i < this.getG().getNumVertices(); i++) {
					if (this.getG().hasEdge(i, v.getLocation())) {
						Vertex w = new Vertex(this.getModelParameters().getStates().get(indexOfState), i);
						otherTerms.add(w);
						addEntryTuple(y, yDot, tuple, s, otherTerms, rateOfTransition, w);
					}
				}
				otherTerms.remove(vComp);
			} else if (getModelParameters().getToEnter()[indexOfState] == 1) {
				addEntryTuple(y, yDot, tuple, s, otherTerms, rateOfTransition, vComp);
			}
		}
	}

	private void addExitTuple(double[] y, double[] yDot, Tuple tuple, StringBuilder s, List<Vertex> otherTerms, double rateOfTransition) {
		Tuple t = new Tuple(otherTerms);
		if (t.isValidTuple(this.modelParamsParameters, this.getG(), this.closures)) {
			yDot[this.getIndicesMapping().get(tuple)] += (-rateOfTransition * y[this.getIndicesMapping().get(t)]);
			s.append("-").append(rateOfTransition).append(t);
		}
	}

	private void addEntryTuple(double[] y, double[] yDot, Tuple tuple, StringBuilder s, List<Vertex> otherTerms, double rateOfTransition, Vertex w) {
		Tuple t = new Tuple(otherTerms);
		if (t.isValidTuple(this.modelParamsParameters, this.getG(), this.closures)) {
			yDot[this.getIndicesMapping().get(tuple)] += (rateOfTransition * y[this.getIndicesMapping().get(t)]);
			s.append("+").append(rateOfTransition).append(t);
		}
		otherTerms.remove(w);
	}

	private List<Vertex> getOtherTerms(Tuple tuple, Vertex v) {
		List<Vertex> otherTerms = new ArrayList<>();
		for (Vertex vertex : tuple.getVertices()) {
			if (!v.equals(vertex)) {
				otherTerms.add(vertex);
			}
		}
		return otherTerms;
	}

	/**
	 * @return a string (textual) summary representation of the current system of equations to print to the console.
	 */
	@Override
	public String toString() {
		return "ODESystem\n" +
		       "GRAPH:\n" + g +
		       "\nREQUIRED TUPLES:\n" + requiredTuples +
		       "\nNUMBER OF TUPLES: " + dimension +
		       "\nEQUATIONS:\n" + getEquations();
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
				s.append(getEquation(new double[this.getTuples().size()], new double[this.getTuples().size()], t));
			}
			this.equations = String.valueOf(s);
			return String.valueOf(s);
		} else {
			return this.equations;
		}
	}

	/**
	 * Used to generate the system of equations given the parameters provided for the current model.
	 *
	 * @param y    double array containing the current value of the state vector
	 * @param yDot placeholder double array to contain the time derivative of the state vector
	 */
	public void getEquations(double[] y, double[] yDot) {
		RequiredTuples tuples = this.getTuples();
		if (this.equations == null || this.equations.length() == 0) {
			StringBuilder s = new StringBuilder();
			for (Tuple t : tuples.getTuples()) {
				s.append(t).append(" = ").append(getEquation(y, yDot, t)).append("\n");
			}
			this.equations = String.valueOf(s);
		} else {
			for (Tuple t : tuples.getTuples()) {
				getEquation(y, yDot, t);
			}
		}
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

	public ModelParams getModelParameters() {
		return modelParamsParameters;
	}
}
