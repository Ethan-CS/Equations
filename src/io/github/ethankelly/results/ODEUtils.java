package io.github.ethankelly.results;

import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.model.ModelParams;
import io.github.ethankelly.model.ODESystem;
import io.github.ethankelly.model.Tuple;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Font;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings("unused")
public class ODEUtils {
	public static Map<List<Tuple>, double[][]> getResultsByLength(ODESystem system, double[][] results, int tMax) {
		Map<List<Tuple>, double[][]> map = new HashMap<>();
		List<Tuple> tuples = system.getTuples();
		int maxLength = 0;
		for (Tuple t : tuples) if (t.size() > maxLength) maxLength = t.size();

		List<List<Tuple>> tuplesByLength = IntStream.range(0, maxLength).<List<Tuple>>mapToObj(i -> new ArrayList<>()).collect(Collectors.toList());
		for (Tuple t : tuples) {
			tuplesByLength.get(t.size()-1).add(t);
		}

		for (List<Tuple> tups : tuplesByLength) {
			double[][] thisResults = new double[tMax][tups.size()];
			for (Tuple t : tups) {
				for (int i = 0; i < tMax; i++) {
					thisResults[i][tups.indexOf(t)] =
							results[i][system.getIndicesMapping().get(t)];
				}
				map.put(tups, thisResults);
			}

		}
		return map;
	}

	public static void setInitialConditions(ODESystem system, double[] y0) {
		for (Tuple t : system.getTuples()) {
			y0[system.getIndicesMapping().get(t)] = y0[system.getIndicesMapping().get(new Tuple(t.getVertices().get(0)))];
			IntStream.range(1, t.size())
					.mapToObj(t::get)
					.forEach(v -> y0[system.getIndicesMapping().get(t)]
							= y0[system.getIndicesMapping().get(t)] * y0[system.getIndicesMapping().get(new Tuple(v))]);
		}
	}

	public static double[][] getIncrementalResults(ODESystem system, double[] y0, FirstOrderIntegrator integrator) {
		// Initialise a 2D array to store the solutions to the system for each time step
		double[][] results = new double[system.tMax+1][system.getIndicesMapping().size()];
		// First row of results (t=0) contains initial conditions
		System.arraycopy(y0, 0, results[0], 0, results[0].length);
		// Print found values to 2 d.p. for clarity of system output
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		System.out.println(); // This method prints the results to the console as it goes
		for (int t = 1; t <= system.tMax; t++) { // Iterate over the specified time period
			System.out.print("\n[");
			// Integrate from previous time-step up to the current value of t
			integrator.integrate(system, t - 1, y0, t, y0);
			// Add computed results to summary data structure
			// Since we're dealing with probabilities, we need to ensure solutions are between 0 and 1
			for (Tuple tup : system.getTuples()) {
				double thisResult = y0[system.getIndicesMapping().get(tup)];
				if (thisResult <= 0) { // Less than 0 or very small
					results[t][system.getIndicesMapping().get(tup)] = 0;
					y0[system.getIndicesMapping().get(tup)] = 0;
				} else if (0 < thisResult && thisResult < 0.00001) { // Less than 0 or very small
					results[t][system.getIndicesMapping().get(tup)] = 0;
				} else if (thisResult > 1) { // Greater than 1
					results[t][system.getIndicesMapping().get(tup)] = 1;
					y0[system.getIndicesMapping().get(tup)] = 1;
				} else { // Anything else (between 0 and 1) is fine
					results[t][system.getIndicesMapping().get(tup)] = thisResult;
				}

				System.out.print(df.format(y0[system.getIndicesMapping().get(tup)]) + " ");
			}
			System.out.print("]");
		}
		System.out.println();
		return results;
	}

	public static void outputCSVResult(ODESystem triangle, double[][] results) {
		StringBuilder sb = new StringBuilder();
		try {
			FileWriter writer = new FileWriter("test.csv");
			sb.append("t,");
			for (Tuple key : triangle.getIndicesMapping().keySet()) sb.append(key).append(",");
			int t = 1;
			for (double[] result : results) {
				// Get rid of extra comma on end of previous line and start new line
				sb.setLength(Math.max(sb.length() - 1, 0));
				sb.append("\n").append(t++).append(",");
				// Append the next row of results
				for (int j = 0; j < results[0].length; j++) {
					sb.append(result[j]).append(",");
				}
			}
			// Get rid of final extra comma
			sb.setLength(Math.max(sb.length() - 1, 0));
			// Write our results to the file
			writer.write(String.valueOf(sb));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Tuple getKeyByValue(Map<Tuple, Integer> map, Integer value) {
		return map.keySet()
				.stream()
				.filter(entry -> Objects.equals(map.get(entry), value))
				.findFirst()
				.orElse(null);
	}

	public static void plotResults(ODESystem system, double[][] results) {
		Map<Tuple, Integer> headers = system.getIndicesMapping();
		// Time arrays, for x values in the plot
		int[] time = IntStream.range(0, system.tMax + 1).toArray();
		double[] timeDouble = Arrays.stream(time).asDoubleStream().toArray();

		// Initialise a table
		// TODO table creation and return should be in its own method
		Table table = Table.create("Results");
		table.addColumns(IntColumn.create("Time", time));

		// Array to store each of the lines representing the results
		// for an individual tuple probability over the time period
		ScatterTrace[] traces = new ScatterTrace[headers.size()];

		for (int column = 0; column < results[0].length; column++) { // Iterate through columns
			double[] thisCol = new double[results.length];
			DoubleColumn col = DoubleColumn.create(String.valueOf(getKeyByValue(headers, column)));
			for (int row = 0; row < results.length; row++) { // Iterate through entries in current column
				col.append(results[row][column]);
				thisCol[row] = results[row][column];
			}
			table.addColumns(col);

			// Add the current column of results as a line to be plotted in final chart
			ScatterTrace trace = ScatterTrace.builder(timeDouble, thisCol)
					.mode(ScatterTrace.Mode.LINE)
					.name(String.valueOf(getKeyByValue(system.getIndicesMapping(), column)))
					.build();
			traces[column] = trace;
		}
		// House style settings for plot
		Font titleFont = Font.builder()
				.family(Font.Family.VERDANA)
				.size(30)
				.color("black")
				.build();
		Font font = Font.builder()
				.family(Font.Family.VERDANA)
				.size(18)
				.color("black")
				.build();
		// Set the layout of the plot (title, width, dataset, etc.)
		Layout layout = Layout.builder()
				.title("Plot of Probabilities Describing an SIR Epidemic on a " +
				       system.getG().getName() +
				       " Network")
				.titleFont(titleFont)
				.xAxis(Axis.builder().title("Time").font(font).build())
				.yAxis(Axis.builder().title("Probability").font(font).build())
				.height(1200)
				.width(2500)
				.build();
		// Show the plot (opens in the default web browser)
		Plot.show(new Figure(layout, traces));
	}

    /**
     * Returns the String representation of the specified Tuple in the context of a wider ODE system and uses the
     * initial conditions provided to update the calculated equations ready for solving for the Tuple provided.
     *
     * @param ode the ODE system for which we would like to generate equations.
     * @param y     array representing initial conditions of the system.
     * @param yDot  array that will be written to based on the generation of the system of equations.
     * @param tuple the tuple we wish to generate an equation for.
     * @return the string representation of the equation for the specified tuple that the method has generated.
     */
    public static String getEquation(ODESystem ode, double[] y, double[] yDot, Tuple tuple) {
        StringBuilder s = new StringBuilder();
        s.append(tuple).append("=");
        tuple.getVertices().forEach(v -> getTerms(ode, y, yDot, tuple, s, v));
		s.append("\n");
        return String.valueOf(s);
    }

	private static void getTerms(ODESystem ode, double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		// Chain rule - v is derived, others re-entered as ordinary terms
		// So we store a list of the remaining terms to enter back in later
		getEntryTerms(ode, y, yDot, tuple, s, v);
		getExitTerms(ode, y, yDot, tuple, s, v);
	}

	private static void getExitTerms(ODESystem ode, double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		int indexOfState = ode.getModelParameters().getStates().indexOf(v.getState());
		List<Vertex> otherTerms = getOtherTerms(tuple, v, true);
		// How do we exit this state?
		for (int col = 0; col < ode.getModelParameters().getStates().size(); col++) {
			if (ode.getModelParameters().getToExit()[indexOfState] == 2) { // State needs another vertex to leave
				// Store a list of states that, if a neighbouring vertex is in them,
				// would mean we leave the state represented by the current tuple.
				Map<Character, Double> otherStates = new HashMap<>();
				Map<Character, String> otherSymbols = new HashMap<>();
				if (ode.getModelParameters().getTransitionGraph().hasEdge(indexOfState, col)) {
					otherStates.put(ode.getModelParameters().getStates().get(col),
							ode.getModelParameters().getRatesMatrix()[indexOfState][col]);
					otherSymbols.put(ode.getModelParameters().getStates().get(col),
							ode.getModelParameters().getRatesForPrinting()[indexOfState][col]);
				}

				// Loop through our found exit-inducing states
				for (Character state : otherStates.keySet()) {
					// Loop through all vertices in the graph, skipping any non-neighbours of v
					for (int i = 0; i < ode.getG().getNumVertices(); i++) {
						if (ode.getG().hasEdge(i, v.getLocation())) {
							Vertex w = new Vertex(state, i);
							boolean added = false;
							if (!otherTerms.contains(w)) {
								otherTerms.add(w);
								added = true;
							}
							addExitTuple(ode, y, yDot, tuple, s, otherTerms, otherStates.get(state), otherSymbols.get(state) == null ? "" : otherSymbols.get(state));
							if (added) otherTerms.remove(w); //  Make sure we only remove if we just added for this
						}
					}
				}
			} else if (ode.getModelParameters().getToExit()[indexOfState] == 1) { // Can exit by itself
				double rateOfTransition = 0; // The rate at which this transition occurs
				String symbolOfTransition = "";
				if (ode.getModelParameters().getTransitionGraph().hasEdge(indexOfState, col) &&
						ode.getModelParameters().getToEnter()[col] == 1) {
					rateOfTransition = ode.getModelParameters().getRatesMatrix()[indexOfState][col];
					symbolOfTransition = ode.getModelParameters().getRatesForPrinting()[indexOfState][col];
				}
				if ((symbolOfTransition != null && !symbolOfTransition.equals("")) && rateOfTransition > 0) {
					addExitTuple(ode, y, yDot, tuple, s, otherTerms, rateOfTransition, symbolOfTransition);
				}
			}
		}
	}

	private static void getEntryTerms(ODESystem ode, double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		ModelParams modelParams = ode.getModelParameters();
		List<Character> modelStates = modelParams.getFilterStates();
		int indexOfState = modelStates.indexOf(v.getState());
		List<Vertex> otherTerms = getOtherTerms(tuple, v, false);
		int numNeighboursToEnter = modelParams.getToEnter()[indexOfState];

		if (numNeighboursToEnter > 0) {
			// Need a neighbour in another state to change state, so find out which other state(s)
			for (int otherStateIndex = 0; otherStateIndex < modelStates.size(); otherStateIndex++) {
				// If this state and other state share an edge in transition graph, there's
				// a transition between them, so a dynamically relevant term to add.
				if (modelParams.getFilterGraph().hasDirectedEdge(otherStateIndex, indexOfState)) {
					char otherState = modelStates.get(otherStateIndex); // The other state we are required to consider
					double rateOfTransition = modelParams.getRatesMatrix()[otherStateIndex][indexOfState];
					String symbolOfTransition = modelParams.getRatesForPrinting()[otherStateIndex][indexOfState];
					// vComp is v in the state it would be before it transitions to its current state
					Vertex vComp = new Vertex(otherState, v.getLocation());
					otherTerms.add(vComp);
					// How do we enter this state?
					if (numNeighboursToEnter == 2) {
						// This state requires another vertex to enter, so we need to consider pair terms
						for (int i = 0; i < ode.getG().getNumVertices(); i++) {
							if (ode.getG().hasEdge(i, v.getLocation())) {
								Vertex w = new Vertex(modelStates.get(indexOfState), i);
								boolean added = false;
								if (!otherTerms.contains(w)) {
									otherTerms.add(w);
									added = true;
								}
								addEntryTuple(ode, y, yDot, tuple, s, otherTerms, rateOfTransition, w, symbolOfTransition);
//								if (added) otherTerms.remove(w);
							}
						}
					} else if (numNeighboursToEnter == 1) {
						// State can be entered without another neighbour, so add a "solo" term
						addEntryTuple(ode, y, yDot, tuple, s, otherTerms, rateOfTransition, vComp, symbolOfTransition);
					}
					otherTerms.remove(vComp);
				}
			}
		}
	}

	public static void addExitTuple(ODESystem odeSystem, double[] y, double[] yDot, Tuple tuple, StringBuilder s, List<Vertex> otherTerms, double rateOfTransition, String rateSymbol) {
		Tuple t = new Tuple(otherTerms);
		if (odeSystem.getTuples().contains(t)) {
			yDot[odeSystem.getIndicesMapping().get(tuple)] += (-rateOfTransition * y[odeSystem.getIndicesMapping().get(t)]);
			s.append("-");
			if (!(rateSymbol == null || rateSymbol.equals(""))) s.append(rateSymbol);
			else s.append(rateOfTransition);
			s.append(t);
		}
	}

	private static void addEntryTuple(ODESystem odeSystem, double[] y, double[] yDot, Tuple tuple, StringBuilder s, List<Vertex> otherTerms, double rateOfTransition, Vertex w, String rateSymbol) {
		Tuple t = new Tuple(otherTerms);
		if (odeSystem.getTuples().contains(t)) {
			yDot[odeSystem.getIndicesMapping().get(tuple)] += (rateOfTransition * y[odeSystem.getIndicesMapping().get(t)]);
			if (s.charAt(s.length()-1) != '=') s.append("+");
			if (!(rateSymbol == null || rateSymbol.equals(""))) s.append(rateSymbol);
			else s.append(rateOfTransition);
			s.append(t);
		}
		otherTerms.remove(w);
	}

	private static List<Vertex> getOtherTerms(Tuple tuple, Vertex v, boolean exit) {
		List<Vertex> otherTerms = new ArrayList<>();
		for (Vertex vertex : tuple.getVertices()) {
			if (!v.equals(vertex)) {
				otherTerms.add(vertex);
			}
		}
		if (exit && !otherTerms.contains(v)) otherTerms.add(v);
		return otherTerms;
	}
}
