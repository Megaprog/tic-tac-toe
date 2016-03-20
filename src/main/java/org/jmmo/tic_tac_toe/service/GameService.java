package org.jmmo.tic_tac_toe.service;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.javatuples.Pair;
import org.jmmo.sc.Cassandra;
import org.jmmo.tic_tac_toe.model.Game;
import org.jmmo.tic_tac_toe.model.Pending;
import org.jmmo.tic_tac_toe.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class GameService {

    @Autowired
    Cassandra cassandra;

    @Autowired
    Supplier<Date> dateSupplier;

    @Autowired
    Supplier<ThreadLocalRandom> randomSupplier;

    static final int PLAYER_LOCK_TTL = 60;
    static final int PLAYER_GAME_TTL = (int) TimeUnit.MINUTES.toSeconds(5);
    static final int PENDING_TTL = (int) TimeUnit.MINUTES.toSeconds(3);
    static final int CANDIDATES_LIMIT = 20;
    static final int CANDIDATES_ATTEMPTS = 2;
    static final int GAME_TTL = (int) TimeUnit.HOURS.toSeconds(24);
    static final int MOVE_TIMEOUT = (int) TimeUnit.MINUTES.toSeconds(3);

    public enum RegistrationResult {
        Wait, Preparing, GameStarted
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
                    return findGame(updateResult.getUUID("game")).thenApply(game -> Pair.with(RegistrationResult.GameStarted, game));
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
                cassandra.insertQuery(game).using(QueryBuilder.ttl(GAME_TTL)),
                cassandra.insertQuery(new Player(player1, null, game.getId())).using(QueryBuilder.ttl(PLAYER_GAME_TTL)),
                cassandra.insertQuery(new Player(player2, null, game.getId())).using(QueryBuilder.ttl(PLAYER_GAME_TTL))
        )).thenApply(resultSet -> game);
    }

    public CompletableFuture<Game> findGame(UUID gameId) {
        return cassandra.selectOneAsync(Game.class, gameId).thenApply(gameOpt -> gameOpt
                .<RuntimeException>orElseThrow(() -> new IllegalArgumentException("Game " + gameId + " is not found")));
    }
}
