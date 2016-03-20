package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;
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
        return null;
    }
}
