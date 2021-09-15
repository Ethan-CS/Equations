package io.github.ethankelly.symbols;

/**
 * An enum containing maths symbols used in the project with their associated unicode values.
 *
 * @author <a href="mailto:e.kelly.1@research.gla.ac.uk">Ethan Kelly</a>
 */
public enum Maths {

    L_ANGLE("\u3008"),
    R_ANGLE("\u3009"),
    PRIME("\u2032");

	private final String uni;

    Maths(String uni) {
        this.uni = uni;
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
