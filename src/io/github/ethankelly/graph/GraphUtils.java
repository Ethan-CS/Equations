package io.github.ethankelly.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphUtils {
	private static int time;

	/*
         Dynamic programming-based method to count walks on k edges in given graph
         Time Complexity: O(V^3) for V - number of vertices in the graph
         Three nested loops are needed to fill the table, so the time complexity is O(V^3).
         Auxiliary Space: O(V^3).
         To store the table, O(V^3) space is needed.
        */
	public static void countWalks(Graph graph, int k) {
		// Loop for number of edges from 0 to k
		for (int e = 0; e <= k; e++) {
			// Ensure we don't recalculate matrices if they are already stored in walks field
			if (graph.getNumWalks().size() <= e) {
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
									count[i][j] += graph.getNumWalks().get(e - 1)[a][j];
							}
						}
					}
				}
				graph.getNumWalks().add(e, count);
			}
		}
		// No reflexive vertices (e.g. avoids all I or all S walks in SIR)
		Integer[][] zeroWalks = new Integer[graph.getNumVertices()][graph.getNumVertices()];
		for (int i = 0; i < zeroWalks.length; i++) {
			for (int j = 0; j < zeroWalks[0].length; j++) {
				zeroWalks[i][j] = 0;
			}
		}
		graph.getNumWalks().set(0, zeroWalks);
	}

	// Recursive helper method - used to decompose the given graph into sub-graphs by cut-vertices
	static void spliceUtil(List<Graph> subGraphs, List<Vertex> cutVertices, Graph clone, Graph original) {
		// Arbitrarily start with first cut vertex in the list
		Vertex cutVertex = cutVertices.get(0);
		original.addCutVertex(cutVertex);
		// Remove the cut vertex from the graph
		clone.removeVertex(cutVertex);
		// Get the remaining connected components
		List<List<Vertex>> CCs = clone.getCCs();
		// Iterate over the connected components
		for (List<Vertex> cc : CCs) {
			// Replace the cut vertex into any connected components that don't have it after removal
			if (!cc.contains(cutVertex)) cc.add(cutVertex);
			// Make a sub-graph from the found connected component
			Graph subGraph = original.makeSubGraph(cc);
			if (cc.size() > 1) {
				List<Vertex> cutVerticesOfSubgraph = subGraph.getCutVertices();
				// If the graph cannot be spliced further, then add it to the list to return
				// Otherwise, send it back through the util method to obtain its connected components
				if (cutVerticesOfSubgraph.isEmpty()) {
					int prevCount = original.getCutVertexFreq().get(cutVertex);
					System.out.println("USED TO BE: " + prevCount);
					original.getCutVertexFreq().replace(cutVertex, prevCount + 1);
					subGraphs.add(subGraph);
				} else spliceUtil(subGraphs, cutVerticesOfSubgraph, subGraph.clone(), original);
			}
		}
	}

	// Recursive helper method that uses a depth-first search to find connected components
	static List<Vertex> DFS_CC_Util(Graph graph, Vertex v, boolean[] visited, List<Vertex> thisComponent) {
		// Mark the current node as visited and print it
		visited[graph.getVertices().indexOf(v)] = true;
		thisComponent.add(v);
		// Recur for all vertices adjacent to this vertex
		for (Vertex w : graph.getVertices()) {
			int i = graph.getVertices().indexOf(w);
			if (!visited[i] && graph.hasEdge(v, w)) DFS_CC_Util(graph, w, visited, thisComponent);
		}
		return thisComponent;
	}

    // Recursive helper method - Depth First Search to determine whether graph is connected
    static void DFS_ConnectedUtil(Graph graph, int source, boolean[] visited){
        // Mark the vertex visited as True
        visited[source] = true;
        // Travel the adjacent neighbours
//        for (int i = 0; i < graph.getAdjList().get(source).size() ; i++) {
		for (Vertex v : graph.getNeighbours(graph.getVertices().get(source))) {
			int neighbour = graph.getVertices().indexOf(v);
            if(!visited[neighbour]) { // Call DFS_ConnectedUtil from neighbour
                DFS_ConnectedUtil(graph, neighbour, visited);
            }
        }
    }

	static void APUtil(Graph g, int u, boolean[] visited, int[] disc, int[] low, int parent, List<Vertex> cutVertices) {
		List<List<Vertex>> adj = g.getAdjListAsLists();
		// Count of children in DFS Tree
		int children = 0;

		// Mark the current node as visited
		visited[u] = true;

		// Initialize discovery time and low value
		disc[u] = low[u] = ++time;

		// Go through all vertices adjacent to this
		for (Vertex ver : adj.get(u)) {
			int v = g.getVertices().indexOf(ver);
			// If v is not visited yet, then make it a child of u in DFS tree and recur for it
			if( !visited[v]) {
				children++;
				APUtil(g, v, visited, disc, low, u, cutVertices);
				// Check if the subtree rooted with v has a connection to one of the ancestors of u
				low[u] = Math.min(low[u], low[v]);
				// If u is not root and low value of one of its child is more than discovery value of u.
				if (parent != -1 && low[v] >= disc[u]) cutVertices.add(g.getVertices().get(u));
			}
			// Update low value of u for parent function calls.
			else if (v != parent) low[u] = Math.min(low[u], disc[v]);
		}

		// If u is root of DFS tree and has two or more children.
		if (parent == -1 && children > 1) cutVertices.add(g.getVertices().get(u));
	}

	static List<Vertex> AP(Graph g)
	{
		int V = g.getNumVertices();
		boolean[] visited = new boolean[V];
		int[] disc = new int[V];
		int[] low = new int[V];
		List<Vertex> cutVertices = new ArrayList<>();
		int par = -1;
		// Adding this loop so that the
		// code works even if we are given
		// disconnected graph
		for (int u = 0; u < V; u++) if (!visited[u]) APUtil(g, u, visited, disc, low, par, cutVertices);

		cutVertices.sort(null);
		return cutVertices;
	}

	public static void main(String[] args) {
		// Creating first example graph
		int V = 5;
		Graph g = new Graph(V);
		g.addEdge(1, 0);
		g.addEdge(0, 2);
		g.addEdge(2, 1);
		g.addEdge(0, 3);
		g.addEdge(3, 4);
		System.out.println("Articulation points in first graph");
		AP(g).forEach(System.out::println);

		// Creating second example graph
		V = 4;
		Graph g2 = new Graph(V);
		g2.addEdge(0, 1);
		g2.addEdge(1, 2);
		g2.addEdge(2, 3);

		System.out.println("Articulation points in second graph");
		AP(g2).forEach(System.out::println);
	}
}
