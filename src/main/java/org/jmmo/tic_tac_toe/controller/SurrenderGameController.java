package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class SurrenderGameController extends NameAwareController {

    @Override
    public String getType() {
        return "surrender";
    }

    @Override
    protected CompletableFuture<JsonObject> response(String name, JsonObject request) {
        throw new UnsupportedOperationException();
    }
}
