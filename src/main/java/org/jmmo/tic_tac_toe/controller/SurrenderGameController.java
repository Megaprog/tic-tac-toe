package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class SurrenderGameController implements AsyncController {

    @Override
    public String getType() {
        return "surrender";
    }

    @Override
    public CompletableFuture<JsonObject> response(JsonObject request) {
        throw new UnsupportedOperationException();
    }
}
