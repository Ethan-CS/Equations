package io.github.ethankelly.graph;

import java.util.List;

public class GraphUtils {
	/*
	 Dynamic programming-based method to count walks on k edges in given graph
	 Time Complexity: O(V^3) for V - number of vertices in the graph
	 Three nested loops are needed to fill the table, so the time complexity is O(V^3).
	 Auxiliary Space: O(V^3).
	 To store the table, O(V^3) space is needed.
	*/
	private static void countWalks(Graph graph, int k) {
		// Loop for number of edges from 0 to k
		for (int e = 0; e <= k; e++) {
			// Ensure we don't recalculate matrices if they are already stored in walks field
			if (graph.walks.size() <= e) {
				// Value count[i][j] is the number of possible walks from i to j with e-many edges
				Integer[][] count = new Integer[graph.getNumVertices()][graph.getNumVertices()];
				for (int i = 0; i < graph.getNumVertices(); i++) { // source
					for (int j = 0; j < graph.getNumVertices(); j++) { // destination
						// initialize value
						count[i][j] = 0;
						// base cases
						if (e == 0 && i == j) count[i][j] = 1;
						if (e == 1 && graph.hasDirectedEdge(i, j)) count[i][j] = 1;
						// go to adjacent only when number of edges is more than 1
						if (e > 1) {
							for (int a = 0; a < graph.getNumVertices(); a++) { // adjacent to i
								if (graph.hasDirectedEdge(i, a))
									count[i][j] += graph.walks.get(e - 1)[a][j];
							}
						}
					}
				}
				graph.walks.add(e, count);
			}
		}
	}

	private static String printWalks(Graph g) {
		StringBuilder sb = new StringBuilder();
		List<Integer[][]> walks = g.walks;
		for (int i = 0; i < walks.size(); i++) {
			Integer[][] count = walks.get(i);
			sb.append("Walks of length ").append(i).append(":\n");
			for (Integer[] integers : count) {
				for (int j = 0; j < count[0].length; j++) {
					sb.append(integers[j]);
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		return String.valueOf(sb);
	}

	// Driver method
	public static void main(String[] args) {
		Graph g = new Graph(4, "");
		g.addDirectedEdge(0, 1);
		g.addDirectedEdge(1, 2);
		g.addDirectedEdge(2, 3);
		g.addDirectedEdge(3, 1);

		int k = 2;
		countWalks(g, k);
		System.out.println(printWalks(g));

		k=5;
		countWalks(g, k);
		System.out.println(printWalks(g));

	}

}
