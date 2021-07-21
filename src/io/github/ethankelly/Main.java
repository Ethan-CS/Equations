package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class Main {

	/**
	 * Takes command-line args to generate equations for a compartmental model on a specified graph.
	 *
	 * @param args command-line args
	 * @throws FileNotFoundException if the files cannot be written to.
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String[] arguments = GraphGenerator.getUserInput();
		char[] states = arguments[0].toUpperCase().toCharArray();
		Graph graph = GraphGenerator.getGraph(arguments);
		Tuples tuples = new Tuples(graph, states, false);

		// Creating a File object that represents the disk file and assign to output stream
		PrintStream o = new PrintStream(new FileOutputStream("Equations.txt"));
		System.setOut(o);
		System.out.println(" *** Equations for an " + arguments[0].toUpperCase() + " model on a " + graph.getName()
		                   + " Graph ***\n\n" + graph + "Numbers of equations of each size: "
		                   + Arrays.toString(tuples.findNumbers(tuples.getTuples())) + "\n");
		PrintEquations.generateEquations(tuples).forEach(System.out::println);
	}

}
