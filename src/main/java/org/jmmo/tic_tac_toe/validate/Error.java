package org.jmmo.tic_tac_toe.validate;

public enum Error {

    Unknown,
    BadName,
    GameNotFound,
    GameNotFinished,
    OutOfBoard
    ;

    public GameException getException() {
        return new GameException(this);
    }

    public void fire() {
        throw getException();
    }
}
