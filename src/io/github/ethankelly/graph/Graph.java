package io.github.ethankelly.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * The {@code Graph} class represents a graph - a collection of vertices connected by edges in some meaningful
 * configuration.
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public class Graph implements Cloneable {
	private static final int NIL = -1;
	private int time; // Discovery time for cut-vertex identification
	/**
	 * Name of the graph - generally corresponds to graph class.
	 */
	private String name;
	/**
	 * The number of vertices in the given graph
	 */
	private int numVertices;
	/**
	 * The number of edges in the given graph
	 */
	private int numEdges;
	/**
	 * A linked list representation of the graph (known as an adjacency list).
	 */
	private List<List<Vertex>> adjList = new ArrayList<>();

	/**
	 * Class constructor.
	 *
	 * @param numVertices the number of vertices to include in the graph.
	 * @param name        the name of the graph (to indicate graph class).
	 */
	public Graph(int numVertices, String name) {
		this.numVertices = numVertices;
		this.name = name;
		IntStream.range(0, numVertices).forEach(i -> adjList.add(i, new ArrayList<>()));
		this.time = 0;
	}

	public static void main(String[] args) {
		System.out.println(" ***** LOLLIPOP ****** ");
		Graph lollipop = GraphGenerator.getLollipop();
		System.out.println(lollipop);
		List<Graph> lollipopCC = lollipop.splice();
		System.out.println("--- Spliced ---");
		for (Graph g : lollipopCC) System.out.println(g);

		System.out.println("\n ***** G1 ***** ");
		Graph g1 = new Graph(6, "Test");
		g1.addEdge(0, 1);
		g1.addEdge(0, 5);
		g1.addEdge(1, 2);
		g1.addEdge(2, 5);
		g1.addEdge(2, 3);
		g1.addEdge(3, 4);
		List<Graph> g1Spliced = g1.splice();
		System.out.println("--- Spliced ---");
		for (Graph g : g1Spliced) System.out.println(g);
	}

	// Recursive helper method used to decompose the given graph into sub-graphs by cut-vertices
	private static void spliceUtil(List<Graph> subGraphs, Vertex cutVertex, Graph clone, Graph original) {
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
				if (cutVerticesOfSubgraph.isEmpty()) subGraphs.add(subGraph);
				// Otherwise, send it back through the util method to obtain its connected components
				else spliceUtil(subGraphs, cutVerticesOfSubgraph.get(0), subGraph, original);
			}
		}
	}

	/**
	 * Finds the cut-vertices of the graph, deletes edges attached to them, creates sub-graphs for each of the remaining
	 * connected components and replaces the cut-vertex in each of them and returns the list of graphs.
	 *
	 * @return the sub-graphs based on cut-vertex removal from the current graph.
	 */
	public List<Graph> splice() {
		// Clone of this graph that we can safely remove edges and vertices from
		Graph clone = this.clone();
		// Empty List of Graphs that will contain our sub-graphs
		List<Graph> subGraphs = new ArrayList<>();
		// Get the cut vertices of the current graph and split the graph up by them (if there are any)
		List<Vertex> cutVertices = clone.getCutVertices();
		if (!cutVertices.isEmpty()) spliceUtil(subGraphs, cutVertices.get(0), clone, this.clone());
		else subGraphs.add(this);

		return subGraphs;
	}

	/*
	 * Recursive helper function that finds cut-vertices using the depth-first search algorithm.
	 */
	private void findCutVertices(Vertex u, boolean[] visited, int[] times, int[] low, int[] parent, boolean[] cutVertices) {
		int children = 0; // Counter for children in the DFS Tree
		visited[u.getLocation()] = true; // Mark the current node visited
		times[u.getLocation()] = low[u.getLocation()] = ++time; // Initialise discovery time and low value
		List<Vertex> adj = adjList.get(u.getLocation());
		// v is current adjacent of u
		for (Vertex v : adj) {
			// If v is not visited yet, make it a child of u in DFS tree and recurse
			if (!visited[v.getLocation()]) {
				children++;
				parent[v.getLocation()] = u.getLocation();
				findCutVertices(v, visited, times, low, parent, cutVertices);
				// Check if the subtree rooted with v has a connection to one of the ancestors of u
				low[u.getLocation()] = Math.min(low[u.getLocation()], low[v.getLocation()]);
				// u is a cut vertex if either (1) it is a root of DFS tree and has two or more children,
				// or (2) the low value of one of its children is more than its own discovery value.
				if (parent[u.getLocation()] == NIL && children > 1) cutVertices[u.getLocation()] = true; // (1)
				if (parent[u.getLocation()] != NIL && low[v.getLocation()] >= times[u.getLocation()])
					cutVertices[u.getLocation()] = true; // (2)
			} else if (v.getLocation() != parent[u.getLocation()]) {
				// Update the low value of u for parent function calls.
				low[u.getLocation()] = Math.min(low[u.getLocation()], times[v.getLocation()]);
			}
		}
	}

	// Recursive helper method that uses a depth-first search to find connected components
	private List<Vertex> DFSUtil(Vertex v, boolean[] visited, List<Vertex> thisComponent) {
		// Mark the current node as visited and print it
		visited[v.getLocation()] = true;
		thisComponent.add(v);
		// Recur for all vertices adjacent to this vertex
		for (int i = 0; i < getNumVertices(); i++) {
			Vertex w = new Vertex(i);
			if (!visited[i] && this.hasEdge(v, w)) DFSUtil(new Vertex(i), visited, thisComponent);
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
		for (int v = 0; v < this.getNumVertices(); ++v) {
			if (!visited[v]) {
				// Get all reachable vertices from v
				components.add(DFSUtil(new Vertex(v), visited, new ArrayList<>()));
			}
		}
		return components;
	}

	// Given a list of vertices, creates a sub-graph from them with all relevant edges from the original graph
	private Graph makeSubGraph(List<Vertex> vertices) {
		vertices.sort(null);
		// Make a new graph with appropriate number of vertices, name and adjacency matrices
		Graph subGraph = new Graph(vertices.size(), this.getName() + " Subgraph");

		List<Vertex> vs = new ArrayList<>();
		int i = 0;
		for (Vertex v : vertices) vs.add(i++, v);

		for (Vertex v : vs)
			for (Vertex w : vs)
				if (this.hasEdge(v, w) && !subGraph.hasEdge(vs.indexOf(v), vs.indexOf(w)))
					subGraph.addEdge(vs.indexOf(v), vs.indexOf(w));

		return subGraph;
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
			if (!visited[i]) findCutVertices(new Vertex(i), visited, disc, low, parent, cutVertices);

		List<Vertex> list = new ArrayList<>();
		for (int i = 0; i < cutVertices.length; i++) {
			if (cutVertices[i]) {
				Vertex vertex = new Vertex(i);
				list.add(vertex);
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
	@SuppressWarnings("unused")
	public void addEdge(Vertex v, Vertex w) {
		// Ensure we aren't trying to add an edge between a vertex and itself
		assert !v.equals(w) : "Cannot add an edge between a vertex and itself";
		// Update adjacency list
		adjList.get(v.getLocation()).add(w);
		adjList.get(w.getLocation()).add(v);
		// Increment the number of edges
		this.numEdges++;
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
		adjList.get(i).add(new Vertex(j));
		adjList.get(j).add(new Vertex(i));
		// Increment the number of edges
		this.numEdges++;
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
	}

	public void removeVertex(Vertex v) {
		// Empty the adjacency list for the given vertex
		adjList.set(v.getLocation(), new ArrayList<>());
		// Iterate through all vertices in each list
		for (List<Vertex> list : adjList) {
			Iterator<Vertex> iter = list.iterator();
			while (iter.hasNext()) {
				Vertex w = iter.next();
				// If the current vertex is v, remove it and decrement the number of edges
				if (w.equals(v)) {
					iter.remove();
					numEdges--;
				}
			}
		}
	}

	/**
	 * Adds a given number of vertices to the current graph.
	 *
	 * @param verticesToAdd the number of vertices to append to the graph.
	 */
	@SuppressWarnings("unused")
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
		assert adjList.size() == this.getNumVertices() : "The provided adjacency list does not contain enough lists";
		this.adjList = adjList;
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
		return numVertices;
	}

	/**
	 * Sets the number of vertices in a given graph.
	 *
	 * @param numVertices the number of vertices to be included in the graph.
	 */
	@SuppressWarnings("unused")
	public void setNumVertices(int numVertices) {
		this.numVertices = numVertices;
	}

	/**
	 * Determines whether there exists an edge between two vertices in a graph.
	 *
	 * @param v first vertex
	 * @param w second vertex
	 * @return true if v and w share an edge, false otherwise
	 */
	public boolean hasEdge(Vertex v, Vertex w) {
		return adjList.get(v.getLocation()).contains(w) || adjList.get(w.getLocation()).contains(v);
	}

	public boolean hasEdge(int i, int j) {
		return this.hasEdge(new Vertex(i), new Vertex(j));
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
		for (int i = 0; i < this.getNumVertices(); i++) {
			s.append(i).append(" ->");
			for (Vertex vertex : adjList.get(i)) s.append(" ").append(vertex);
			s.append("\n");
		}
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
		Graph that = (Graph) o;
		// TODO this should check that adjacency lists are equal as well
		boolean adjListsEqual = false;
		for (List<Vertex> thisList : this.getAdjList())
			for (List<Vertex> thatList : that.getAdjList())
				adjListsEqual = thisList.containsAll(thatList);

		return getNumVertices() == that.getNumVertices() &&
		       getNumEdges() == that.getNumEdges() && getName().equals(that.getName()) && adjListsEqual;
	}

	@Override
	public int hashCode() {
		int result = getName() != null ? getName().hashCode() : 0;
		result = 31 * result + getNumVertices();
		result = 31 * result + getNumEdges();
		result = 31 * result + time;
		result = 31 * result + (adjList != null ? adjList.hashCode() : 0);
		return result;
	}
}
