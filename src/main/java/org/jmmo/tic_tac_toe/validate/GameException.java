package org.jmmo.tic_tac_toe.validate;

public class GameException extends RuntimeException {
    private final Error error;

    public GameException() {
        this(Error.UNKNOWN);
    }

    public GameException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return getError().toString();
    }
}
