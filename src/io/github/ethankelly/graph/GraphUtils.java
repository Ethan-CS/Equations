package io.github.ethankelly.graph;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class GraphUtils {

	// Driver program
	public static void main(String[] args) {
		// Create a sample graph
		Graph g = new Graph(4, "");
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 3);

		System.out.println(g);
	}

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
			if (graph.numWalks.size() <= e) {
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
									count[i][j] += graph.numWalks.get(e - 1)[a][j];
							}
						}
					}
				}
				graph.numWalks.add(e, count);
			}
		}
		// No reflexive vertices (e.g. avoids all I or all S walks in SIR)
		Integer[][] zeroWalks = new Integer[graph.getNumVertices()][graph.getNumVertices()];
		for (int i = 0; i < zeroWalks.length; i++) {
			for (int j = 0; j < zeroWalks[0].length; j++) {
				zeroWalks[i][j] = 0;
			}
		}
		graph.numWalks.set(0, zeroWalks);
	}

	private static String printWalks(Graph g) {
		StringBuilder sb = new StringBuilder();
		List<Integer[][]> walks = g.numWalks;
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


	public static <T> List<List<T>> powerSet(List<T> originalSet) {
		List<List<T>> sets = new ArrayList<>();
		if (originalSet.isEmpty()) {
			sets.add(new ArrayList<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		List<T> rest = new ArrayList<T>(list.subList(1, list.size()));
		for (List<T> set : powerSet(rest)) {
			List<T> newSet = new ArrayList<>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
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
    static void DFS_ConnectedUtil(Graph graph, int source, List<List<Vertex>> adjacencyList, boolean[] visited){
        // Mark the vertex visited as True
        visited[source] = true;
        // Travel the adjacent neighbours
        for (int i = 0; i <adjacencyList.get(source).size() ; i++) {
            int neighbour = graph.getVertices().indexOf(adjacencyList.get(source).get(i));
            if(!visited[neighbour]){
                // Call DFS_ConnectedUtil from neighbour
                DFS_ConnectedUtil(graph, neighbour, adjacencyList, visited);
            }
        }
    }
}
