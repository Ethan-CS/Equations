package io.github.ethankelly.graph;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The {@code Graph} class represents a mathematical structure consisting of a collection of vertices
 * connected by edges in some specified configuration.
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public class Graph implements Cloneable {
    /** Number of sub-graphs the cut vertex belongs to after splicing . */
    private final Map<Vertex, Integer> cutVertexFreq = new HashMap<>();
    private List<Integer[][]> numWalks = new ArrayList<>();
    /** Discovery time for cut-vertex identification. */
    int time = 0;
    /** Name of the graph - generally corresponds to graph class. */
    private String name;
    /** Vertices in the graph. */
    private List<Vertex> vertices;
    /** The number of vertices in the graph. */
    private int numVertices;
    /** The number of edges in the graph. */
    private int numEdges;
    /** A linked-list representation of the graph (adjacency list). */
    private List<Map<Vertex, Double>> adjList;
    /** Labels for vertices, if required (otherwise null entries). */
    private List<Character> labels;
    /** The list of connected components after cut-vertex-based splicing. */
    private List<Graph> spliced = new ArrayList<>();
    /** Frequencies of each of the sub-graphs in the spliced graph. */
    private Map<Graph, Integer> subGraphFreq = new HashMap<>();

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
        this.adjList = new ArrayList<>();
        IntStream.range(0, numVertices).forEach(i -> {
            adjList.add(i, new LinkedHashMap<>());
            vertices.add(new Vertex(i));
        });
        this.labels = new ArrayList<>();
        IntStream.range(0, numVertices).forEach(i -> this.labels.add(' '));
    }

    /**
     * Class constructor - used to create transmission graphs.
     *
     * @param adjMatrix the adjacency matrix of the graph.
     * @param states    the states in the model, e.g. S for susceptible, R for recovered.
     */
    public Graph(boolean[][] adjMatrix, List<Character> states) {
        this.name = ""; // Default name
        this.numVertices = adjMatrix.length;
        this.vertices = new ArrayList<>();
        this.adjList = new ArrayList<>();
        // Initialise a new list for each vertex in the graph for the adjacency list
        List<Map<Vertex, Double>> list = new ArrayList<>();
        for (int i1 = 0; i1 < adjMatrix.length; i1++) {
            Map<Vertex, Double> vertexList = new HashMap<>();
            list.add(vertexList);
        }
        for (int i = 0; i < adjMatrix.length; i++) {
            vertices.add(new Vertex(i));
            for (int j = 0; j < adjMatrix[0].length; j++) {
                if (adjMatrix[i][j] && !list.get(i).containsKey(new Vertex(j))) {
                    list.get(i).put(new Vertex(j), 1.0);
                    this.numEdges++;
                }
            }
        }
        this.labels = states;
        this.adjList = list;
    }

    public Graph(int numVertices) {
        this(numVertices, "");
    }

    public static void main(String[] args) {
        Graph g = GraphGenerator.getBowTieWithBridge();
        System.out.println("BOWTIE" + g);
        Map<Graph, Integer> freq = g.getSubGraphFreq();
        System.out.println("FREQUENCY MAP\n" + freq);
        System.out.println("CUT VERTEX FREQUENCIES:\n" + g.cutVertexFreq);
    }

    public double getWeight(Vertex v, Vertex w) {
        return this.adjList.get(this.vertices.indexOf(new Vertex(v.getLocation()))).get(new Vertex(w.getLocation()));
    }

    /**
     * Uses the walks of the current graph to produce lists of states corresponding to the labels of the walks of vertices.
     *
     * @param maxLength the largest length walk we require.
     * @return A 3D list where the first index represents the length of the walks contained in the sub-lists.
     */
    public List<List<List<Character>>> getCharWalks(int maxLength) {
        List<List<List<Character>>> allCharWalks = new ArrayList<>();
        // For each length of walk up to max length,
        // Generate all possible walks in graph
        for (int i = 1; i <= maxLength; i++) {
            PathFinder pf = new PathFinder(this);
            List<List<Vertex>> allWalks = pf.findWalksOfLength(i);

            List<List<Character>> list = new ArrayList<>();
            for (List<Vertex> walk : allWalks) {
                List<Character> charWalkCandidate = new ArrayList<>();
                char prev = ' '; // Not used for states, won't be ignored by later comparison
                for (Vertex vertex : walk) {
                    char state = this.getLabels().get(this.getVertices().indexOf(vertex));
                    // Because states can be the same, possible to get duplicate adjacent states
                    // Without directly checking - hence keep track of prev for comparison
                    if (state != prev) charWalkCandidate.add(state);
                    else break;
                    prev = state;
                }
                // If we finished loop with a char walk of correct size, add to list
                if (charWalkCandidate.size() == i) list.add(charWalkCandidate);
            }
            // Add to list with index corresponding to size of the char walks found in current iteration
            allCharWalks.add(i - 1, list);
        }
        return allCharWalks;
    }

    public List<Character> getLabels() {
        return labels;
    }

    /**
     * Permits a list of labels to be assigned to the current graph instance, where labels are used for assigning
     * vertices more meaningful names than default indices.
     *
     * @param labels the labels to assign to vertices in like index locations in the graph.
     */
    public void setLabels(List<Character> labels) {
        this.labels = labels;
    }

    /**
     * @return the vertices in the graph as a list.
     */
    public List<Vertex> getVertices() {
        return vertices;
    }

    public Map<Vertex, Integer> getCutVertexFreq() {
        if (this.cutVertexFreq.isEmpty()) this.splice();
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
            GraphUtils.spliceUtil(subGraphs, cutVertices, this.clone(), this);
        } else subGraphs.add(this);

        for (int i = 0; i < subGraphs.size(); i++) {
            Graph sub = subGraphs.get(i);
            subGraphFreq.put(sub, 1);
            for (int j = 0; j < subGraphs.size(); j++) {
                Graph otherSub = subGraphs.get(j);
                // If they are not the exact same graph and are isomorphic,
                // Remove the duplicate instance and increase frequency of the subgraph
                if (i != j && sub.isIsomorphic(otherSub)) {
                    subGraphs.remove(otherSub);
                    subGraphFreq.put(sub, subGraphFreq.get(sub) + 1);
                }
            }
        }
        this.subGraphFreq = subGraphFreq;
        this.spliced = subGraphs;
        return subGraphs;
    }

    public List<List<Vertex>> splice(Vertex cut) {
        // Clone of this graph that we can safely remove edges and vertices from
        Graph clone = this.clone();
        // Remove the cut-vertex
        clone.removeVertex(cut);
        // Get the remaining connected components
        List<List<Vertex>> CCs = clone.getCCs();
        for (List<Vertex> cc : CCs) {
            if (!cc.contains(cut)) cc.add(cut);
        }
        // Return a list of remaining components
        return CCs;
    }

    // Given a list of vertices, creates a sub-graph from them with all relevant edges from the original graph
    public Graph makeSubGraph(List<Vertex> subList) {
        List<Vertex> vertices = new ArrayList<>(subList);
        vertices.sort(null);
        // Make a new graph with appropriate number of vertices, name and adjacency matrices
        Graph subGraph = new Graph(vertices.size(), getName().equals("") ? "" : getName() + " Subgraph");
        subGraph.vertices = vertices;
        for (Vertex v : vertices) {
            for (Vertex w : vertices) {
                if (this.hasEdge(v, w)) subGraph.addEdge(v, w);
            }
        }
        return subGraph;
    }

    /**
     * @return the name (often indicating graph class) of the current Graph.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adds a bidirectional edge between two vertices v and j in a graph with specified weight.
     *
     * @param v first vertex
     * @param w second vertex
     * @param d the weight of the edge.
     */
    public void addEdge(Vertex v, Vertex w, Double d) {
        // Ensure we aren't trying to add an edge between a vertex and itself
        assert !v.equals(w) : "Cannot add an edge between a vertex and itself";

        int indexOfV = this.vertices.indexOf(v);
        int indexOfW = this.vertices.indexOf(w);

        assert indexOfV >= 0 : "The vertex " + v + " does not exist in the graph";
        assert indexOfW >= 0 : "The vertex " + w + " does not exist in the graph";

        if (!this.hasEdge(v, w)) {
            // Update adjacency list
            if (!adjList.get(indexOfV).containsKey(w)) {
                adjList.get(indexOfV).put(w, d);
            }
            if (!adjList.get(indexOfW).containsKey(v)) {
                adjList.get(indexOfW).put(v, d);
            }
            // Increment the number of edges
            this.numEdges++;
            clearSpliceFields();
        }
    }

    public void addEdge(Vertex v, Vertex w) {
        addEdge(v, w, 1.0);
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
        else return (adjList.get(vertices.indexOf(v)).containsKey(w) || adjList.get(vertices.indexOf(w)).containsKey(v));
    }

    /** Number of walks of each length in the graph. */
    public List<Integer[][]> getNumWalks() {
        return numWalks;
    }

    public List<List<Vertex>> getAdjListAsLists() {
        List<List<Vertex>> adj = new ArrayList<>();
        for (Vertex v : this.getVertices()) {
            adj.add(this.getNeighbours(v));
        }
        return adj;
    }

    public void addCutVertex(Vertex cutVertex) {
        this.cutVertexFreq.put(cutVertex, 0);
    }

    private void clearSpliceFields() {
        if (this.spliced != null && !this.spliced.isEmpty()) {
            this.spliced.clear();
            this.subGraphFreq.clear();
        }
        this.numWalks = new ArrayList<>();
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
     * @param v the vertex we want to find in the vertices field.
     * @return true if v is in the vertices of the current graph, false otherwise.
     */
    public boolean contains(Vertex v) {
        return (this.vertices.contains(v));
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
        if (this.getNumVertices() != that.getNumVertices() || this.getNumEdges() != that.getNumEdges()) {
            return false;
        }

        Set<Integer> thisDegrees = new HashSet<>();
        Set<Integer> thatDegrees = new HashSet<>();

        for (int i = 0; i < this.getNumVertices(); i++) {
            int thisDeg = 0;
            int thatDeg = 0;
            for (int j = 0; j < this.getNumVertices(); j++) {
                if (this.hasEdge(vertices.get(i), vertices.get(j))) thisDeg++;
                if (that.hasEdge(that.getVertices().get(i), that.getVertices().get(j))) thatDeg++;
            }
            thisDegrees.add(thisDeg);
            thatDegrees.add(thatDeg);
        }
        for (Integer i : thisDegrees) {
            if (!thatDegrees.contains(i)) return false;
        }
        for (Integer j : thatDegrees) {
            if (!thisDegrees.contains(j)) return false;
        }
        return true;
    }

    public List<Vertex> getNeighbours(Vertex v) {
        int index = this.getVertices().indexOf(v);
        if (index < 0) {
            index = v.getLocation();
        }
        return new ArrayList<>(this.getAdjList().get(index).keySet());
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
                components.add(GraphUtils.DFS_CC_Util(this, v, visited, new ArrayList<>()));
            }
        }
        return components;
    }

    /**
     * Traverses the graph and returns a list of cut-vertices (if any) in the current graph.
     * <p>
     * Uses depth-first search with additional arrays, so time complexity is the same as DFS_ConnectedUtil i.e. O(V+E) for an
     * adjacency list representation of the graph.
     *
     * @return the cut-vertices present in the graph, if any.
     */
    public List<Vertex> getCutVertices() {
        return GraphUtils.AP(this);
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
            adjList.get(vertices.indexOf(v)).remove(w);
            adjList.get(w.getLocation()).remove(v);
            // Decrement number of edges
            this.numEdges--;
        }
        clearSpliceFields();
    }

    public void removeVertex(Vertex v) {

        // Iterate through all vertices in each list
        for (Map<Vertex, Double> map : adjList) {
            List<Vertex> toRemove = new ArrayList<>();
            for (Vertex w : map.keySet()) {
                // If the current vertex is v, remove it and decrement the number of edges
                if (w.equals(v)) {
                    toRemove.add(w);
                    numEdges--;
                }
            }
            toRemove.forEach(map::remove);
        }

        // Empty the adjacency list for the given vertex
        adjList.remove(vertices.indexOf(v));
        labels.remove(vertices.indexOf(v));
        this.vertices.remove(v);
        this.numVertices--;

        clearSpliceFields();
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
     * @return The number of vertices in the graph.
     */
    public int getNumVertices() {
        return this.getVertices().size();
    }

    /**
     * Overloaded method - allows for adding vertices by location (integer value) as a shorthand.
     *
     * @param i integer location of the first vertex of the edge.
     * @param j integer location of the second vertex of the edge.
     */
    public void addEdge(int i, int j, Double d) {
        // Ensure we aren't trying to add an edge between a vertex and itself
        assert i != j : "Cannot add an edge between a vertex and itself";
        // Update adjacency list
        if (!adjList.get(i).containsKey(new Vertex(j))) adjList.get(i).put(new Vertex(j), d);
        if (!adjList.get(j).containsKey(new Vertex(i))) adjList.get(j).put(new Vertex(i), d);
        // Increment the number of edges
        this.numEdges++;
        clearSpliceFields();
    }

    public void addEdge(int i, int j) {
        addEdge(i, j, 1.0);
    }

    /**
     * @return the adjacency list representing the current graph
     */
    public List<Map<Vertex, Double>> getAdjList() {
        return this.adjList;
    }

    /**
     * @param adjList the adjacency list to assign to the current graph
     */
    void setAdjList(List<Map<Vertex, Double>> adjList) {
//        assert adjList.size() == this.getNumVertices() : "The provided adjacency list does not contain enough lists." +
//                " Expected " + this.getNumVertices() + " but got " + adjList.size() +
//                "\n" + adjList;
        this.adjList = adjList;
        this.numVertices = adjList.size();
        clearSpliceFields();
    }

    /**
     * Sets the number of vertices in a given graph.
     *
     * @param numVertices the number of vertices to be included in the graph.
     */
    public void setNumVertices(int numVertices) {
        for (int i = this.numVertices; i < numVertices; i++) {
            this.vertices.add(new Vertex(i));
        }
        this.numVertices = numVertices;
        clearSpliceFields();
    }

    public boolean isConnected() {
        int vertices = this.getNumVertices();
        // Take a boolean visited array
        boolean[] visited = new boolean[vertices];

        // Start the DFS from vertex 0
        GraphUtils.DFS_ConnectedUtil(this, 0, visited);

        // Check if all the vertices are visited
        // Return false if at least one is not visited
        for (boolean b : visited) if (!b) return false;
        return true;
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
        assert !minConnected || (this.getNumEdges() - 1 == this.getNumVertices()) :
                "There are n-1 edges in a minimally connected graph.";
        return minConnected;
    }

    public void addWeight(int i, int j, double d) {
        if (hasDirectedEdge(i, j)) adjList.get(i).replace(new Vertex(j), d);
    }

    public void addWeight(Vertex v, Vertex w, double d) {
        if (hasDirectedEdge(v, w)) adjList.get(v.getLocation()).replace(w, d);
    }

    public void addDirectedEdge(int i, int j, double d) {
        // Ensure we aren't trying to add an edge between a vertex and itself
        assert i != j : "Cannot add an edge between a vertex and itself";
        // Update adjacency list
        adjList.get(i).put(new Vertex(j), d);
        // Increment the number of edges
        if (!this.adjList.get(j).containsKey(new Vertex(i))) this.numEdges++;
        clearSpliceFields();
    }

    public void addDirectedEdge(int i, int j) {
        addDirectedEdge(i, j, 1.0);
    }

    @SuppressWarnings("unused")
    public void addDirectedEdge(Vertex v, Vertex w, Double d) {
        // Ensure we aren't trying to add an edge between a vertex and itself
        assert !v.equals(w) : "Cannot add an edge between a vertex and itself";

        if (!adjList.get(getVertices().indexOf(v)).containsKey(w)) {
            // Update adjacency list
            adjList.get(getVertices().indexOf(v)).put(w, d);
            // Increment the number of edges if appropriate
            if (!this.adjList.get(getVertices().indexOf(w)).containsKey(v)) this.numEdges++;
        }
        clearSpliceFields();
    }

    public void addDirectedEdge(Vertex v, Vertex w) {
        addDirectedEdge(v, w, 1.0);
    }

    public boolean hasDirectedEdge(int i, int j) {
        if (i>this.getNumVertices() || j > this.getNumVertices()) return false;
        return this.adjList.get(i).containsKey(new Vertex(j));
    }

    @SuppressWarnings("unused")
    public boolean hasDirectedEdge(Vertex v, Vertex w) {
        return this.adjList.get(this.vertices.indexOf(v)).containsKey(w);
    }

    public boolean hasEdge(int i, int j) {
        if (i>this.getNumVertices() || j > this.getNumVertices()) return false;
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

    @Override
    public int hashCode() {
        int result = getNumVertices();
        result = 31 * result + (getAdjList() != null ? getAdjList().hashCode() : 0);
        return result;
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

    /**
     * @return a new instance of the current Graph object with identical parameters
     */
    @Override
    public Graph clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Graph cloned = new Graph(this.numVertices, this.name);
        // Copy across vertices for the clone
        List<Vertex> newVertices = new ArrayList<>();
        for (Vertex v : this.getVertices())
            newVertices.add(v.getState()==' ' ? new Vertex(v.getLocation()) : new Vertex(v.getState(), v.getLocation()));

        List<Character> newLabels = new ArrayList<>(this.getLabels());

        // For each edge between vertices in original, add edge in clone
        List<Map<Vertex, Double>> newAdjList = new ArrayList<>();
        for (int i = 0; i < newVertices.size(); i++) newAdjList.add(new LinkedHashMap<>());
        for (Vertex v : newVertices) {
            int i = newVertices.indexOf(v);
            for (Vertex neighbour : this.getNeighbours(v)) {
                // TODO directed?
                if (newVertices.contains(neighbour)) {
                    newAdjList.get(i).put(neighbour, this.getAdjList().get(this.vertices.indexOf(v)).get(neighbour));
                }
            }
        }
        cloned.setName(this.getName());
        cloned.setVertices(newVertices);
        cloned.setNumVertices(newVertices.size());
        cloned.setLabels(newLabels);
        cloned.setAdjList(newAdjList);
        return cloned;
    }

    private void setVertices(List<Vertex> vertices) {
        this.vertices = vertices;
    }

    public String toStringCSV(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.numVertices; i++) {
            for (int j = 0; j < this.numVertices; j++) {
                Double neighbour = this.getAdjList().get(i).get(this.getVertices().get(j));
                sb.append(neighbour == null ? "0" : "1").append(",");
            }
            sb.deleteCharAt(sb.length()-1);
            sb.append("\n");
        }
        return sb.toString();
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
            List<Character> allBlank = IntStream.range(0, this.getNumVertices()).mapToObj(j -> ' ').collect(Collectors.toList());
            if (labels == null || labels.equals(allBlank)) s.append(vertices.get(i));
            else s.append(labels.get(i));
            s.append(" ->");
            for (Vertex vertex : adjList.get(i).keySet()) {
                s.append(" ").append("(");
                if (labels == null || labels.equals(allBlank)) s.append(vertex);
                else s.append(labels.get(vertex.getLocation()));
                s.append(",").append(adjList.get(i).get(vertex)).append(")");
            }
            s.append("\n");
        }
        s.deleteCharAt(s.length() - 1);
        return s.toString();
    }
}
