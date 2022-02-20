package io.github.ethankelly.symbols;

/**
 * An enum containing maths symbols used in the project with their associated unicode values.
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
@SuppressWarnings("unused")
public enum Maths {
    L_ANGLE("\u3008"),
    R_ANGLE("\u3009"),
    PRIME("\u2032"),
    SUB_0("\u2080"),
    SUB_1("\u2081"),
    SUB_2("\u2082"),
    SUB_3("\u2083"),
    SUB_4("\u2084"),
    SUB_5("\u2085"),
    SUB_6("\u2086"),
    SUB_7("\u2087"),
    SUB_8("\u2088"),
    SUB_9("\u2089"),
    SUB_PLUS("\u208A"),
    SUB_MINUS("\u208B"),
    SUB_EQUALS("\u208C"),
    SUB_OPEN_BRACKET("\u208D"),
    SUB_CLOSE_BRACKET("\u208E");

	private final String uni;

    Maths(String uni) {
        this.uni = uni;
    }

    public String subFromVal(int i) {
        return switch (i) {
            case 0 -> SUB_0.uni();
            case 1 -> SUB_1.uni();
            case 2 -> SUB_2.uni();
            case 3 -> SUB_3.uni();
            case 4 -> SUB_4.uni();
            case 5 -> SUB_5.uni();
            case 6 -> SUB_6.uni();
            case 7 -> SUB_7.uni();
            case 8 -> SUB_8.uni();
            case 9 -> SUB_9.uni();
            default -> "";
        };
    }

    /**
     * Each symbol has a String unicode value associated to it, which is used to print it to the standard output.
     *
     * @return the value of the letter.
     */
    public String uni() {
        return this.uni;
    }
}
