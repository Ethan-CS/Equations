package main.io.github.ethankelly.symbols;

public enum Maths {

    LANGLE("\u3008"),
    RANGLE("\u3009");

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
