package io.github.ethankelly.exceptions;

public class UnexpectedStateException extends Exception {
    public UnexpectedStateException() {
        super();
    }

    public UnexpectedStateException(String message) {
        super(message);
    }

    public UnexpectedStateException(Throwable cause) {
        super(cause);
    }

    public UnexpectedStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
