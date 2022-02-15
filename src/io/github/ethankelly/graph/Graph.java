package io.github.ethankelly.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The {@code Graph} class represents a graph - a mathematical structure consisting of a collection of vertices
 * connected by edges in some specified configuration.
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public class Graph implements Cloneable {
	/** Discovery time for cut-vertex identification */
	private int time;
	/** Name of the graph - generally corresponds to graph class. */
	private String name;
	/** Vertices in the current graph. */
	private List<Vertex> vertices;
	/** The number of vertices in the given graph. */
	private int numVertices;
	/** The number of edges in the given graph. */
	private int numEdges;
	/** A linked list representation of the graph (known as an adjacency list). */
	private List<List<Vertex>> adjList = new ArrayList<>();

	/**
	 * Permits a list of labels to be assigned to the current graph instance, where labels are used for assigning
	 * vertices more meaningful names than default indices.
	 *
	 * @param labels the labels to assign to vertices in like index locations in the graph.
	 */
	public void setLabels(List<Character> labels) {
		this.labels = labels;
	}

	/** Labels for vertices, if required (otherwise null entries). */
	private List<Character> labels;
	/** The list of connected components after cut-vertex-based splicing. */
	private List<Graph> spliced = new ArrayList<>();
	/** Frequencies of each of the sub-graphs in the spliced graph. */
	private Map<Graph, Integer> subGraphFreq = new HashMap<>();
	/** The number of sub-graphs the cut vertex belongs to after splicing . */
	private final Map<Vertex, Integer> cutVertexFreq = new HashMap<>();

	/**
	 * Class constructor.
	 *
	 * @param numVertices the number of vertices to include in the graph.
	 * @param name        the name of the graph (to indicate graph class).
	 */
	public Graph(int numVertices, String name) {
		this.numVertices = numVertices;
		this.name = name;
		this.vertices = new ArrayList<>();
		IntStream.range(0, numVertices).forEach(i -> {
			adjList.add(i, new ArrayList<>());
			vertices.add(new Vertex(i));
		});
		this.time = 0;
	}

	/**
	 * Class constructor - used to create transmission graphs.
	 *
	 * @param adjMatrix the adjacency matrix of the graph.
	 * @param states the states in the model, e.g. S for susceptible, R for recovered.
	 */
	public Graph(boolean[][] adjMatrix, List<Character> states) {
		this.name = ""; // Default name
		this.numVertices = adjMatrix.length;
		this.vertices = new ArrayList<>();
		// Initialise a new list for each vertex in the graph for the adjacency list
		List<List<Vertex>> list = IntStream.range(0, adjMatrix.length).<List<Vertex>>mapToObj(i -> new ArrayList<>()).collect(Collectors.toList());
		for (int i = 0; i < adjMatrix.length; i++) {
			vertices.add(new Vertex(i));
			for (int j = 0; j < adjMatrix[0].length; j++) {
				if (adjMatrix[i][j] && !list.get(i).contains(new Vertex(j))) {
					list.get(i).add(new Vertex(j));
					this.numEdges++;
				}
			}
		}
		this.labels = states;
		this.adjList = list;
	}

	// Recursive helper method - used to decompose the given graph into sub-graphs by cut-vertices
	private static void spliceUtil(List<Graph> subGraphs, List<Vertex> cutVertices, Graph clone, Graph original) {
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
					original.cutVertexFreq.put(cutVertex, original.cutVertexFreq.get(cutVertex) + 1);
					subGraphs.add(subGraph);
				} else spliceUtil(subGraphs, cutVerticesOfSubgraph, subGraph, original);
			}
		}
	}

	public static void main(String[] args) {
		Graph g = GraphGenerator.getBowTieWithBridge();
		System.out.println("BOWTIE WITH BRIDGE" + g);
		Map<Graph, Integer> freq = g.getSubGraphFreq();
		System.out.println("FREQUENCY MAP\n" + freq);
		System.out.println("CUT VERTEX FREQUENCIES:\n" + g.cutVertexFreq);
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	public Map<Vertex, Integer> getCutVertexFreq() {
		if (this.spliced == null || this.spliced.isEmpty()) this.splice();
		return cutVertexFreq;
	}

	/**
	 * Finds the cut-vertices of the graph, deletes edges attached to them, creates sub-graphs for each of the remaining
	 * connected components and replaces the cut-vertex in each of them and returns the list of graphs.
	 *
	 * @return the sub-graphs based on cut-vertex removal from the current graph.
	 */
	public List<Graph> splice() {
		// Store sub-graph frequencies
		Map<Graph, Integer> subGraphFreq = new HashMap<>();
		// Clone of this graph that we can safely remove edges and vertices from
		Graph clone = this.clone();
		// Empty List of Graphs that will contain our sub-graphs
		List<Graph> subGraphs = new ArrayList<>();
		// Get the cut vertices of the current graph and split the graph up by them (if there are any)
		List<Vertex> cutVertices = clone.getCutVertices();
		if (!cutVertices.isEmpty()) {
			cutVertices.forEach(cv -> this.cutVertexFreq.put(cv, 0));
			spliceUtil(subGraphs, cutVertices, this.clone(), this.clone());
		} else subGraphs.add(this);

		for (int i = 0; i < subGraphs.size(); i++) {
			Graph sub = subGraphs.get(i);
			subGraphFreq.put(sub, 1);
//			for (int j = 0; j < subGraphs.size(); j++) {
//				Graph otherSub = subGraphs.get(j);
//				// If they are not the exact same graph and are isomorphic,
//				// Remove the duplicate instance and increase frequency of the subgraph
//				if (i != j && sub.isIsomorphic(otherSub)) {
//					subGraphs.remove(otherSub);
//					subGraphFreq.put(sub, subGraphFreq.get(sub) + 1);
//				}
//			}
		}

		this.subGraphFreq = subGraphFreq;
		this.spliced = subGraphs;
		return subGraphs;
	}

	// Given a list of vertices, creates a sub-graph from them with all relevant edges from the original graph
	private Graph makeSubGraph(List<Vertex> subList) {
		subList.sort(null);
		// Make a new graph with appropriate number of vertices, name and adjacency matrices
		Graph subGraph = new Graph(subList.size(), getName() + " Subgraph");
		subGraph.vertices = subList;
		subList.forEach(v -> subList.stream()
				.filter(w -> !v.equals(w) && adjList.get(v.getLocation()).contains(w))
				.forEach(w -> subGraph.addEdge(v, w)));
		return subGraph;
	}

	/**
	 * @param v the vertex we want to find in the vertices field.
	 * @return true if v is in the vertices of the current graph, false otherwise.
	 */
	public boolean contains(Vertex v) {
		return (this.vertices.contains(v));
	}

	public boolean areAllConnected(List<Vertex> toCheck) {
		// If there's only one vertex in the list, required in the system of equations - ensure this returns true.
		// If more than one vertex, return whether they all have some path between them.
		return toCheck.size() == 1 || this.makeSubGraph(toCheck).getCCs().size() == 1;
	}

	public List<Graph> getSpliced() {
		if (this.spliced == null || this.spliced.isEmpty()) return this.splice();
		else return this.spliced;
	}

	public Map<Graph, Integer> getSubGraphFreq() {
		// If the spliced field is null, we need to splice the graph to find frequencies
		if (this.spliced == null || this.spliced.isEmpty()) this.splice();
		return this.subGraphFreq;
	}

	public boolean isIsomorphic(Graph that) {
		if (this.getNumVertices() != that.getNumVertices() || this.getNumEdges() != that.getNumEdges()) return false;

		Set<Integer> thisDegrees = new HashSet<>();
		Set<Integer> thatDegrees = new HashSet<>();

		for (int i = 0; i < this.getNumVertices(); i++) {
			int thisDeg = 0;
			int thatDeg = 0;
			for (int j = 0; j < this.getNumVertices(); j++) {
				if (this.hasEdge(vertices.get(i), vertices.get(j))) thisDeg++;
				if (that.hasEdge(vertices.get(i), vertices.get(j))) thatDeg++;
			}
			thisDegrees.add(thisDeg);
			thatDegrees.add(thatDeg);
		}
		return thisDegrees.containsAll(thatDegrees);
	}

	/*
	 * Recursive helper function that finds cut-vertices using the depth-first search algorithm.
	 */
	private void findCutVertices(Vertex vertex,
	                             boolean[] visited,
	                             int[] times,
	                             int[] low,
	                             int[] parent,
	                             boolean[] cutVertices) {
		int NIL = -1;
		int u = this.vertices.indexOf(vertex);
		int children = 0; // Counter for children in the DFS Tree
		visited[u] = true; // Mark the current node visited
		times[u] = low[u] = ++time; // Initialise discovery time and low value
		List<Vertex> adj = adjList.get(u);
		// v is current adjacent of u
		for (Vertex otherVertex : adj) {
			int v = this.vertices.indexOf(otherVertex);
			// If v is not visited yet, make it a child of u in DFS tree and recurse
			if (!visited[v]) {
				children++;
				parent[v] = u;
				findCutVertices(otherVertex, visited, times, low, parent, cutVertices);
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

	// Recursive helper method that uses a depth-first search to find connected components
	private List<Vertex> DFSUtil(Vertex v, boolean[] visited, List<Vertex> thisComponent) {
		// Mark the current node as visited and print it
		visited[this.vertices.indexOf(v)] = true;
		thisComponent.add(v);
		// Recur for all vertices adjacent to this vertex
		for (Vertex w : this.vertices) {
			int i = this.vertices.indexOf(w);
			if (!visited[i] && this.hasEdge(v, w)) DFSUtil(w, visited, thisComponent);
		}
		return thisComponent;
	}

	/**
	 * Finds the connected components in the current graph and returns them as a list of lists of integers, each sub-
	 * list representing a sub-graph (connected component).
	 *
	 * @return a list of connected components as lists of integers.
	 */
	public List<List<Vertex>> getCCs() {
		List<List<Vertex>> components = new ArrayList<>();
		// Mark all the vertices as not visited
		boolean[] visited = new boolean[this.getNumVertices()];
		for (Vertex v : this.vertices) {
			if (!visited[this.vertices.indexOf(v)]) {
				// Get all reachable vertices from v
				components.add(DFSUtil(v, visited, new ArrayList<>()));
			}
		}
		return components;
	}

	/**
	 * This method uses the depth-first search algorithm to traverse the given graph and return, with the use of a
	 * helper method, a list of cut-vertices (if any exist) in the current graph.
	 * <p>
	 * The method uses depth-first search with additional arrays, hence the time complexity is the same as DFS, which is
	 * O(V+E) for an adjacency list representation of the given graph.
	 *
	 * @return the cut-vertices present in the graph, if any.
	 */
	public List<Vertex> getCutVertices() {
		int NIL = -1;
		int numVertices = this.getNumVertices();
		// Mark all the vertices as not visited
		boolean[] visited = new boolean[numVertices];
		int[] disc = new int[numVertices];
		int[] low = new int[numVertices];
		int[] parent = new int[numVertices];
		boolean[] cutVertices = new boolean[numVertices]; // To store found cut-vertices

		// Initialize parent and visited and cut-vertices(articulation point) arrays
		for (int i = 0; i < numVertices; i++) {
			parent[i] = NIL;
			visited[i] = false;
			cutVertices[i] = false;
		}

		// Call the recursive helper function to find articulation points in DFS tree rooted with vertex 'i'
		for (int i = 0; i < getNumVertices(); i++)
			if (!visited[i]) findCutVertices(vertices.get(i), visited, disc, low, parent, cutVertices);

		List<Vertex> list = new ArrayList<>();
		for (int i = 0; i < cutVertices.length; i++) {
			if (cutVertices[i]) {
				list.add(vertices.get(i));
			}
		}
		return list;
	}

	/**
	 * Adds a bidirectional edge between two vertices v and j in a graph
	 *
	 * @param v first vertex
	 * @param w second vertex
	 */
	public void addEdge(Vertex v, Vertex w) {
		// Ensure we aren't trying to add an edge between a vertex and itself
		assert !v.equals(w) : "Cannot add an edge between a vertex and itself";
		// Update adjacency list
		if (!adjList.get(vertices.indexOf(v)).contains(w)) {
			adjList
					.get(vertices.indexOf(v))
					.add(w);
		}
		if (!adjList.get(vertices.indexOf(w)).contains(v)) {
			adjList
					.get(vertices.indexOf(w))
					.add(v);
		}
		// Increment the number of edges
		this.numEdges++;
		clearSpliceFields();
	}

	/**
	 * Overloaded method - allows for adding vertices by location (integer value) as a shorthand.
	 *
	 * @param i integer location of the first vertex of the edge.
	 * @param j integer location of the second vertex of the edge.
	 */
	public void addEdge(int i, int j) {
		// Ensure we aren't trying to add an edge between a vertex and itself
		assert i != j : "Cannot add an edge between a vertex and itself";
		// Update adjacency list
		if (!adjList.get(i).contains(new Vertex(j))) adjList.get(i).add(new Vertex(j));
		if (!adjList.get(j).contains(new Vertex(i))) adjList.get(j).add(new Vertex(i));
		// Increment the number of edges
		this.numEdges++;
		clearSpliceFields();
	}

	/**
	 * Removes an edge between the two vertices specified from a graph
	 *
	 * @param v first vertex
	 * @param w second vertex
	 */
	public void removeEdge(Vertex v, Vertex w) {
		if (this.hasEdge(v, w)) {
			// Update adjacency list
			adjList.get(v.getLocation()).remove(w);
			adjList.get(w.getLocation()).remove(v);
			// Decrement number of edges
			this.numEdges--;
		}
		clearSpliceFields();
	}

	public void removeVertex(Vertex v) {
		int i = vertices.indexOf(v);
		// Iterate through all vertices in each list
		for (List<Vertex> list : adjList) {
			for (Vertex w : list) {
				// If the current vertex is v, remove it and decrement the number of edges
				if (w.equals(v)) {
					list.remove(w);
					numEdges--;
					break;
				}
			}
		}
		// Empty the adjacency list for the given vertex
		adjList.set(i, new ArrayList<>());
		clearSpliceFields();
	}

	private void clearSpliceFields() {
		if (this.spliced != null && !this.spliced.isEmpty()) {
			this.spliced.clear();
			this.subGraphFreq.clear();
		}
	}

	/**
	 * Adds a given number of vertices to the current graph.
	 *
	 * @param verticesToAdd the number of vertices to append to the graph.
	 */
	public void appendVertices(int verticesToAdd) {
		assert verticesToAdd >= 0 : "Number of vertices to add must be a positive integer";
		// Create a new graph object with the new number of vertices
		int newNumVert = this.getNumVertices() + verticesToAdd;
		Graph that = new Graph(newNumVert, this.getName());
		// Ensure all original edges are in the new graph instance
		for (int i = 0; i < this.getNumVertices(); i++) {
			Vertex v = new Vertex(i);
			for (int j = 0; j < this.getNumVertices(); j++) {
				Vertex w = new Vertex(j);
				if (this.hasEdge(v, w) && v.getLocation() != w.getLocation()) that.addEdge(i, j);
			}
		}
		this.setNumVertices(that.getNumVertices());
		this.setAdjList(that.getAdjList());
		clearSpliceFields();
	}

	/**
	 * @return the adjacency list representing the current graph
	 */
	public List<List<Vertex>> getAdjList() {
		return this.adjList;
	}

	/**
	 * @param adjList the adjacency list to assign to the current graph
	 */
	void setAdjList(List<List<Vertex>> adjList) {
		assert adjList.size() == this.getNumVertices() : "The provided adjacency list does not contain enough lists." +
				" Expected " + this.getNumVertices() + " but got " + adjList.size() +
				"\n" +  adjList;
		this.adjList = adjList;
		clearSpliceFields();
	}

	@SuppressWarnings("unused")
	public boolean isMinimallyConnected() {
		Graph copy = this.clone();
		boolean minConnected = true;
		// Check if graph is connected at all
		if (copy.getCCs().size() > 1) minConnected = false;
			// For each edge, if removing it results in no increase in connected
			// components, the current graph is not minimally connected
		else for (int i = 0; i < copy.getNumVertices(); i++) {
			Vertex v = new Vertex(i);
			for (int j = 0; j < copy.getNumVertices(); j++) {
				Vertex w = new Vertex(j);
				if (copy.hasEdge(v, w)) {
					copy.removeEdge(v, w);
					if (copy.getCCs().size() == 1) minConnected = false;
				}
			}
		}
		assert !minConnected || (this.getNumEdges() - 1 == this.getNumVertices()) : "There are n-1 edges in a minimally connected graph.";
		return minConnected;
	}

	/**
	 * @return The number of vertices in the graph.
	 */
	public int getNumVertices() {
		return this.getVertices().size();
	}

	/**
	 * Sets the number of vertices in a given graph.
	 *
	 * @param numVertices the number of vertices to be included in the graph.
	 */
	@SuppressWarnings("unused")
	public void setNumVertices(int numVertices) {
		for (int i = this.numVertices; i < numVertices; i++) {
			this.vertices.add(new Vertex(i));
		}
		this.numVertices = numVertices;
		clearSpliceFields();
	}

	/**
	 * Determines whether there exists an edge between two vertices in a graph.
	 *
	 * @param v first vertex
	 * @param w second vertex
	 * @return true if v and w share an edge, false otherwise
	 */
	public boolean hasEdge(Vertex v, Vertex w) {
		if (!vertices.contains(v) || !vertices.contains(w)) return false;
		else return (adjList.get(vertices.indexOf(v)).contains(w) || adjList.get(vertices.indexOf(w)).contains(v));
	}

	public boolean hasEdge(int i, int j) {
		return this.hasEdge(this.vertices.get(i), this.vertices.get(j));
	}

	/**
	 * @return the number of edges in the given graph.
	 */
	public int getNumEdges() {
		return this.numEdges;
	}

	/**
	 * Given a number of edges to associate with a given graph, updates the associated attribute.
	 *
	 * @param numEdges the number of edges to associate with the given graph.
	 */
	public void setNumEdges(int numEdges) {
		this.numEdges = numEdges;
		clearSpliceFields();
	}

	/**
	 * @return the name (often indicating graph class) of the current Graph.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to assign to the current Graph.
	 * @return the current graph instance.
	 */
	public Graph setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Returns a textual representation of a graph as an adjacency matrix to be printed to the standard output.
	 *
	 * @return a string representation of the graph.
	 */
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("\n");
		for (int i = 0; i < this.getNumVertices(); i++) {
			if (labels == null || labels.isEmpty()) s.append(vertices.get(i));
			else s.append(labels.get(i));
			s.append(" ->");
			for (Vertex vertex : adjList.get(i)) {
				s.append(" ");
				if (labels == null || labels.isEmpty()) s.append(vertex);
				else s.append(labels.get(vertex.getLocation()));
			}
			s.append("\n");
		}
		s.deleteCharAt(s.length() - 1);
		return s.toString();
	}

	/**
	 * @return a new instance of the current Graph object with identical parameters
	 */
	@Override
	public Graph clone() {
		Graph cloned;
		try {
			cloned = (Graph) super.clone();
		} catch (CloneNotSupportedException e) {
			cloned = new Graph(this.getNumVertices(), this.getName());
		}

		List<List<Vertex>> newList = new ArrayList<>();
		int i = 0;
		for (List<Vertex> list : this.getAdjList()) {
			newList.add(new ArrayList<>());
			for (Vertex v : list) {
				newList.get(i).add(v);
			}
			i++;
		}
		cloned.setName(this.getName());
		cloned.setAdjList(newList);
		return cloned;
	}

	/**
	 * Returns true if the current graph instance and the given graph instance have identical attributes.
	 *
	 * @param o the object to check for equality of attributes against the current graph.
	 * @return true if the two objects are true, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Graph graph = (Graph) o;

		if (getNumVertices() != graph.getNumVertices()) return false;

		return getAdjList() != null ? getAdjList().equals(graph.getAdjList()) : graph.getAdjList() == null;
	}

	@Override
	public int hashCode() {
		int result = getNumVertices();
		result = 31 * result + (getAdjList() != null ? getAdjList().hashCode() : 0);
		return result;
	}

	public void addDirectedEdge(int i, int j) {
		// Ensure we aren't trying to add an edge between a vertex and itself
		assert i != j : "Cannot add an edge between a vertex and itself";
		// Update adjacency list
		adjList.get(i).add(new Vertex(j));
		// Increment the number of edges
		if (!this.adjList.get(j).contains(new Vertex(i))) this.numEdges++;
		clearSpliceFields();
	}
}
