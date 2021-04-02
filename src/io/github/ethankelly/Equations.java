package io.github.ethankelly;

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
                            if (eqn.length() > (tuple.toString() + " = ").length()) eqn.append("+ ");
                            eqn.append(Greek.ALPHA.uni()).append(Symbol.LANGLE.uni()).append("P")
                                    .append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                        }

                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                eqn.append("- ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(Greek.BETA.uni()).append(Symbol.LANGLE.uni()).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(Symbol.RANGLE.uni());
                            }
                        }
                        if (Arrays.equals(states, sirp))
                            eqn.append("- ").append(Greek.ZETA.uni()).append(Symbol.LANGLE.uni())
                                    .append("S").append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                    }
                    case 'I' -> {
                        for (int j = 0; j < numVertices; j++) {
                            if (t[vertex.getLocation()][j] > 0) {
                                if (eqn.length() > (tuple.toString() + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                                    eqn.append("+ ");
                                if (t[vertex.getLocation()][j] != 1) eqn.append(t[vertex.getLocation()][j]);
                                eqn.append(Greek.GAMMA.uni()).append(Symbol.LANGLE.uni()).append("S").append(vertex.getLocation())
                                        .append(" I").append(j).append(Symbol.RANGLE.uni());
                            }
                        }
                        eqn.append("- ").append(Greek.GAMMA.uni()).
                                append(Symbol.LANGLE.uni()).append("I").append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                    }
                    case 'P' -> {
                        if (eqn.length() > (tuple.toString() + " = ").length() && eqn.charAt(eqn.length() - 1) != '+')
                            eqn.append("+ ");
                        eqn.append(Greek.ZETA.uni()).append(Symbol.LANGLE.uni()).append("S").append(vertex.getLocation())
                                .append(Symbol.RANGLE.uni()).append("- ").append(Greek.ALPHA.uni()).append(Symbol.LANGLE.uni())
                                .append("P").append(vertex.getLocation()).append(Symbol.RANGLE.uni());
                    }
                }
            }
            equations.add(String.valueOf(eqn));
        }

        return equations;
    }
}
