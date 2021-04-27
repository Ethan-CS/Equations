package main.io.github.ethankelly;

import main.io.github.ethankelly.symbols.Greek;
import main.io.github.ethankelly.symbols.Maths;

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
        int[][] t = tuples.getGraph().getTransmissionMatrix();

        if (Arrays.equals(states, sir)) states = si;
        else if (Arrays.equals(states, sirp)) states = sip;

        for (List<Vertex> tuple : tuples.getTuples()) {
            StringBuilder eqn = new StringBuilder(); // The String representation of the equation of the tuples
            eqn.append(tuple.toString()).append(" = ");
            for (Vertex vertex : tuple) {
                switch (Character.toUpperCase(vertex.getState())) {
                    case 'S' -> {
                        if (new String(states).contains("P")) {
                            if (eqn.length() > (tuple + " = ").length()) eqn.append("+ ");
                            eqn.append(Greek.ALPHA.uni()).append(Maths.LANGLE.uni()).append("P")
                                    .append(vertex.getLocation()).append(Maths.RANGLE.uni());
                        }

                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                eqn.append("- ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(Greek.BETA.uni()).append(Maths.LANGLE.uni()).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(Maths.RANGLE.uni());
                            }
                        }
                        if (Arrays.equals(states, sirp))
                            eqn.append("- ").append(Greek.ZETA.uni()).append(Maths.LANGLE.uni())
                                    .append("S").append(vertex.getLocation()).append(Maths.RANGLE.uni());
                    }
                    case 'I' -> {
                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                if (eqn.length() > (tuple + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                                    eqn.append("+ ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(Greek.GAMMA.uni()).append(Maths.LANGLE.uni()).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(Maths.RANGLE.uni());
                            }
                        }
                        eqn.append("- ").append(Greek.GAMMA.uni()).
                                append(Maths.LANGLE.uni()).append("I").append(vertex.getLocation()).append(Maths.RANGLE.uni());
                    }
                    case 'P' -> {
                        if (eqn.length() > (tuple + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                            eqn.append("+ ");
                        eqn.append(Greek.ZETA.uni()).append(Maths.LANGLE.uni()).append("S").append(vertex.getLocation())
                                .append(Maths.RANGLE.uni()).append("- ").append(Greek.ALPHA.uni()).append(Maths.LANGLE.uni())
                                .append("P").append(vertex.getLocation()).append(Maths.RANGLE.uni());
                    }
                    default -> throw new IllegalStateException("Unexpected state: " + Character.toUpperCase(vertex.getState()));
                }
            }
            equations.add(String.valueOf(eqn));
        }

        return equations;
    }

    public static int getUpperBound(int numVertices, char[] states, boolean closures) {
        Graph g = GraphGenerator.complete(numVertices);
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getUpperBound(Graph g, char[] states, boolean closures) {
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getLowerBound(int numVertices, char[] states, boolean closures) {
        Graph g = GraphGenerator.cycle(numVertices);
        Tuple tuples = new Tuple(g, states, closures);
        return tuples.getTuples().size();
    }

    public static void main(String[] args) {
        char[] SIR = new char[]{'S', 'I', 'R'};
        char[] SIRP = new char[]{'S', 'I', 'R','P'};
        System.out.println(getUpperBound(4, SIR, true));
        System.out.println(getUpperBound(4, SIR, false));

        System.out.println(getLowerBound(4, SIR, true));
        System.out.println(getLowerBound(4, SIR, false));
    }
}
