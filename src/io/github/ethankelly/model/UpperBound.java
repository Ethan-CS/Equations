package io.github.ethankelly.model;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;

import java.util.Arrays;
import java.util.Map;

public class UpperBound {
	long ub = 0;
	public UpperBound(Graph g, Model mp) {
		// Find all cut vertices using DFS - runs in O(|E|+|V|)
		// And splice by cut-vertices

		// The number of times each cut vertex appears in sub-graphs after splicing,
		// i.e. the number of graphs created after removal of each cut vertex
		Map<Vertex, Integer> cutVertexFreq = g.getCutVertexFreq();
		// How many of each sub-graph we have
		Map<Graph, Integer> subGraphFrequencies = g.getSubGraphFreq();


		StringBuilder calc = new StringBuilder();

		long firstTerm = 0;
		for (Graph subG : g.getSpliced()) {
			RequiredTuples rq = new RequiredTuples(subG, mp, true);
			calc.append(rq.size()).append("*").append(subGraphFrequencies.get(subG)).append(" + ");
			firstTerm += (long) rq.size() * subGraphFrequencies.get(subG);
		}
		calc.deleteCharAt(calc.length()-1).deleteCharAt(calc.length()-1).deleteCharAt(calc.length()-1);

		long secondTerm = 0;
		for (Vertex cv : cutVertexFreq.keySet()) {
			calc.append(" - (").append(cutVertexFreq.get(cv)).append("-1)");
			secondTerm += cutVertexFreq.get(cv) - 1;
		}
		this.ub = firstTerm - 2*secondTerm;
		System.out.println("UPPER BOUND = " + calc + " = " + ub);

	}

	public static void main(String[] args) {
		Model ms = new Model(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
		ms.addTransition('S', 'I', 0.75);
		ms.addTransition('I', 'R', 0.2);

		System.out.println("\n *** EDGE ***");
		new UpperBound(GraphGenerator.getEdge(), ms);

		System.out.println("\n *** TRIANGLE ***");
		new UpperBound(GraphGenerator.getTriangle(), ms);

		System.out.println("\n *** SQUARE ***");
		Graph square = GraphGenerator.getToast();
		square.setName("Square");
		square.removeEdge(new Vertex(0), new Vertex(2));
		new UpperBound(square, ms);

		System.out.println("\n *** TOAST ***");
		new UpperBound(GraphGenerator.getToast(), ms);

		System.out.println("\n *** COMPLETE on 4 ***");
		new UpperBound(GraphGenerator.complete(4), ms);

		System.out.println("\n *** BOWTIE ***");
		new UpperBound(GraphGenerator.getBowTie(), ms);

		System.out.println("\n *** BOWTIE WITH BRIDGE ***");
		new UpperBound(GraphGenerator.getBowTieWithBridge(), ms);
	}
}
