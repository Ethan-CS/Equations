package io.github.ethankelly.results;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.model.ModelParams;
import io.github.ethankelly.model.ODESystem;

import java.util.Arrays;
import java.util.Map;

public class UpperBound {
	long ub = 0;
	public UpperBound(Graph g, ModelParams mp) {
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
			// TODO no rq class anymore
//			RequiredTuples rq = new RequiredTuples(subG, mp, true);
//			calc.append(rq.size()).append("*").append(subGraphFrequencies.get(subG)).append(" + ");
//			firstTerm += (long) rq.size() * subGraphFrequencies.get(subG);
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
		ModelParams ms = new ModelParams(Arrays.asList('S', 'I', 'R'), new int[]{0, 2, 1}, new int[]{2, 1, 0});
		ms.addTransition('S', 'I', 0.75);
		ms.addTransition('I', 'R', 0.2);

		System.out.println("\n *** EDGE ***");
		new UpperBound(GraphGenerator.getEdge(), ms);
		ODESystem edge = new ODESystem(GraphGenerator.getEdge(), 1, ms);
		System.out.println("ACTUAL = " + edge.getDimension());

		System.out.println("\n *** TRIANGLE ***");
		new UpperBound(GraphGenerator.getTriangle(), ms);
		ODESystem triangle = new ODESystem(GraphGenerator.getTriangle(), 1, ms);
		System.out.println("ACTUAL = " + triangle.getDimension());

		System.out.println("\n *** SQUARE ***");
		Graph square = GraphGenerator.getToast();
		square.setName("Square");
		square.removeEdge(new Vertex(0), new Vertex(2));
		new UpperBound(square, ms);
		ODESystem sq = new ODESystem(square, 1, ms);
		System.out.println("ACTUAL = " + sq.getDimension());

		System.out.println("\n *** TOAST ***");
		new UpperBound(GraphGenerator.getToast(), ms);
		ODESystem toast = new ODESystem(GraphGenerator.getToast(), 1, ms);
		System.out.println("ACTUAL = " + toast.getDimension());

		System.out.println("\n *** COMPLETE on 4 ***");
		new UpperBound(GraphGenerator.complete(4), ms);
		ODESystem c4 = new ODESystem(GraphGenerator.complete(4), 1, ms);
		System.out.println("ACTUAL = " + c4.getDimension());

		System.out.println("\n *** BOWTIE ***");
		new UpperBound(GraphGenerator.getBowTie(), ms);
		ODESystem bowtie = new ODESystem(GraphGenerator.getBowTie(), 1, ms);
		System.out.println("ACTUAL = " + bowtie.getDimension());

		System.out.println("\n *** BOWTIE WITH BRIDGE ***");
		new UpperBound(GraphGenerator.getBowTieWithBridge(), ms);
		ODESystem bowtieWithBridge = new ODESystem(GraphGenerator.getBowTieWithBridge(), 1, ms);
		System.out.println("ACTUAL = " + bowtieWithBridge.getDimension());
	}
}
