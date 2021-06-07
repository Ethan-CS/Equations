package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.graph.VertexState;
import io.github.ethankelly.symbols.Greek;
import io.github.ethankelly.symbols.Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class Equations {

    public static List<String> generateEquations(Tuple tuples) {
        List<String> equations = new ArrayList<>();
        char[] states = tuples.getStates();
        Arrays.sort(states);
        char[] si = new char[]{'S', 'I'};
        char[] sir = new char[]{'S', 'I', 'R'};
        char[] sip = new char[]{'S', 'I', 'P'};
        char[] sirp = new char[]{'S', 'I', 'R', 'P'};
        Arrays.sort(si);
        Arrays.sort(sir);
        Arrays.sort(sip);
        Arrays.sort(sirp);
        int numVertices = tuples.getGraph().getNumVertices();

        if (Arrays.equals(states, sir)) states = si;
        else if (Arrays.equals(states, sirp)) states = sip;

        for (List<VertexState> tuple : tuples.getTuples()) {
            StringBuilder eqn = new StringBuilder(); // The String representation of the equation of the tuples
            eqn.append(tuple.toString()).append(" = ");
            for (VertexState vs : tuple) {
                switch (Character.toUpperCase(vs.getState())) {
                    case 'S' -> {
                        if (new String(states).contains("P")) {
                            if (eqn.length() > (tuple + " = ").length()) eqn.append("+ ");
                            eqn.append(Greek.ALPHA.uni()).append(Maths.LANGLE.uni()).append("P")
                                    .append(vs.getLocation()).append(Maths.RANGLE.uni());
                        }

                        for (int j = 0; j < numVertices; j++) {
                            Vertex w = new Vertex(j);
                            if (tuples.getGraph().hasEdge(vs.getLocation(), w.getLocation())) {
                                eqn.append("- ");
                                eqn.append(Greek.BETA.uni()).append(Maths.LANGLE.uni()).append("S").append(vs.getLocation())
                                        .append(" I").append(j).append(Maths.RANGLE.uni());
                            }
                        }
                        if (Arrays.equals(states, sirp))
                            eqn.append("- ").append(Greek.ZETA.uni()).append(Maths.LANGLE.uni())
                                    .append("S").append(vs.getLocation()).append(Maths.RANGLE.uni());
                    }
                    case 'I' -> {
                        for (int j = 0; j < numVertices; j++) {
                            if (tuples.getGraph().hasEdge(vs.getLocation(), j)) {
                                if (eqn.length() > (tuple + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                                    eqn.append("+ ");
                                eqn.append(Greek.GAMMA.uni()).append(Maths.LANGLE.uni()).append("S").append(vs.getLocation())
                                        .append(" I").append(j).append(Maths.RANGLE.uni());
                            }
                        }
                        eqn.append("- ").append(Greek.GAMMA.uni()).
                                append(Maths.LANGLE.uni()).append("I").append(vs.getLocation()).append(Maths.RANGLE.uni());
                    }
                    case 'P' -> {
                        if (eqn.length() > (tuple + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                            eqn.append("+ ");
                        eqn.append(Greek.ZETA.uni()).append(Maths.LANGLE.uni()).append("S").append(vs.getLocation())
                                .append(Maths.RANGLE.uni()).append("- ").append(Greek.ALPHA.uni()).append(Maths.LANGLE.uni())
                                .append("P").append(vs.getLocation()).append(Maths.RANGLE.uni());
                    }
                    default -> throw new IllegalStateException("Unexpected state: " + Character.toUpperCase(vs.getState()));
                }
            }
            equations.add(String.valueOf(eqn));
        }

        return equations;
    }

    public static int getUpperBound(int numVertices, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Graph g = GraphGenerator.complete(numVertices);
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getUpperBound(Graph g, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getLowerBound(int numVertices, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Graph g = GraphGenerator.cycle(numVertices);
        assert g.getCutVertices().isEmpty() : "Lower bound graph should have no cut vertices";
        assert g.getCCs().size() == 1 : "Lower bound graph should be connected";
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getLowerBound(Graph g, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static void main(String[] args) {
        char[] SIR = new char[]{'S', 'I', 'R'};

        Graph g = GraphGenerator.getLollipop();
        System.out.println(g);
        System.out.print("\nCut vertices: ");
        g.getCutVertices().forEach(System.out::print);
        System.out.println("\nUB with closures: " + getUpperBound(g.getNumVertices(), SIR, true));
        System.out.println("LB with closures: " + getLowerBound(g.getNumVertices(), SIR, true));
        List<Graph> subGraphs = g.splice();
        for (Graph subGraph : subGraphs) {
            System.out.println(subGraph);
        }

        Graph h = GraphGenerator.getBowTie();
        System.out.println(h);
        System.out.print("\nCut vertices: ");
        h.getCutVertices().forEach(System.out::print);
        System.out.println("\nUB with closures: " + getUpperBound(h.getNumVertices(), SIR, true));
        System.out.println("LB with closures: " + getLowerBound(h.getNumVertices(), SIR, true));
        List<Graph> subGraphsH = h.splice();
        for (Graph subGraph : subGraphsH) {
            System.out.println(subGraph);
        }
    }
}
