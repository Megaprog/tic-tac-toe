package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;
import org.jmmo.tic_tac_toe.validate.Error;
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
            if (!result.getValue0()) {
                throw Error.GameNotFinished.getException();
            }

            return null;
        });
    }
}
