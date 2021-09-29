package io.github.ethankelly;

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

public class ODEResultsUtils {
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

	static void setInitialConditions(ODESystem system, double[] y0) {
		for (Tuple t : system.getTuples().getTuples()) {
			y0[system.getIndicesMapping().get(t)] = y0[system.getIndicesMapping().get(new Tuple(t.getVertices().get(0)))];
			IntStream.range(1, t.size())
					.mapToObj(t::get)
					.forEach(v -> y0[system.getIndicesMapping().get(t)]
							= y0[system.getIndicesMapping().get(t)] * y0[system.getIndicesMapping().get(new Tuple(v))]);
		}
	}

	static double[][] getIncrementalResults(ODESystem system, double[] y0, FirstOrderIntegrator integrator) {
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

	static void outputCSVResult(ODESystem triangle, double[][] results) {
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
}
