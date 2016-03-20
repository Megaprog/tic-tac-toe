CREATE TABLE IF NOT EXISTS game (
    id uuid,
    player1 text,
    player2 text,
    time timestamp,
    result1 int,
    result2 int,
    moves list<FROZEN<tuple<int, int>>>,
    PRIMARY KEY (id)
);