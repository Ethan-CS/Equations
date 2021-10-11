package io.github.ethankelly;

import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.model.Model;
import io.github.ethankelly.model.ODEResultsUtils;
import io.github.ethankelly.model.ODESystem;
import io.github.ethankelly.model.Tuple;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;

public class Main {

	/**
	 * Takes command-line args to generate equations for a compartmental model on a specified graph.
	 *
	 * @param args command-line args
	 * @throws FileNotFoundException if the files cannot be written to.
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Model ms = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
		ms.addTransition('S', 'I', 0.75);
		ms.addTransition('I', 'R', 0.2);
		ODESystem triangle = new ODESystem(GraphGenerator.getTriangle(), 5, ms);
//		triangle.getTuples().getTuples().add(new Tuple(Arrays.asList(new Vertex('I', 0), new Vertex('I', 1), new Vertex('I', 2))));
		double[] y0 = new double[triangle.getDimension()];

		// Known state - S0I1S2, so S0, I1, S2, S0I1 and S0I1S2 all 1, others 0
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 0))))] = 0.8;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 0))))] = 0.2;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 1))))] = 0.2;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 1))))] = 0.8;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('S', 2))))] = 0.8;
		y0[triangle.getIndicesMapping().get(new Tuple(Collections.singletonList(new Vertex('I', 2))))] = 0.2;

		ODEResultsUtils.setInitialConditions(triangle, y0);

		System.out.println("Initial Conditions");
		for (Tuple t : triangle.getTuples().getTuples()) {
			System.out.println(t + " = " + y0[triangle.getIndicesMapping().get(t)]);
		}
		System.out.println(ms);
		double[][] results = ODEResultsUtils.getIncrementalResults(triangle, y0, new ClassicalRungeKuttaIntegrator(0.0001));

		ODEResultsUtils.outputCSVResult(triangle, results);
		ODEResultsUtils.plotResults(triangle, triangle.getIndicesMapping(), results);
	}

//	private static void testMethods() throws FileNotFoundException {
//		String[] arguments = GraphGenerator.getUserInput();
//		char[] states = arguments[0].toUpperCase().toCharArray();
//		Graph graph = GraphGenerator.getGraph(arguments);
//		RequiredTuples requiredTuples = new RequiredTuples(graph, states, false);
//
//		// Creating a File object that represents the disk file and assign to output stream
//		PrintStream o = new PrintStream(new FileOutputStream("Equations.txt"));
//		System.setOut(o);
//		System.out.println(" *** Equations for an " + arguments[0].toUpperCase() + " model on a " + graph.getName()
//		                   + " Graph ***\n\n" + graph + "Numbers of equations of each size: "
//		                   + Arrays.toString(requiredTuples.findNumbers(requiredTuples.getTuples())) + "\n");
//		PrintEquations.generateEquations(requiredTuples).forEach(System.out::println);
//	}

}
