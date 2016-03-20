package org.jmmo.tic_tac_toe.validate;

public enum Error {

    UNKNOWN,
    BAD_NAME,
    NO_GAME
    ;

    public GameException getException() {
        return new GameException(this);
    }

    public void fire() {
        throw getException();
    }
}
