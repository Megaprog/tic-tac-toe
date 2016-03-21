package org.jmmo.tic_tac_toe;

import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import org.jmmo.tic_tac_toe.config.CommonConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScenarioIT.Config.class)
public class ScenarioIT {
    private static final Logger log = LoggerFactory.getLogger(ScenarioIT.class);

    public static final Integer OK = 200;
    public static final Integer BAD = 400;

    public static final String REQUEST = "request";
    public static final String STATUS = "status";
    public static final String ERROR = "error";
    public static final String NAME = "name";
    public static final String DATA = "data";
    public static final String GAME = "game";
    public static final String MOVE = "move";

    @Configuration
    @PropertySource(value = "classpath:/test.properties", ignoreResourceNotFound = true)
    @PropertySource(value = "file:${ttt.config}", ignoreResourceNotFound = true)
    @Import({CommonConfig.class})
    static class Config {
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (System.getProperty("ttt.config") == null) {
            System.setProperty("ttt.config", "classpath:test.properties");
        }

        Launcher.main(new String[]{"run", "org.jmmo.tic_tac_toe.Server"});

        Thread.sleep(3000);
    }

    @Value("http://localhost:${ttt.http.port:8080}")
    String url;

    @Value("${ttt.game.claimTTL:30}")
    int claimTTL;

    @Value("${ttt.game.moveTimeout:180}")
    int moveTimeoutSec;

    HTTPClient httpClient;

    String p1 = "p1";
    String p2 = "p2";

    static JsonObject gameJson;
    JsonObject jsonObject;
    JsonObject moveJson;

    @Before
    public void setUp() throws Exception {
        httpClient = new HTTPClient(url);
    }

    @Test
    public void test000_BadName() throws Exception {
        JsonObject jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "start"));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("BadName", jsonObject.getString(ERROR));
    }

    @Test
    public void test010_Start() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "start").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals("Wait", jsonObject.getJsonObject(DATA).getString("result"));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "start").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals("GameStarted", jsonObject.getJsonObject(DATA).getString("result"));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertEquals(p1, gameJson.getString("player1"));
        assertEquals(p2, gameJson.getString("player2"));
        assertFalse(gameJson.getBoolean("finished"));
        assertEquals(p1, gameJson.getString("nextPlayer"));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "start").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals("GameStarted", jsonObject.getJsonObject(DATA).getString("result"));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));
    }

    @Test
    public void test020_Info() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));
    }

    @Test
    public void test030_New_Fail() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "new").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(BAD, jsonObject.getInteger(STATUS));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "new").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
    }

    @Test
    public void test040_Move_Fail() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", 5).put("y", 1))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("OutOfBoard", jsonObject.getString(ERROR));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", -1).put("y", 1))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("OutOfBoard", jsonObject.getString(ERROR));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 1).put("y", -1))));
        assertEquals("OutOfBoard", jsonObject.getString(ERROR));
        assertEquals(BAD, jsonObject.getInteger(STATUS));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 1).put("y", 5))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("OutOfBoard", jsonObject.getString(ERROR));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 1).put("y", 1))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("OutOfTurn", jsonObject.getString(ERROR));
    }

    @Test
    public void test050_Move1() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", 1).put("y", 1))));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertFalse(gameJson.getBoolean("finished"));
        assertEquals(p2, gameJson.getString("nextPlayer"));
        assertEquals(1, gameJson.getJsonArray("moves").size());

        moveJson = gameJson.getJsonArray("moves").getJsonObject(0);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(1, moveJson.getInteger("y").intValue());
    }

    @Test
    public void test060_Move_Bad() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", 2).put("y", 0))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("OutOfTurn", jsonObject.getString(ERROR));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 1).put("y", 1))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("BadMove", jsonObject.getString(ERROR));
    }

    @Test
    public void test070_Move2() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 1).put("y", 2))));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertFalse(gameJson.getBoolean("finished"));
        assertEquals(p1, gameJson.getString("nextPlayer"));
        assertEquals(2, gameJson.getJsonArray("moves").size());

        moveJson = gameJson.getJsonArray("moves").getJsonObject(0);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(1, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(1);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
    }

    @Test
    public void test080_Move3() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", 2).put("y", 0))));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertFalse(gameJson.getBoolean("finished"));
        assertEquals(p2, gameJson.getString("nextPlayer"));
        assertEquals(3, gameJson.getJsonArray("moves").size());

        moveJson = gameJson.getJsonArray("moves").getJsonObject(0);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(1, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(1);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(2);
        assertEquals(2, moveJson.getInteger("x").intValue());
        assertEquals(0, moveJson.getInteger("y").intValue());
    }

    @Test
    public void test090_Move4() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 2).put("y", 2))));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertFalse(gameJson.getBoolean("finished"));
        assertEquals(p1, gameJson.getString("nextPlayer"));
        assertEquals(4, gameJson.getJsonArray("moves").size());

        moveJson = gameJson.getJsonArray("moves").getJsonObject(0);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(1, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(1);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(2);
        assertEquals(2, moveJson.getInteger("x").intValue());
        assertEquals(0, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(3);
        assertEquals(2, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
    }


    @Test
    public void test100_InfoInGameProcess() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));
    }

    @Test
    public void test110_Move5_Win() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", 0).put("y", 2))));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertTrue(gameJson.getBoolean("finished"));
        assertNull(p1, gameJson.getString("nextPlayer"));
        assertEquals(5, gameJson.getJsonArray("moves").size());
        assertEquals("Win", gameJson.getString("result1"));
        assertEquals("Loss", gameJson.getString("result2"));

        moveJson = gameJson.getJsonArray("moves").getJsonObject(0);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(1, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(1);
        assertEquals(1, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(2);
        assertEquals(2, moveJson.getInteger("x").intValue());
        assertEquals(0, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(3);
        assertEquals(2, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
        moveJson = gameJson.getJsonArray("moves").getJsonObject(4);
        assertEquals(0, moveJson.getInteger("x").intValue());
        assertEquals(2, moveJson.getInteger("y").intValue());
    }

    @Test
    public void test120_Move_GameFinished() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p2)
                .put(MOVE, new JsonObject().put("x", 2).put("y", 1))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("GameFinished", jsonObject.getString(ERROR));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "move").put(DATA, new JsonObject().put(NAME, p1)
                .put(MOVE, new JsonObject().put("x", 0).put("y", 1))));
        assertEquals(BAD, jsonObject.getInteger(STATUS));
        assertEquals("GameFinished", jsonObject.getString(ERROR));
    }

    @Test
    public void test130_InfoAfterFinished() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));
    }

    @Test
    public void test140_NewGame() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "new").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "new").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
    }

    @Test
    public void test150_NewStart() throws Exception {
        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "start").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals("Wait", jsonObject.getJsonObject(DATA).getString("result"));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "start").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals("GameStarted", jsonObject.getJsonObject(DATA).getString("result"));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertEquals(p1, gameJson.getString("player2"));
        assertEquals(p2, gameJson.getString("player1"));
        assertFalse(gameJson.getBoolean("finished"));
        assertEquals(p2, gameJson.getString("nextPlayer"));
    }

    @Test
    public void test160_Timeout() throws Exception {
        log.info("Waiting for move timeout " + moveTimeoutSec + " seconds");
        Thread.sleep(TimeUnit.SECONDS.toMillis(moveTimeoutSec + 1));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p1)));
        assertEquals(OK, jsonObject.getInteger(STATUS));

        gameJson = jsonObject.getJsonObject(DATA).getJsonObject(GAME);
        assertTrue(gameJson.getBoolean("finished"));
        assertNull(p1, gameJson.getString("nextPlayer"));
        assertEquals("Timeout", gameJson.getString("result1"));
        assertEquals("Win", gameJson.getString("result2"));

        jsonObject = httpClient.connect(new JsonObject().put(REQUEST, "info").put(DATA, new JsonObject().put(NAME, p2)));
        assertEquals(OK, jsonObject.getInteger(STATUS));
        assertEquals(gameJson, jsonObject.getJsonObject(DATA).getJsonObject(GAME));
    }
}
