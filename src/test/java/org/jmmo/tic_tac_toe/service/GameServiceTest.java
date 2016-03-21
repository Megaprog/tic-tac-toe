package org.jmmo.tic_tac_toe.service;

import org.javatuples.Pair;
import org.jmmo.tic_tac_toe.AbstractPersistence;
import org.jmmo.tic_tac_toe.config.PersistenceConfig;
import org.jmmo.tic_tac_toe.model.Game;
import org.jmmo.tic_tac_toe.model.Claim;
import org.jmmo.tic_tac_toe.model.Player;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.Assert.*;

public class GameServiceTest extends AbstractPersistence {

    @Autowired
    GameService gameService;

    @Before
    public void setUp() throws Exception {
        PersistenceConfig.truncateTables(cassandra);
    }

    @Test
    public void testRegistration() throws Exception {
        assertEquals(Pair.with(GameService.RegistrationResult.Wait, null), gameService.registration("p1").get());
        Claim claim = cassandra.selectOne(Claim.class, Claim.KEY).get();
        assertEquals("p1", claim.getName());

        Pair<GameService.RegistrationResult, Game> registration = gameService.registration("p2").get();
        assertEquals(GameService.RegistrationResult.GameStarted, registration.getValue0());
        assertNotNull(registration.getValue1());
        assertEquals("p2", registration.getValue1().getPlayer1());
        assertEquals("p1", registration.getValue1().getPlayer2());
        assertEquals(testDateSupplier.get(), registration.getValue1().getTime());
        assertEquals(new Player("p1", null, registration.getValue1().getId()), cassandra.selectOne(Player.class, "p1").get());
        assertEquals(new Player("p2", null, registration.getValue1().getId()), cassandra.selectOne(Player.class, "p2").get());

        assertEquals(0, cassandra.select(Claim.class, Claim.KEY).count());

        cassandra.insert(new Player("p3", UUID.randomUUID(), null));
        assertEquals(Pair.with(GameService.RegistrationResult.Preparing, null), gameService.registration("p3").get());

        Game game = new Game(UUID.randomUUID(), "p4", "p5", testDateSupplier.get());
        cassandra.insert(game);
        cassandra.insert(new Player("p4", null, game.getId()));
        assertEquals(Pair.with(GameService.RegistrationResult.GameStarted, game), gameService.registration("p4").get());
    }
}