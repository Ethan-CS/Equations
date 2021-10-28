package io.github.ethankelly;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.model.Model;
import io.github.ethankelly.model.ODESystem;

import java.util.Arrays;

public class Main {

	/**
	 * Takes command-line args to generate equations for a compartmental model on a specified graph.
	 *
	 * @param args command-line args
	 */
	public static void main(String[] args) {
		Model ms = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
		ms.addTransition('S', 'I', 0.75);
		ms.addTransition('I', 'R', 0.2);
		ODESystem triangle = new ODESystem(GraphGenerator.getTriangle(), 5, ms);
		ODESystem complete5 = new ODESystem(GraphGenerator.complete(5), 1, ms);
		ODESystem cycle5 = new ODESystem(GraphGenerator.cycle(5), 1, ms);

		System.out.println("TRIANGLE: " + triangle.getTuples().size() + triangle.getEquations());
		System.out.println("COMPLETE ON 5: " + complete5.getTuples().size() + complete5.getEquations());
		System.out.println("CYCLE ON 5: " + cycle5.getTuples().size() +cycle5.getEquations());
//		double[] y0 = new double[triangle.getDimension()];
//
//		// Known state - S0I1S2, so S0, I1, S2, S0I1 and S0I1S2 all 1, others 0
//		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 0))))] = 0.8;
//		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 0))))] = 0.2;
//		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 1))))] = 0.2;
//		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 1))))] = 0.8;
//		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 2))))] = 0.8;
//		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 2))))] = 0.2;
//
//		ODEResultsUtils.setInitialConditions(triangle, y0);
//
//		System.out.println("Initial Conditions");
//		for (Tuple t : triangle.getTuples().getTuples()) {
//			System.out.println(t + " = " + y0[triangle.getIndicesMapping().get(t)]);
//		}
//		System.out.println(ms);
//		double[][] results = ODEResultsUtils.getIncrementalResults(triangle, y0, new ClassicalRungeKuttaIntegrator(0.0001));
//
//		ODEResultsUtils.outputCSVResult(triangle, results);
//		ODEResultsUtils.plotResults(triangle, triangle.getIndicesMapping(), results);
	}
}
