package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.jmmo.tic_tac_toe.service.GameService;
import org.jmmo.tic_tac_toe.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class GameInfoController implements AsyncController {

    @Autowired
    GameService gameService;

    @Autowired
    Validator validator;

    @Override
    public String getType() {
        return "info";
    }

    @Override
    public CompletableFuture<JsonObject> response(JsonObject request) {
        return gameService.findGameByPlayer(validator.validateName(request.getString("name"))).thenApply(gameOpt -> {
            final JsonObject jsonObject = new JsonObject();

            jsonObject.put("result", gameOpt.map(game -> game.getResult1() == null || game.getResult2() == null ? "in_progress" : "finished")
                    .orElseGet(() -> "not_found"));
            gameOpt.ifPresent(game -> jsonObject.put("game", new JsonObject(Json.encode(game))));

            return jsonObject;
        });
    }
}
