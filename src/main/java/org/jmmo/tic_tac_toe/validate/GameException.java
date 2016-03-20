package org.jmmo.tic_tac_toe.validate;

public class GameException extends RuntimeException {
    private Error error = Error.Unknown;

    public GameException() {
    }

    public GameException(String message) {
        super(message);
    }

    public GameException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }

    @Override
    public String getMessage() {
        return getError() != Error.Unknown || super.getMessage() == null ? getError().toString() : super.getMessage();
    }
}
