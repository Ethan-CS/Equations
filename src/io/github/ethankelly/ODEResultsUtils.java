package io.github.ethankelly;

import io.github.ethankelly.symbols.Maths;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

public class ODEResultsUtils {
	public static void main(String[] args) {

	}

//	static List<double[][]> getResultsByLength(ODESystem system, double[][] results) {
//		List<Tuple> tuples = system.getTuples().getTuples();
//		int maxLength = 0;
//		for (Tuple t : tuples) if (t.length > maxLength) maxLength = t.length;
//
//		List<List<Tuple>> tuplesByLength = IntStream.range(0, maxLength).<List<Tuple>>mapToObj(i -> new ArrayList<>()).collect(Collectors.toList());
//		for (Tuple t : tuples) {
//			tuplesByLength.get(t.length).add(t);
//		}
//
//	}

	static double[][] getIncrementalResults(ODESystem triangle, double[] y0, int tMax) {
		FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.001);
		double[][] results = new double[tMax][triangle.getIndicesMapping().size()];
		// Print the initial state and specify the initial condition used in this test
		System.out.println("Initial state:\n" + Arrays.toString(y0) + "I.e. " + Maths.L_ANGLE.uni() + "S0_I1_S2" + Maths.R_ANGLE.uni());
		// Print relevant system information
		System.out.println(triangle);
		// Empty array to contain derivative values
		double[] yDot = Arrays.copyOf(y0, y0.length);
		// Print found values to 2 d.p. for clarity of system output
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		System.out.println((triangle.getIndicesMapping().keySet()));

		for (int t = 1; t <= tMax; t++) {
			System.out.print("\n[");
			// Integrate up to specified t value
			integrator.integrate(triangle, 0, y0, t, yDot);
			// Compute derivatives based on equations generation
			triangle.computeDerivatives(t, y0, yDot);
			// Add computed results to summary data structure
			for (Tuple tup : triangle.getTuples().getTuples()) {
				// t-1 so that array indexing starts at 0
				results[t-1][triangle.getIndicesMapping().get(tup)] = yDot[triangle.getIndicesMapping().get(tup)];
				System.out.print(df.format(yDot[triangle.getIndicesMapping().get(tup)]) + " ");
			}
			System.out.print("]");
		}
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
}
