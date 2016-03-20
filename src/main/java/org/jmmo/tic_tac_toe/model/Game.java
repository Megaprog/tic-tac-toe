package org.jmmo.tic_tac_toe.model;

import com.datastax.driver.core.TupleValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.Nullable;
import org.jmmo.sc.annotation.Key;
import org.jmmo.sc.annotation.Table;
import org.jmmo.tic_tac_toe.json.TupleToCoordinatesSerializer;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table("game")
public class Game {

    public enum Result {
        Win, Loss, Draw, Surrender, Timeout
    }

    @Key
    private UUID id;

    private String player1;
    private String player2;

    private Date time;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private Result result1;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private Result result2;

    @JsonSerialize(contentUsing = TupleToCoordinatesSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Nullable
    private List<TupleValue> moves;

    public Game() {
    }

    public Game(UUID id, String player1, String player2, Date time) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.time = time;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Nullable
    public Result getResult1() {
        return result1;
    }

    public void setResult1(@Nullable Result result1) {
        this.result1 = result1;
    }

    @Nullable
    public Result getResult2() {
        return result2;
    }

    public void setResult2(@Nullable Result result2) {
        this.result2 = result2;
    }

    @Nullable
    public List<TupleValue> getMoves() {
        return moves;
    }

    public void setMoves(@Nullable List<TupleValue> moves) {
        this.moves = moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Game game = (Game) o;
        return Objects.equals(id, game.id) &&
                Objects.equals(player1, game.player1) &&
                Objects.equals(player2, game.player2) &&
                Objects.equals(time, game.time) &&
                Objects.equals(result1, game.result1) &&
                Objects.equals(result2, game.result2) &&
                Objects.equals(moves, game.moves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, player1, player2, time, result1, result2, moves);
    }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                ", time=" + time +
                ", result1=" + result1 +
                ", result2=" + result2 +
                ", moves=" + moves +
                '}';
    }
}
