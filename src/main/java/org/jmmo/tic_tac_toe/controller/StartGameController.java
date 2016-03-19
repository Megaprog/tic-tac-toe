package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public class StartGameController implements AsyncController {

    @Override
    public String getType() {
        return "start";
    }

    @Override
    public CompletableFuture<Response> response(JsonObject request) {
        return CompletableFuture.completedFuture(Response.success());
    }
}
