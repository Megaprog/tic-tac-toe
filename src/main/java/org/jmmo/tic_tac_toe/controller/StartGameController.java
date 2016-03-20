package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.jmmo.tic_tac_toe.service.GameService;
import org.jmmo.tic_tac_toe.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class StartGameController implements AsyncController {

    @Autowired
    GameService gameService;

    @Autowired
    Validator validator;

    @Override
    public String getType() {
        return "start";
    }

    @Override
    public CompletableFuture<JsonObject> response(JsonObject request) {
        return gameService.registration(validator.validateName(request.getString("name"))).thenApply(result -> {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.put("state", result.getValue0());

            switch (result.getValue0()) {
                case Wait:
                case Preparing:
                    break;
                case GameStarted:
                    jsonObject.put("game", new JsonObject(Json.encode(result.getValue1())));
                    break;
            }

            return jsonObject;
        });
    }
}
