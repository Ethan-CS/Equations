package io.github.ethankelly;

public enum Greek {

    ALPHA("\u03B1"),
    BETA("\u03B2"),
    GAMMA("\u03B3"),
    DELTA("\u03B4"),
    EPSILON("\u03B5"),
    ZETA("\u03B6"),
    ETA("\u03B7"),
    THETA("\u03B8"),
    IOTA("\u03B9"),
    KAPPA("\u03BA"),
    LAMBDA("\u03BB"),
    MU("\u03BC"),
    NU("\u03BD"),
    XI("\u03BE"),
    OMICRON("\u03BF"),
    PI("\u03C0"),
    RHO("\u03C1"),
    SIGMA("\u03C3"),
    TAU("\u03C4"),
    UPSILON("\u03C5"),
    PHI("\u03C6"),
    CHI("\u03C7"),
    PSI("\u03C8"),
    OMEGA("\u03C9");


    private final String uni;

    /**
     * Class constructor, used to associate the inputted value with a given state.
     */
    Greek(String uni) {
        this.uni = uni;
    }

    /**
     * Each letter has a String unicode value associated to it, which is used to print it to the standard output.
     *
     * @return the value of the letter.
     */
    public String uni() {
        return this.uni;
    }
}
