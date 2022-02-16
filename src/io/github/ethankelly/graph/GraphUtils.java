package io.github.ethankelly.graph;

import java.util.ArrayList;
import java.util.List;

public class GraphUtils {
	// Prints all paths from 's' to 'd'
	public static List<List<Integer>> printAllPaths(Graph g, int l) {
		boolean[] isVisited = new boolean[g.getNumVertices()];
		List<List<Integer>> paths = new ArrayList<>();
		for (Vertex v : g.getVertices()) {
			ArrayList<Integer> pathList = new ArrayList<>();
			// Add source to path array
			pathList.add(v.getLocation());
			for (Vertex w : g.getVertices()) {
				// Call recursive utility
				List<List<Integer>> results = printAllPathsUtil(g, v.getLocation(), w.getLocation(), l, isVisited, pathList, new ArrayList<>());
				if (results != null) {
					for (List<Integer> result : results) {
						if (!paths.contains(result)) paths.add(result);
					}
				}
			}
		}
		return paths;
	}

	// A recursive function to print all paths from 'u' to 'd'.
	// isVisited[] keeps track of vertices in current path.
	// localPathList<> stores actual vertices in the current path.
	private static List<List<Integer>> printAllPathsUtil(Graph g, Integer u, Integer d, Integer l, boolean[] isVisited, List<Integer> localPathList, List<List<Integer>> paths) {
		if (localPathList.size() == l) {
			System.out.println(localPathList);
			if (!paths.contains(localPathList)) paths.add(localPathList);
			// if match found then no need to traverse more till depth
			return null;
		}
		// Mark the current node
		isVisited[u] = true;
		// Recur for all the vertices adjacent to current vertex
		for (Vertex v : g.getAdjList().get(u)) {
			Integer i = v.getLocation();
			if (!isVisited[i]) {
				// store current node in path list
				localPathList.add(i);
				printAllPathsUtil(g, i, d, l, isVisited, localPathList, paths);
				// remove current node in path list
				localPathList.remove(i);
			}
			// Mark the current node
			isVisited[u] = false;
		}
		return paths;

	}
	// Driver program
	public static void main(String[] args) {
		// Create a sample graph
		Graph g = new Graph(4, "");
		g.addDirectedEdge(0, 1);
		g.addDirectedEdge(0, 2);
		g.addDirectedEdge(0, 3);
		g.addDirectedEdge(2, 0);
		g.addDirectedEdge(2, 1);
		g.addDirectedEdge(1, 3);

		System.out.println(g);

		for (int i = 0; i < 10; i++) {
			System.out.println("The following are all the different paths of length " + i);
			System.out.println(printAllPaths(g, i));
		}
	}

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

	// Recursive helper method - used to decompose the given graph into sub-graphs by cut-vertices
	static void spliceUtil(List<Graph> subGraphs, List<Vertex> cutVertices, Graph clone, Graph original) {
		Vertex cutVertex = cutVertices.get(0);
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
					original.getCutVertexFreq().put(cutVertex, original.getCutVertexFreq().get(cutVertex) + 1);
					subGraphs.add(subGraph);
				} else spliceUtil(subGraphs, cutVerticesOfSubgraph, subGraph, original);
			}
		}
	}

    /*
     * Recursive helper function that finds cut-vertices using the depth-first search algorithm.
     */
    static void findCutVertices(Graph graph, Vertex vertex,
                                boolean[] visited,
                                int[] times,
                                int[] low,
                                int[] parent,
                                boolean[] cutVertices) {
		// TODO this method tends to lead to a stack overflow - easiest thing may be to rewrite
        int NIL = -1;
        int u = graph.getVertices().indexOf(vertex);
        int children = 0; // Counter for children in the DFS Tree
        visited[u] = true; // Mark the current node visited
        times[u] = low[u] = ++graph.time; // Initialise discovery time and low value
        List<Vertex> adj = graph.getAdjList().get(u);
        // v is current adjacent of u
        for (Vertex otherVertex : adj) {
			if (!vertex.equals(otherVertex)) {
				int v = graph.getVertices().indexOf(otherVertex);
				// If v is not visited yet, make it a child of u in DFS tree and recurse
				if (!visited[v]) {
					children++;
					parent[v] = u;
					findCutVertices(graph, otherVertex, visited, times, low, parent, cutVertices);
					// Check if the subtree rooted with v has a connection to one of the ancestors of u
					low[u] = Math.min(low[u], low[v]);
					// u is a cut vertex if either (1) it is a root of DFS tree and has two or more children,
					// or (2) the low value of one of its children is more than its own discovery value.
					if (parent[u] == NIL && children > 1) cutVertices[u] = true; // (1)
					if (parent[u] != NIL && low[v] >= times[u])
						cutVertices[u] = true; // (2)
				} else if (v != parent[u]) {
					// Update the low value of u for parent function calls.
					low[u] = Math.min(low[u], times[v]);
				}
			}
        }
    }
}
