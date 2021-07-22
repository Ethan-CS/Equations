package io.github.ethankelly;

import io.github.ethankelly.graph.Graph;
import io.github.ethankelly.graph.GraphGenerator;
import io.github.ethankelly.graph.Vertex;
import io.github.ethankelly.symbols.Greek;
import io.github.ethankelly.symbols.Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrintEquations {

    public static List<String> generateEquations(Tuples tuples) {
        List<String> equations = new ArrayList<>();
        char[] states = tuples.getStates();
        Arrays.sort(states);
        int numVertices = tuples.getGraph().getNumVertices();

        if (Arrays.equals(states, States.sir.states())) states = States.si.states();
        else if (Arrays.equals(states, States.sirp.states())) states = States.sip.states();

        for (Tuple tuple : tuples.getTuples()) {
            StringBuilder eqn = new StringBuilder(); // The String representation of the equation of the tuples
            eqn.append(tuple.toString()).append(" = ");
            for (Vertex vs : tuple.getSingles()) {
                switch (Character.toUpperCase(vs.getState())) {
                    case 'S' -> {
                        if (new String(states).contains("P")) {
                            if (eqn.length() > (tuple + " = ").length()) eqn.append("+ ");
                            eqn.append(Greek.ALPHA.uni()).append(Maths.L_ANGLE.uni()).append("P")
                                    .append(vs.getLocation()).append(Maths.R_ANGLE.uni());
                        }

                        for (int j = 0; j < numVertices; j++) {
                            Vertex w = new Vertex(j);
                            if (tuples.getGraph().hasEdge(vs.getLocation(), w.getLocation())) {
                                eqn.append("- ");
                                eqn.append(Greek.BETA.uni()).append(Maths.L_ANGLE.uni()).append("S").append(vs.getLocation())
                                        .append(" I").append(j).append(Maths.R_ANGLE.uni());
                            }
                        }
                        if (Arrays.equals(states, States.sirp.states()))
                            eqn.append("- ").append(Greek.ZETA.uni()).append(Maths.L_ANGLE.uni())
                                    .append("S").append(vs.getLocation()).append(Maths.R_ANGLE.uni());
                    }
                    case 'I' -> {
                        for (int j = 0; j < numVertices; j++) {
                            if (tuples.getGraph().hasEdge(vs.getLocation(), j)) {
                                if (eqn.length() > (tuple + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                                    eqn.append("+ ");
                                eqn.append(Greek.GAMMA.uni()).append(Maths.L_ANGLE.uni()).append("S").append(vs.getLocation())
                                        .append(" I").append(j).append(Maths.R_ANGLE.uni());
                            }
                        }
                        eqn.append("- ").append(Greek.GAMMA.uni()).
                                append(Maths.L_ANGLE.uni()).append("I").append(vs.getLocation()).append(Maths.R_ANGLE.uni());
                    }
                    case 'P' -> {
                        if (eqn.length() > (tuple + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                            eqn.append("+ ");
                        eqn.append(Greek.ZETA.uni()).append(Maths.L_ANGLE.uni()).append("S").append(vs.getLocation())
                                .append(Maths.R_ANGLE.uni()).append("- ").append(Greek.ALPHA.uni()).append(Maths.L_ANGLE.uni())
                                .append("P").append(vs.getLocation()).append(Maths.R_ANGLE.uni());
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
        Tuples tuples = new Tuples(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getUpperBound(Graph g, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Tuples tuples = new Tuples(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getLowerBound(int numVertices, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Graph g = GraphGenerator.cycle(numVertices);
        assert g.getCutVertices().isEmpty() : "Lower bound graph should have no cut vertices";
        assert g.getCCs().size() == 1 : "Lower bound graph should be connected";
        Tuples tuples = new Tuples(g, states, closures);
        return tuples.getTuples().size();
    }

    public static int getLowerBound(Graph g, char[] states, boolean closures) {
        //TODO this is true iff there are no cut vertices
        Tuples tuples = new Tuples(g, states, closures);
        return tuples.getTuples().size();
    }

    public static void main(String[] args) {
        char[] SIR = new char[]{'S', 'I', 'R'};

    }
}