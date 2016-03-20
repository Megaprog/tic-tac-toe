package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class GameInfoController extends NameAwareController {

    @Override
    public String getType() {
        return "info";
    }

    @Override
    protected CompletableFuture<JsonObject> response(String name, JsonObject request) {
        return gameService.findGameByPlayer(name).thenApply(gameOpt -> {
            final JsonObject jsonObject = new JsonObject();

            jsonObject.put("result", gameOpt.isPresent());
            gameOpt.ifPresent(game -> jsonObject.put("game", new JsonObject(Json.encode(game))));

            return jsonObject;
        });
    }
}