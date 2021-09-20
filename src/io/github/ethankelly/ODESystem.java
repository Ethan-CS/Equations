package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.symbols.Greek;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a system of differential equations that describe a compartmental model of disease on a given graph. This
 * class contains method to generate the system of equations and return as a string representation or use this system
 * and specified initial conditions to solve the system of equations.
 */
public class ODESystem implements FirstOrderDifferentialEquations {
	private final Graph g; // Underlying graph
	private final RequiredTuples requiredTuples; // The required requiredTuples (found using Tuples class)
	private final int dimension; // The number of equations required (i.e. number of requiredTuples we have)
	private final double[][] T; // Transmission matrix
	public double tau; // Rate of transmission
	public double gamma; // Rate of recovery
	boolean closures; // Whether we need to consider closure-related tuples, i.e. all susceptible tuples.
	private Map<Tuple, Integer> indicesMapping = new HashMap<>(); // Mapping of requiredTuples to unique integers
	private String equations; // String representation of the system of equations

	/**
	 * Class constructor.
	 *
	 * @param g        the graph that underpins our compartmental model.
	 * @param closures whether we need to consider otherwise unnecessary tuples (i.e. all susceptible).
	 * @param tau      the rate of transmission in the model.
	 * @param gamma    the rate of recovery in the model.
	 */
	public ODESystem(Graph g, boolean closures, double tau, double gamma) {
		this.g = g;
		this.closures = closures;
		this.tau = tau;
		this.gamma = gamma;
		this.requiredTuples = new RequiredTuples(g, new char[] {'S', 'I', 'R'}, this.closures);
		this.dimension = requiredTuples.size();
		this.T = new double[g.getNumVertices()][g.getNumVertices()];
		// For now, every element in transmission matrix is equal to beta (can change based on context)
		// But we do not allow self-transmission, so diagonal values are all zero
		for (int i = 0; i < T.length; i++) for (int j = 0; j < T.length; j++) if (i != j) T[i][j] = this.tau;
	}

	/**
	 * Unit testing.
	 *
	 * @param args command-line arguments, ignored.
	 */
	public static void main(String[] args) {
		ODESystem triangle = new ODESystem(GraphGenerator.getTriangle(), false, 0.7, 0.25);
		double[] y0 = new double[triangle.getDimension()];

		// Known state - S0I1S2, so S0, I1, S2, S0I1 and S0I1S2 all 1, others 0
		y0[triangle.getIndicesMapping().get(new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('I', 1), new Vertex('S', 2))))] = 1;
		y0[triangle.getIndicesMapping().get(new Tuple(Arrays.asList(new Vertex('S', 0), new Vertex('I', 1))))] = 1;
		y0[triangle.getIndicesMapping().get(new Tuple(Arrays.asList(new Vertex('I', 1), new Vertex('S', 2))))] = 1;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 0))))] = 1;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 1))))] = 1;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 2))))] = 1;

		int tMax = 10;
		double[][] results = ODEResultsUtils.getIncrementalResults(triangle, y0, tMax);
		ODEResultsUtils.outputCSVResult(triangle, results);

		Map<List<Tuple>, double[][]> map = ODEResultsUtils.getResultsByLength(triangle, results, tMax);
		for (List<Tuple> key : map.keySet()) {
			System.out.println("TUPLES\n" + key + "\n" + Arrays.deepToString(map.get(key)));
		}
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
	private String getEquation(double[] y, double[] yDot, Tuple tuple) {
		Graph graph = this.g; // The graph underpinning the model
		boolean closures = this.closures; // Whether we consider all susceptible tuples

		// Initialise a new string builder, used for the string representation of the generated equation
		StringBuilder s = new StringBuilder();
		// Select each individual vertex in the tuple and get the first order derivatives
		for (Vertex v : tuple.getVertices()) {
			int i = v.getLocation();
			// Store the other vertices in the tuple to add back in later as 0th order derivative terms (CHAIN RULE)
			List<Vertex> extraTerms = tuple.getVertices().stream().filter(w -> !w.equals(v)).collect(Collectors.toList());

			// SUSCEPTIBLE
			if (v.getState() == 'S') {
				// Loop through all vertices in graph
				for (int j = 0; j < graph.getNumVertices(); j++) {
					List<Vertex> verticesToAdd = new ArrayList<>(Arrays.asList(new Vertex('S', i), new Vertex('I', j)));
					if (graph.hasEdge(i, j)) {
						// Add any of the remaining terms back in that aren't the current term
						if (!extraTerms.isEmpty()) {
							extraTerms.stream().filter(extraTerm -> !verticesToAdd.contains(extraTerm)).forEach(verticesToAdd::add);
						}
						// Make a new tuple from the list of vertices
						Tuple t = new Tuple(verticesToAdd);
						if (t.isValidTuple(graph, closures)) {
							// Append appropriate term to the string builder
							s.append("- ").append(Greek.TAU.uni()).append(t);
							// Append to the relevant yDot term
							yDot[this.getIndicesMapping().get(tuple)] +=
									-T[i][j] * y[this.getIndicesMapping().get(t)];
						}
					}
				}

				// INFECTED
			} else if (v.getState() == 'I') {
				// Loop through all vertices in graph
				for (int j = 0; j < graph.getNumVertices(); j++) {
					List<Vertex> verticesToAdd = new ArrayList<>(Arrays.asList(new Vertex('S', i), new Vertex('I', j)));
					if (graph.hasEdge(i, j)) {
						// Add any of the remaining terms back in that aren't the current term
						if (!extraTerms.isEmpty()) {
							extraTerms.stream().filter(extraTerm -> !verticesToAdd.contains(extraTerm)).forEach(verticesToAdd::add);
						}
						// Create the relevant tuple and, if it is a valid tuple, add to equations list
						Tuple t = new Tuple(verticesToAdd);
						if (t.isValidTuple(graph, closures)) {
							if (s.length() > 0 && s.charAt(s.length() - 1) != '+') s.append("+ ");
							// Append appropriate term to the string builder
							s.append(Greek.TAU.uni()).append(t);
							// Append to the relevant yDot term
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
				// Append appropriate term to the string builder (if appropriate)
				if (t.size() == 1 || t.isValidTuple(graph, closures)) {
					s.append("- ").append(Greek.GAMMA.uni()).append(t);
					// Append to the relevant yDot term
					yDot[this.getIndicesMapping().get(tuple)] -= (gamma * y[this.getIndicesMapping().get(t)]);
				}
			}
		}

		// Return the string representation of equations
		return String.valueOf(s);
	}

	/**
	 * @return a string (textual) summary representation of the current system of equations to print to the console.
	 */
	@Override
	public String toString() {
		StringBuilder tMat = new StringBuilder();
		for (double[] doubles : this.T) {
			tMat.append("[");
			for (int j = 0; j < this.T.length - 1; j++) {
				tMat.append(doubles[j]).append(", ");
			}
			tMat.append(doubles[this.T.length - 1]).append("]\n");
		}

		return "ODESystem\n" +
		       "GRAPH:\n" + g +
		       "\nREQUIRED TUPLES:\n" + requiredTuples +
		       "\nNUMBER OF TUPLES: " + dimension +
		       "\nTRANSMISSION MATRIX T = \n" + tMat +
		       "\nEQUATIONS:\n" + getEquations() +
		       "\nRATE OF TRANSMISSION, " + Greek.TAU.uni() + " = " + tau +
		       "\nRATE OF RECOVERY, " + Greek.GAMMA.uni() + " = " + gamma;
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
				s.append(t).append(" = ").append(getEquation(new double[this.getTuples().size()], new double[this.getTuples().size()], t)).append("\n");
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
}
