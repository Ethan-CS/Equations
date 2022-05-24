package io.github.ethankelly.graph.decomposition;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.symbols.Greek;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathDecomposition {
    /** Root token of the path decomposition */
    Vertex root = null;
    /** Graph to decompose. */
    Graph graph;
    /** Mapping of tokened vertices to their tokens (represented as strings) */
    Map<Vertex, String> tokened;
    /** List that stores the tokened vertices at each timestep of the decomposition */
    List<List<Vertex>> tokenedAtTime;
    /** Mapping of each vertex to the colour it has been assigned (blue or red) */
    Map<Vertex, COLOUR> colourMap;

    enum COLOUR {
        /** Blue vertices have not had a token assigned to them */
        BLUE,
        /** Red vertices have had a token at some point, possibly currently */
        RED
    }

    public PathDecomposition(Graph graph) {
        this.graph = graph;
        this.tokened = new HashMap<>();
        this.tokenedAtTime = new ArrayList<>();
        this.colourMap = new HashMap<>();
        // Initially set all vertices to blue
        for (Vertex v : this.graph.getVertices()) colourMap.put(v, COLOUR.BLUE);
    }

    public static void main(String[] args) {
        Graph g = new Graph(9);
        PathDecomposition pd = new PathDecomposition(g);
    }

    private List<Vertex> growTokenTree() {
        List<Vertex> tokenedVertices = new ArrayList<>();

        // if root token not yet placed, place arbitrarily on graph
        if (root == null) {
            setRoot(this.graph.getVertices().get(0));
        }

        // while there's a vertex u with token T with a blue neighbour v
        // and token has an unplaced child T.b,
        // place T.b on v
        String T = "";
        do {
            Vertex v = null;
            l:
            for (Vertex u : tokened.keySet()) {
                T = tokened.get(u);
                for (Vertex neighbour : this.graph.getAdjList().get(u.getLocation()).keySet()) {
                    if (this.colourMap.get(neighbour).equals(COLOUR.BLUE)) {
                        v = neighbour;
                        break l;
                    }
                }
            }
            if (v == null) break;
            else {
                // if T has an unplaced child T.b...
                // TODO how do we decide 0 or 1?
                //  Maybe a LinkedList to keep track?
                tokened.put(v, T + "");
                tokenedVertices.add(v);
            }
        } while (true);

        // return the tokened vertices of the graph
        return tokenedVertices;
    }

    public void decompose() {
        int i = 0;
        tokenedAtTime.add(i, growTokenTree());
        // while |P[i]|=f(t) or H has no blue vertices
        {
            // pick a token T with untokened children

            // remove T from H

            // if T had one tokened child then
            // replace all tokens T.b.S with T.S

//				i++;
//				tokenedAtTime.add(i, growTokenTree());
        }
    }

    private void setRoot(Vertex v) {
        this.root = v;
        tokened.put(v, Greek.LAMBDA.uni());
        colourMap.replace(v, COLOUR.RED);
    }
}
