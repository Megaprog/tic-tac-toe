package org.jmmo.tic_tac_toe.service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TupleType;
import com.datastax.driver.core.TupleValue;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;
import org.jmmo.sc.Cassandra;
import org.jmmo.tic_tac_toe.model.Game;
import org.jmmo.tic_tac_toe.model.Pending;
import org.jmmo.tic_tac_toe.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@Service
public class GameService {
    public static final int MAX_X = 2;
    public static final int MAX_Y = 2;

    @Autowired
    Cassandra cassandra;

    @Autowired
    @Qualifier("coordsTupleType")
    TupleType coordsTupleType;

    @Autowired
    Supplier<Date> dateSupplier;

    @Autowired
    Supplier<ThreadLocalRandom> randomSupplier;

    static final int PLAYER_LOCK_TTL = 60;
    static final int PENDING_TTL = (int) TimeUnit.MINUTES.toSeconds(5);
    static final int CANDIDATES_LIMIT = 20;
    static final int CANDIDATES_ATTEMPTS = 2;
    static final long MOVE_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(3);

    public int getMaxX() {
        return MAX_X;
    }

    public int getMaxY() {
        return MAX_Y;
    }

    public enum RegistrationResult {
        Wait, Preparing, GameStarted, GameFinished
    }

    public CompletableFuture<Pair<RegistrationResult, Game>> registration(String name) {
        final Player player = new Player(name, UUID.randomUUID(), null);

        return lockPlayer(player).thenCompose(resultSet -> {
            final Row updateResult = resultSet.one();
            if (cassandra.isApplied(updateResult)) {
                return findContender(name).thenCompose(contenderOpt -> contenderOpt.map(contender -> createGame(name, contender)
                        .thenApply(game -> Pair.with(RegistrationResult.GameStarted, game)))
                        .orElseGet(() -> cassandra.executeAsync(QueryBuilder.batch(
                                        cassandra.insertQuery(new Pending(dateSupplier.get(), name)).using(QueryBuilder.ttl(PENDING_TTL)),
                                        cassandra.deleteQuery(player))
                        ).thenApply(rs -> Pair.<RegistrationResult, Game>with(RegistrationResult.Wait, null))));
            } else {
                if (updateResult.getUUID("game") != null) {
                    return findGameById(updateResult.getUUID("game")).thenApply(game -> Pair.with(
                            game.isFinished() ? RegistrationResult.GameFinished : RegistrationResult.GameStarted, game));
                } else {
                    return CompletableFuture.completedFuture(Pair.with(RegistrationResult.Preparing, null));
                }
            }
        });
    }

    protected CompletableFuture<ResultSet> lockPlayer(Player player) {
        return cassandra.updateAsync(player, where ->
                where.onlyIf(QueryBuilder.eq("lock", null)).and(QueryBuilder.eq("game", null))
                .using(QueryBuilder.ttl(PLAYER_LOCK_TTL)));
    }

    public CompletableFuture<Optional<String>> findContender(String name) {
        return cassandra.selectAsync(Pending.class, where -> where.limit(CANDIDATES_LIMIT), Pending.KEY)
                .thenCompose(candidates -> {
                    candidates.removeIf(candidate -> candidate.getName().equals(name));
                    return selectContender(candidates, CANDIDATES_ATTEMPTS);
                });
    }

    protected CompletableFuture<Optional<String>> selectContender(List<Pending> candidates, int attempts) {
        if (attempts == 0 || candidates.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.<String>empty());
        } else {
            final Pending candidate = candidates.get(randomSupplier.get().nextInt(candidates.size()));
            return lockPlayer(new Player(candidate.getName(), UUID.randomUUID(), null)).thenCompose(resultSet -> {
                if (cassandra.isApplied(resultSet)) {
                    return CompletableFuture.completedFuture(Optional.of(candidate.getName()));
                } else {
                    candidates.remove(candidate);
                    return selectContender(candidates, attempts - 1);
                }
            });
        }
    }

    public CompletableFuture<Game> createGame(String player1, String player2) {
        boolean dice = randomSupplier.get().nextDouble() < 0.5;
        final Game game = new Game(UUID.randomUUID(), dice ? player1 : player2, dice ? player2 : player1, dateSupplier.get());

        return cassandra.executeAsync(QueryBuilder.batch(
                cassandra.insertQuery(game),
                cassandra.insertQuery(new Player(player1, null, game.getId())),
                cassandra.insertQuery(new Player(player2, null, game.getId()))
        )).thenApply(resultSet -> game);
    }

    public CompletableFuture<Game> findGameById(UUID gameId) {
        return cassandra.selectOneAsync(Game.class, gameId).thenApply(gameOpt -> gameOpt
                .<RuntimeException>orElseThrow(() -> new IllegalArgumentException("Game " + gameId + " is not found")))
                .thenCompose(game -> {
                    if (checkTimeout(game)) {
                        return cassandra.insertAsync(game).thenApply(resultSet -> game);
                    } else {
                        return CompletableFuture.completedFuture(game);
                    }
                });
    }

    public CompletableFuture<Optional<Game>> findGameByPlayer(String name) {
        return cassandra.selectOneAsync(Player.class, name).thenCompose(playerOpt ->
                playerOpt.map(Player::getGame).map(gameId -> findGameById(gameId).thenApply(Optional::of))
                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Game>empty())));
    }

    public CompletableFuture<Pair<Boolean, Game>> newGame(String name) {
        return findGameByPlayer(name).thenCompose(gameOpt -> gameOpt.map(game -> {
            if (!game.isFinished()) {
                return CompletableFuture.completedFuture(Pair.with(false, game));
            }

            return cassandra.insertAsync(new Player(name, null, null)).thenApply(resultSet -> Pair.with(true, game));
        }).orElseGet(() -> CompletableFuture.completedFuture(Pair.<Boolean, Game>with(true, null))));
    }

    protected boolean checkTimeout(Game game) {
        if (!game.isFinished() && dateSupplier.get().getTime() - game.getTime().getTime() > MOVE_TIMEOUT) {
            final String looser = game.getNextPlayer();
            if (game.getPlayer1().equals(looser)) {
                game.setResult1(Game.Result.Timeout);
                game.setResult2(Game.Result.Win);
            } else {
                game.setResult1(Game.Result.Win);
                game.setResult2(Game.Result.Timeout);
            }
            return true;
        } else {
            return false;
        }
    }

    public enum MoveResult {
        Ok, NoGame, GameFinished, OutOfTurn, BadMove
    }

    public CompletableFuture<Pair<MoveResult, Game>> move(String name, Pair<Integer, Integer> move) {
        return findGameByPlayer(name).thenCompose(gameOpt -> gameOpt.map(game -> {
            if (game.isFinished()) {
                return CompletableFuture.completedFuture(Pair.with(MoveResult.GameFinished, game));
            }
            if (!name.equals(game.getNextPlayer())) {
                return CompletableFuture.completedFuture(Pair.with(MoveResult.OutOfTurn, game));
            }

            final int[][] board = constructBoard(game.getMoves());
            if (board[move.getValue0()][move.getValue1()] != 0) {
                return CompletableFuture.completedFuture(Pair.with(MoveResult.BadMove, game));
            }

            final List<TupleValue> newMoves = game.getMoves() != null ? game.getMoves() : new ArrayList<>();
            newMoves.add(coordsTupleType.newValue(move.toArray()));
            game.setMoves(newMoves);
            board[move.getValue0()][move.getValue1()] = playerByMoveNumber(newMoves.size() + 1);

            final int winner = checkWinner(board);
            if (winner == 1) {
                game.setResult1(Game.Result.Win);
                game.setResult2(Game.Result.Loss);
            } else if (winner == 2) {
                game.setResult1(Game.Result.Loss);
                game.setResult2(Game.Result.Win);
            } else if (newMoves.size() == (getMaxX() + 1) * (getMaxY() + 1)) {
                game.setResult1(Game.Result.Draw);
                game.setResult2(Game.Result.Draw);
            }

            game.setTime(dateSupplier.get());

            return cassandra.insertAsync(game).thenApply(resultSet -> Pair.with(MoveResult.Ok, game));
        }).orElseGet(() -> CompletableFuture.completedFuture(Pair.<MoveResult, Game>with(MoveResult.NoGame, null))));
    }

    protected int[][] constructBoard(@Nullable List<TupleValue> coords) {
        final int[][] board = new int[getMaxX() + 1][getMaxY() + 1];

        final List<TupleValue> finalCoords = coords != null ? coords : Collections.<TupleValue>emptyList();
        IntStream.range(0, finalCoords.size()).forEach(i -> board[finalCoords.get(i).getInt(0)][finalCoords.get(i).getInt(1)] = playerByMoveNumber(i));

        return board;
    }

    protected int playerByMoveNumber(int moveNumber) {
        return (moveNumber % 2) + 1;
    }

    protected int checkWinner(int[][] board) {
        return IntStream.rangeClosed(1, 2).filter(player -> checkWinner(board, player)).findAny().orElse(0);
    }

    protected boolean checkWinner(int[][] board, int player) {
        return checkWinner(board, player, 0, 0, 1, 0) || checkWinner(board, player, 0, 0, 0, 1) || checkWinner(board, player, 0, 0, 1, 1)
                || checkWinner(board, player, getMaxX(), 0, 0, 1) || checkWinner(board, player, 0, getMaxY(), 1, 0) || checkWinner(board, player, getMaxX(), 0, -1, 1);
    }

    protected boolean checkWinner(int[][] board, int player, int x, int y, int dx, int dy) {
        while (x >= 0 && x <= getMaxX() && y >= 0 && y <= getMaxY()) {
            if (board[x][y] != player) {
                return false;
            }

            x += dx;
            y += dy;
        }

        return true;
    }
}
