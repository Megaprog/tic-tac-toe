package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class NewGameController extends NameAwareController {

    @Override
    public String getType() {
        return "new";
    }

    @Override
    protected CompletableFuture<JsonObject> response(String name, JsonObject request) {
        return gameService.newGame(name).thenApply(result -> {
            final JsonObject jsonObject = new JsonObject();

            jsonObject.put("result", result.getValue0());
            if (result.getValue1() != null) {
                jsonObject.put("game", new JsonObject(Json.encode(result.getValue1())));
            }

            return jsonObject;
        });
    }
}
