package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
import org.jmmo.tic_tac_toe.service.GameService;
import org.jmmo.tic_tac_toe.validate.*;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class MoveController extends NameAwareController {

    @Override
    public String getType() {
        return "move";
    }

    @Override
    protected CompletableFuture<JsonObject> response(String name, JsonObject request) {
        final JsonObject moveJson = request.getJsonObject("move", new JsonObject());
        return gameService.move(name, validator.validateMove(Pair.with(moveJson.getInteger("x"), moveJson.getInteger("y")))).thenApply(result -> {
            if (result.getValue0() != GameService.MoveResult.Ok) {
                throw new GameException(result.getValue0().toString());
            }

            return new JsonObject().put("game", new JsonObject(Json.encode(result.getValue1())));
        });
    }
}
