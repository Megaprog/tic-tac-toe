package org.jmmo.tic_tac_toe.model;

import org.jetbrains.annotations.Nullable;
import org.jmmo.sc.annotation.Key;
import org.jmmo.sc.annotation.Table;

import java.util.Objects;
import java.util.UUID;

@Table("player")
public class Player {

    @Key
    private String name;
    @Nullable
    private UUID lock;
    @Nullable
    private UUID game;

    public Player() {
    }

    public Player(String name, @Nullable UUID lock, @Nullable UUID game) {
        this.name = name;
        this.lock = lock;
        this.game = game;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public UUID getLock() {
        return lock;
    }

    public void setLock(@Nullable UUID lock) {
        this.lock = lock;
    }

    @Nullable
    public UUID getGame() {
        return game;
    }

    public void setGame(@Nullable UUID game) {
        this.game = game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return Objects.equals(name, player.name) &&
                Objects.equals(lock, player.lock) &&
                Objects.equals(game, player.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lock, game);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", lock=" + lock +
                ", game=" + game +
                '}';
    }
}
