package io.github.ethankelly.results;

import io.github.ethankelly.graph.Vertex;
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
		List<Tuple> tuples = system.getTuples().getTuples();
		int maxLength = 0;
		for (Tuple t : tuples) if (t.length > maxLength) maxLength = t.length;

		List<List<Tuple>> tuplesByLength = IntStream.range(0, maxLength).<List<Tuple>>mapToObj(i -> new ArrayList<>()).collect(Collectors.toList());
		for (Tuple t : tuples) {
			tuplesByLength.get(t.length-1).add(t);
		}

		for (List<Tuple> tups : tuplesByLength) {
			double[][] thisResults = new double[tMax][tups.size()];
			for (Tuple t : tups) {
				for (int i = 0; i < tMax; i++) {
					thisResults
							[i]
							[tups.indexOf(t)] =
							results
									[i]
									[system.getIndicesMapping().get(t)];
				}
				map.put(tups, thisResults);
			}

		}
		return map;
	}

	public static void setInitialConditions(ODESystem system, double[] y0) {
		for (Tuple t : system.getTuples().getTuples()) {
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
			for (Tuple tup : system.getTuples().getTuples()) {
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

	public static void plotResults(ODESystem system, Map<Tuple, Integer> headers, double[][] results) {
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
		// Set the layout of the plot (title, width, dataset, etc)
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
        s.append("\n").append(tuple).append("=");
        tuple.getVertices().forEach(v -> getTerms(ode, y, yDot, tuple, s, v));

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
		List<Vertex> otherTerms = getOtherTerms(tuple, v);
		otherTerms.add(v);
		// How do we exit this state?
		if (ode.getModelParameters().getToExit()[indexOfState] == 2) { // State needs another vertex to leave
			// Store a list of states that, if a neighbouring vertex is in them,
			// would mean we leave the state represented by the current tuple.
			Map<Character, Double> otherStates
					= IntStream.range(0, ode.getModelParameters().getStates().size())
					.filter(col -> ode.getModelParameters().getTransitionGraph().hasEdge(indexOfState, col))
					.boxed()
					.collect(Collectors.toMap(col ->
							ode.getModelParameters().getStates().get(col),
							col -> ode.getModelParameters().getRatesMatrix()[indexOfState][col],
							(a, b) -> b));

			// Loop through our found exit-inducing states
			for (Character state : otherStates.keySet()) {
				// Loop through all vertices in the graph, skipping any non-neighbours of v
				for (int i = 0; i < ode.getG().getNumVertices(); i++) {
					if (ode.getG().hasEdge(i, v.getLocation())) {
						Vertex w = new Vertex(state, i);
						if (!otherTerms.contains(w)) otherTerms.add(w);
						addExitTuple(ode, y, yDot, tuple, s, otherTerms, otherStates.get(state));
						otherTerms.remove(w);
					}
				}
			}
			otherTerms.remove(v);
		} else if (ode.getModelParameters().getToExit()[indexOfState] == 1) { // Can exit by itself
			double rateOfTransition = 0; // The rate at which this transition occurs
			for (int col = 0; col < ode.getModelParameters().getStates().size(); col++) {
				if (ode.getModelParameters().getTransitionGraph().hasEdge(indexOfState, col) &&
				    ode.getModelParameters().getToEnter()[col] == 1) {
					rateOfTransition = ode.getModelParameters().getRatesMatrix()[indexOfState][col];
				}
			}
			addExitTuple(ode, y, yDot, tuple, s, otherTerms, rateOfTransition);
		}
	}

	private static void getEntryTerms(ODESystem ode, double[] y, double[] yDot, Tuple tuple, StringBuilder s, Vertex v) {
		int indexOfState = ode.getModelParameters().getStates().indexOf(v.getState());
		List<Vertex> otherTerms = getOtherTerms(tuple, v);
		char otherState= 0; // The other state we are required to consider
		double rateOfTransition = 0; // The rate at which this transition occurs

		if (ode.getModelParameters().getToEnter()[indexOfState] != 0) {
			for (int row = 0; row < ode.getModelParameters().getStates().size(); row++) {
				if (ode.getModelParameters().getTransitionGraph().hasEdge(row, indexOfState)) {
					// TODO more than one possible candidate?
					otherState = ode.getModelParameters().getStates().get(row);
					rateOfTransition = ode.getModelParameters().getRatesMatrix()[row][indexOfState];
				}
			}
			Vertex vComp = new Vertex(otherState, v.getLocation());
			otherTerms.add(vComp);
			// How do we enter this state?
			if (ode.getModelParameters().getToEnter()[indexOfState] == 2) {
				// This state requires another vertex to enter, so we need to consider pair terms
				for (int i = 0; i < ode.getG().getNumVertices(); i++) {
					if (ode.getG().hasEdge(i, v.getLocation())) {
						Vertex w = new Vertex(ode.getModelParameters().getStates().get(indexOfState), i);
						otherTerms.add(w);
						addEntryTuple(ode, y, yDot, tuple, s, otherTerms, rateOfTransition, w);
					}
				}
				otherTerms.remove(vComp);
			} else if (ode.getModelParameters().getToEnter()[indexOfState] == 1) {
				addEntryTuple(ode, y, yDot, tuple, s, otherTerms, rateOfTransition, vComp);
			}
		}
	}

	public static void addExitTuple(ODESystem odeSystem, double[] y, double[] yDot, Tuple tuple, StringBuilder s, List<Vertex> otherTerms, double rateOfTransition) {
		Tuple t = new Tuple(otherTerms);
		if (t.isValidTuple(odeSystem.getModelParameters(), odeSystem.getG(), odeSystem.isClosures())) {
			yDot[odeSystem.getIndicesMapping().get(tuple)] += (-rateOfTransition * y[odeSystem.getIndicesMapping().get(t)]);
			s.append("-").append(rateOfTransition).append(t);
		}
	}

	private static void addEntryTuple(ODESystem odeSystem, double[] y, double[] yDot, Tuple tuple, StringBuilder s, List<Vertex> otherTerms, double rateOfTransition, Vertex w) {
		Tuple t = new Tuple(otherTerms);
		if (t.isValidTuple(odeSystem.getModelParameters(), odeSystem.getG(), odeSystem.isClosures())) {
			yDot[odeSystem.getIndicesMapping().get(tuple)] += (rateOfTransition * y[odeSystem.getIndicesMapping().get(t)]);
			s.append("+").append(rateOfTransition).append(t);
		}
		otherTerms.remove(w);
	}

	private static List<Vertex> getOtherTerms(Tuple tuple, Vertex v) {
		List<Vertex> otherTerms = new ArrayList<>();
		for (Vertex vertex : tuple.getVertices()) {
			if (!v.equals(vertex)) {
				otherTerms.add(vertex);
			}
		}
		return otherTerms;
	}
}