package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.javatuples.Pair;
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
            final JsonObject jsonObject = new JsonObject();
            jsonObject.put("result", result.getValue0());
            if (result.getValue1() != null) {
                jsonObject.put("game", new JsonObject(Json.encode(result.getValue1())));
            }

            return jsonObject;
        });
    }
}
