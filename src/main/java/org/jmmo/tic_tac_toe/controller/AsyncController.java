package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;

public interface AsyncController {

    String getType();

    CompletableFuture<Response> response(JsonObject request);
}
