package io.github.ethankelly.graph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The {@code Graph} class represents a graph - a collection of vertices connected by edges in some meaningful
 * configuration. While the basis for the model is agency, this agency is represented as interactions of vertices of a
 * graph.
 * <p>
 * Using a graph to represent and examine allows the use of many excellent existing results in graph theory, plus the
 * use of a graph is not an unusual choice of visualisation in models of contagion even intuitively. The graphs to be
 * used in this model, objects of this class, have two important attributes:
 * <ul>
 *     <li> The number of vertices in the graph.
 *     <li> An adjacency matrix - each entry is true if the vertices represented by the row
 *          and column are connected by an edge, false otherwise
 *          (@see <a href ="https://mathworld.wolfram.com/AdjacencyMatrix.html">https://mathworld.wolfram.com/AdjacencyMatrix.html</a>).
 * </ul>
 * In much of the literature, graphs of this kind represent 'contact networks.'
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public class Graph implements Cloneable {
	private static final int NIL = -1;
	private int time = 0; // Discovery time for cut-vertex identification

	private String name;
	private int numVertices;
	private int numEdges;
	List<LinkedList<Vertex>> adjList = new LinkedList<>();

	/**
	 * Class constructor.
	 *
	 * @param numVertices        the number of vertices to create in the graph.
	 */
	public Graph(int numVertices, String name) {
		this.numVertices = numVertices;
		this.name = name;
		IntStream.range(0, numVertices).forEach(i -> adjList.add(i, new LinkedList<>()));
		this.time = 0;
	}

	public Graph(List<Vertex> vertices, String name) {
		this.numVertices = vertices.size();
		this.name = name;
		AtomicInteger i = new AtomicInteger();
		vertices.forEach(v -> adjList.add(i.getAndIncrement(), new LinkedList<>()));
		this.time = 0;
	}

	public Graph(Graph clone) {
		this.name = clone.name;
		this.numVertices = clone.numVertices;
		this.numEdges = clone.numEdges;
		this.adjList = clone.adjList;
		this.time = clone.time;
	}

	public static void main(String[] args) {
		Graph lollipop = GraphGenerator.getLollipop();
		System.out.println(lollipop);
		List<Graph> lollipopCC = lollipop.splice();
		System.out.println("--- Spliced ---");
		for (Graph g : lollipopCC) System.out.println(g);
	}

	public void removeEdgesOfVertex(Vertex v) {
		for (int j = 0; j < getNumVertices(); j++) {
			Vertex w = new Vertex(j);
			if (hasEdge(v, w)) removeEdge(v, w);
		}
	}

	/*
	 * A recursive function that finds cut-vertices using the depth-first search algorithm.
	 */
	private void findCutVertices(Vertex u, boolean[] visited, int[] times, int[] low, int[] parent, boolean[] cutVertices) {
		int children = 0; // Counter for children in the DFS Tree
		visited[u.getLocation()] = true; // Mark the current node visited
		times[u.getLocation()] = low[u.getLocation()] = ++time; // Initialise discovery time and low value
		LinkedList<Vertex> adj = adjList.get(u.getLocation());
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
				if (parent[u.getLocation()] != NIL && low[v.getLocation()] >= times[u.getLocation()]) cutVertices[u.getLocation()] = true; // (2)
			} else if (v.getLocation() != parent[u.getLocation()]) {
				// Update the low value of u for parent function calls.
				low[u.getLocation()] = Math.min(low[u.getLocation()], times[v.getLocation()]);
			}
		}
	}

	/*
	Go through graph, deleting edges connected to each cut vertex.
	Then, go back through graph searching for connected components.
	Every time we have a connected component, i.e. group of vertices only accessible to each other,
	send it to the subgraph creator method and store the result in the list of graphs.
	Then, repeat until we have done this with all connected components (create a method that tells us how
	many connected components we have, so we know when we've finished this process).
	Then, return this list of sub-graphs.
	 */
	public List<Graph> splice() {
		// Empty List of Graphs that will contain our sub-graphs
		List<Graph> subGraphs = new ArrayList<>();
		List<Vertex> cutVertices = this.getCutVertices();
		Graph original = this.clone();
		Graph clone = this.clone();
		// If there are no cut vertices, add the graph to the list as it cannot be spliced further
		if (cutVertices.isEmpty()) subGraphs.add(this);
		else {
			for (Vertex cutVertex : cutVertices) {
				System.out.println("before removing cut vertex:");
				System.out.println(original);

				System.out.println("Removing edges of " + cutVertex);
				clone.removeEdgesOfVertex(cutVertex);

				System.out.println("removed cut vertex; graph now looks like this:");
				System.out.println(original);

				List<List<Vertex>> newCCs = clone.getConnectedComponents();
				for (List<Vertex> cc : newCCs) {
					if (!cc.contains(cutVertex)) cc.add(cutVertex);
					if (!cc.equals(Collections.singletonList(cutVertex))) subGraphs.add(makeSubGraph(cc, original));
				}
			}

		}
		return subGraphs;
	}

	private List<Vertex> DFSUtil(Vertex v, boolean[] visited, List<Vertex> thisComponent) {
		// Mark the current node as visited and print it
		visited[v.getLocation()] = true;
		thisComponent.add(v);
		// Recur for all the vertices
		// adjacent to this vertex
		for (int i = 0; i < adjList.size(); i++) {
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
	public List<List<Vertex>> getConnectedComponents() {
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

	private Graph makeSubGraph(List<Vertex> vertices, Graph original) {
		// Make a new graph with appropriate number of vertices, name and adjacency matrices
		Graph subGraph = new Graph(vertices, this.getName() + " Subgraph");

		List<Vertex> newVertices = new ArrayList<>();
		int i = 0;
		for (Vertex v : vertices) newVertices.add(i++, v);
		System.out.println("I got to " + i);

		for (Vertex v : newVertices) {
			for (Vertex w : newVertices) {
				if (original.hasEdge(v, w) &&
				    !subGraph.hasEdge(newVertices.indexOf(v), newVertices.indexOf(w))) {
					subGraph.addEdge(newVertices.indexOf(v), newVertices.indexOf(w));
				}
			}
		}
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
		boolean[] cutVertices = new boolean[numVertices]; // To store articulation points

		// Initialize parent and visited, and cutVertices(articulation point)
		// arrays
		for (int i = 0; i < numVertices; i++) {
			parent[i] = NIL;
			visited[i] = false;
			cutVertices[i] = false;
		}

		// Call the recursive helper function to find articulation
		// points in DFS tree rooted with vertex 'i'
		for (int i = 0; i < adjList.size(); i++)
			if (!visited[i]) findCutVertices(new Vertex(i), visited, disc, low, parent, cutVertices);

		// Now cutVertices contains articulation points, return them
		return IntStream.range(0, cutVertices.length).filter(i -> cutVertices[i]).mapToObj(Vertex::new).collect(Collectors.toList());
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
		adjList.get(v.getLocation()).addFirst(w);
		adjList.get(w.getLocation()).addFirst(v);
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
		this.addEdge(new Vertex(i), new Vertex(j));
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
				if (this.hasEdge(v, w)) that.addEdge(i, j);
			}
		}
		this.setNumVertices(newNumVert);
		this.setNumVertices(that.getNumVertices());
		this.setAdjList(that.getAdjList());
//		this.setAdjMatrix(that.getAdjMatrix());
	}

	private void setAdjList(List<LinkedList<Vertex>> adjList) {
		this.adjList = adjList;
	}

	private List<LinkedList<Vertex>> getAdjList() {
		return this.adjList;
	}

	@SuppressWarnings("unused")
	public boolean isMinimallyConnected() {
		boolean minConnected = true;
		// Check if graph is connected at all
		if (getConnectedComponents().size() > 1) minConnected = false;
		else for (int i = 0; i < getNumVertices(); i++) {
			Vertex v = new Vertex(i);
			for (int j = 0; j < getNumVertices(); j++) {
				Vertex w = new Vertex(j);
				if (hasEdge(v, w)) {
					removeEdge(v, w);
					if (getConnectedComponents().size() == 1) minConnected = false;
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
		Vertex v = new Vertex(i);
		Vertex w = new Vertex(j);
		return this.hasEdge(v, w);
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

	public String getName() {
		return this.name;
	}

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
			s.append(i).append("->");
			for (Vertex vertex : adjList.get(i)) s.append(" ").append(vertex);
			s.append("\n");
		}
		return s.toString();
	}

	@Override
	public Graph clone() {
		Graph cloned;
		try {
			cloned = (Graph) super.clone();
		} catch (CloneNotSupportedException e) {
			cloned = new Graph(this.getNumVertices(), this.getName());
		}

		List<LinkedList<Vertex>> newList = new ArrayList<>();
		for (LinkedList<Vertex> list : this.getAdjList()) {
			int index = this.getAdjList().indexOf(list);
			newList.add(index, new LinkedList<>());
			for (Vertex v : list) newList.get(index).add(v);
		}
		cloned.setName(this.getName());
		cloned.setAdjList(newList);
		return cloned;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Graph graph = (Graph) o;
		return getNumVertices() == graph.getNumVertices() &&
		       getNumEdges() == graph.getNumEdges() &&
		       Objects.equals(getName(), graph.getName());
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
