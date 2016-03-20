package org.jmmo.tic_tac_toe.validate;

public enum Error {

    UNKNOWN,
    BAD_NAME,
    GAME_NOT_FINISHED,
    OUT_OF_BOARD
    ;

    public GameException getException() {
        return new GameException(this);
    }

    public void fire() {
        throw getException();
    }
}
