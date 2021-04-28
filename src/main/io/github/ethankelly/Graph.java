package main.io.github.ethankelly;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
public class Graph {
    private static final int NIL = -1;
    private String name;
    private int numVertices;
    private int numEdges;
    private boolean[][] adjMatrix;
    private int[][] transmissionMatrix;
    private int time = 0; // Discovery time for cut-vertex identification

    /**
     * Class constructor.
     *
     * @param numVertices the number of vertices to create in the graph.
     */
    public Graph(int numVertices, String name) {
        this.numVertices = numVertices;
        this.adjMatrix = new boolean[numVertices][numVertices];
        this.transmissionMatrix = new int[numVertices][numVertices];
        this.name = name;
    }

    public static void main(String[] args) {
//		testMethods();

        // Create a graph given in the above diagram
        Graph g = new Graph(5, "Test"); // 5 vertices numbered from 0 to 4
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 2);
        g.addEdge(2, 3);
        g.addEdge(3, 4);
        System.out.println("The following are connected components");
        List<List<Integer>> components = g.getConnectedComponents();
        for (List<Integer> component : components) System.out.println(component);

        System.out.println("Splicing graphs based on cut vertices:");
        List<Graph> subGraphs = g.splice();

        System.out.println("The following are connected components");
        List<List<Integer>> components2 = g.getConnectedComponents();
        for (List<Integer> component : components2) System.out.println(component);

        for (Graph graph : subGraphs) System.out.println(graph);
    }

    @SuppressWarnings("unused")
    private static void testMethods() {
        //TODO put these into actual JUnit tests

        // Create graphs given in diagrams
        Graph g1 = GraphGenerator.getLollipop();
        System.out.println(g1 +
                MessageFormat.format("\nCut vertices in {0} graph: ", g1.getName()) +
                g1.getCutVertices() +
                "\nTime: " + g1.time);
        System.out.println();

        Graph g2 = GraphGenerator.getBowTie();
        System.out.println(g2 +
                MessageFormat.format("\nCut vertices in {0} graph: ", g2.getName()) +
                g2.getCutVertices() +
                "\nTime: " + g2.time);
        System.out.println();

        Graph g3 = GraphGenerator.getBowTieWithBridge();
        System.out.println(g3 +
                MessageFormat.format("\nCut vertices in {0} graph: ", g3.getName()) +
                g3.getCutVertices() +
                "\nTime: " + g3.time);

        Graph g4 = new Graph(17, "Fig. 7");
        g4.addEdge(0, 1);
        g4.addEdge(0, 3);
        g4.addEdge(1, 2);
        g4.addEdge(1, 3);
        g4.addEdge(1, 4);
        g4.addEdge(2, 4);
        g4.addEdge(4, 5);
        g4.addEdge(5, 6);
        g4.addEdge(5, 8);
        g4.addEdge(6, 7);
        g4.addEdge(6, 8);
        g4.addEdge(8, 11);
        g4.addEdge(9, 10);
        g4.addEdge(10, 12);
        g4.addEdge(11, 12);
        g4.addEdge(11, 13);
        g4.addEdge(11, 14);
        g4.addEdge(12, 13);
        g4.addEdge(13, 16);
        g4.addEdge(14, 15);
        System.out.println(g4 +
                MessageFormat.format("\nCut vertices in {0} graph: ", g4.getName()) +
                g4.getCutVertices() +
                "\nTime: " + g4.time);
    }

    /**
     * A recursive function that finds cut-vertices using the depth-first search algorithm.
     *
     * @param u           The vertex to be visited next.
     * @param visited     Keeps track of visited vertices.
     * @param times       Stores discovery times of visited vertices.
     * @param low         Used to determine whether a vertex has a connection to the ancestor of an adjacent vertex.
     * @param parent      Stores parent vertices in a depth-first search tree.
     * @param cutVertices Stores any found cut-vertices.
     */
    public void findCutVertices(int u, boolean[] visited, int[] times, int[] low, int[] parent, boolean[] cutVertices) {
        //TODO This is over-zealous! Cuts should only be identified if there's a component of size 2 or more after
        int children = 0; // Counter for children in the DFS Tree
        visited[u] = true; // Mark the current node visited
        times[u] = low[u] = ++time; // Initialise discovery time and low value
        LinkedList<Integer>[] adj = getAdjacencyList(); // Go through all vertices adjacent to this
        // v is current adjacent of u
        for (int v : adj[u]) {
            // If v is not visited yet, make it a child of u in DFS tree and recurse
            if (!visited[v]) {
                children++;
                parent[v] = u;
                findCutVertices(v, visited, times, low, parent, cutVertices);
                // Check if the subtree rooted with v has a connection to one of the ancestors of u
                low[u] = Math.min(low[u], low[v]);
                /* u is a cut vertex if either of the following apply:
                 * (1) u is root of DFS tree and has two or more children, or
                 * (2) The low value of one of its child is more than its own discovery value.
                 */
                if (parent[u] == NIL && children > 1) cutVertices[u] = true; // (1)
                if (parent[u] != NIL && low[v] >= times[u]) cutVertices[u] = true; // (2)
            } else if (v != parent[u]) {
                // Update the low value of u for parent function calls.
                low[u] = Math.min(low[u], times[v]);
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
        List<Graph> subGraphs = new ArrayList<>();
        List<Integer> cutVertices = this.getCutVertices();
        if (cutVertices.isEmpty()) subGraphs.add(this);
        else {
            for (Integer cutVertex : cutVertices) {
                System.out.println("Removing edges of " + cutVertex);
                removeBridges(cutVertex);
            }
            List<List<Integer>> connectedComponents = this.getConnectedComponents();
            for (List<Integer> cc : connectedComponents) {
                subGraphs.add(this.makeSubGraph(cc));
            }
        }
        return subGraphs;
    }

    private List<Integer> DFSUtil(int v, boolean[] visited, List<Integer> thisComponent) {
        // Mark the current node as visited and print it
        visited[v] = true;
        thisComponent.add(v);
        // Recur for all the vertices
        // adjacent to this vertex
        for (int w = 0; w < this.getAdjMatrix().length; w++) {
            if (!visited[w] && this.getAdjMatrix()[v][w]) DFSUtil(w, visited, thisComponent);
        }
        return thisComponent;
    }

    /**
     * Finds the connected components in the current graph and returns them as a list of lists of integers, each sub-
     * list representing a sub-graph (connected component).
     *
     * @return a list of connected components as lists of integers.
     */
    public List<List<Integer>> getConnectedComponents() {
        List<List<Integer>> components = new ArrayList<>();
        // Mark all the vertices as not visited
        boolean[] visited = new boolean[this.getNumVertices()];
        for (int v = 0; v < this.getNumVertices(); ++v) {
            if (!visited[v]) {
                // Get all reachable vertices from v
                components.add(DFSUtil(v, visited, new ArrayList<>()));
            }
        }
        return components;
    }

    private Graph makeSubGraph(List<Integer> vertices) {

        Graph subGraph = new Graph(vertices.size(), this.getName() + " Subgraph");
        for (int v = 0; v < vertices.size(); v++) {
            for (int w = 0; w < this.getNumVertices(); w++) {
                if (this.isEdge(vertices.get(v), w) && vertices.contains(w)) subGraph.addEdge(v, vertices.indexOf(w));
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
    public List<Integer> getCutVertices() {
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
        for (int i = 0; i < numVertices; i++)
            if (!visited[i]) findCutVertices(i, visited, disc, low, parent, cutVertices);

        // Now cutVertices contains articulation points, return them
        return IntStream.range(0, cutVertices.length)
                .filter(i -> cutVertices[i]).boxed().collect(Collectors.toList());
    }

    /**
     * Adds a bidirectional edge between two vertices i and j in a graph
     *
     * @param i first vertex
     * @param j second vertex
     */
    public void addEdge(int i, int j) {
        boolean[][] adjMat = getAdjMatrix();
        int[][] tMat = getTransmissionMatrix();
        adjMat[i][j] = true;
        adjMat[j][i] = true;
        tMat[i][j] = 1;
        tMat[j][i] = 1;
        this.setTransmissionMatrix(tMat);
        this.setAdjMatrix(adjMat);
        this.setNumEdges(getNumEdges() + 1);
    }

    /**
     * Removes an edge between the two vertices specified from a graph
     *
     * @param i first vertex
     * @param j second vertex
     */
    public void removeEdge(int i, int j) {
        boolean[][] adjMat = getAdjMatrix();
        int[][] tMat = getTransmissionMatrix();
        adjMat[i][j] = false;
        adjMat[j][i] = false;
        tMat[i][j] = 0;
        tMat[j][i] = 0;
        this.setAdjMatrix(adjMat);
        this.setTransmissionMatrix(tMat);
    }

    public void removeBridges(int i) {
        int numConnectedComponents = getConnectedComponents().size();
        for (int j = 0; j < getNumVertices(); j++) {
            if (isEdge(i, j)) {
                removeEdge(i, j);
                // Ensure any edges we remove are bridges; preserve other edges
                if (getConnectedComponents().size() == numConnectedComponents) addEdge(i, j);
                else for (List<Integer> cc : getConnectedComponents()) if (cc.size() == 1) addEdge(i, j);
            }
        }
    }

    public boolean isMinimallyConnected() {
        boolean minConnected = true;
        // Check if graph is connected at all
        if (getConnectedComponents().size() > 1) minConnected = false;
        else for (int i = 0; i < getNumVertices(); i++) {
            for (int j = 0; j < getNumVertices(); j++) {
                if (isEdge(i, j)) {
                    removeEdge(i, j);
                    if (getConnectedComponents().size() == 1) minConnected = false;
                }
            }
        }
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
     * An adjacency matrix represents a (finite) graph by indicating whether a pair of vertices share an edge between
     * them. At the intersection of row i, column j, if the value is 1 there is an edge from i to j and if it's zero
     * then there is no such edge. In our uses, graphs will mostly be bidirectional, meaning this value will be the same
     * in row i, column j and row j, column i.
     *
     * @return the adjacency matrix of the graph
     */
    public boolean[][] getAdjMatrix() {
        return adjMatrix;
    }

    /**
     * Some methods update the adjacency matrix when called - this method makes this update.
     *
     * @param adjMatrix the updated version of the adjacency matrix to set.
     */
    public void setAdjMatrix(boolean[][] adjMatrix) {
        this.adjMatrix = adjMatrix;
    }

    /**
     * Determines whether there exists an edge between two vertices in a graph.
     *
     * @param i first vertex
     * @param j second vertex
     * @return true if i and j share an edge, false otherwise
     */
    public boolean isEdge(int i, int j) {
        return getAdjMatrix()[i][j] || getAdjMatrix()[j][i];
    }

    /**
     * Returns a textual representation of a graph as an adjacency matrix to be printed to the standard output.
     *
     * @return a string representation of the graph.
     */
    @Override
    public String toString() {
        int n = getNumVertices();
        boolean[][] matrix = getAdjMatrix();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            s.append(i).append(": ");
            for (boolean j : matrix[i]) {
                s.append(j ? 1 : 0).append(" ");
            }
            s.append("\n");
        }
        return s.toString();
    }

    @SuppressWarnings("unchecked")
    // Helper - converts a boolean adjacency matrix to a linked list
    private LinkedList<Integer>[] getAdjacencyList() {
        // First, get the boolean adjacency matrix
        boolean[][] adjMatrix = this.getAdjMatrix();
        int numVertices = adjMatrix[0].length;

        // Create a new list for each vertex
        // where adjacent nodes can be stored
        LinkedList<Integer>[] adjListArray = IntStream.range(0, numVertices).<LinkedList<Integer>>mapToObj(
                i -> new LinkedList<>()).toArray(LinkedList[]::new);

        for (int i = 0; i < adjMatrix[0].length; i++) {
            for (int j = 0; j < adjMatrix.length; j++) {
                if (adjMatrix[i][j]) adjListArray[i].add(j);
            }
        }
        return adjListArray;

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

    public int[][] getTransmissionMatrix() {
        return transmissionMatrix;
    }

    public void setTransmissionMatrix(int[][] transmissionMatrix) {
        this.transmissionMatrix = transmissionMatrix;
    }

    public String getName() {
        return this.name;
    }

    public Graph setName(String name) {
        this.name = name;
        return this;
    }

}
