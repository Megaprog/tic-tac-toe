package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;
import org.jmmo.tic_tac_toe.service.GameService;
import org.jmmo.tic_tac_toe.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.concurrent.CompletableFuture;

@Controller
public abstract class NameAwareController implements AsyncController {

    @Autowired
    protected GameService gameService;

    @Autowired
    protected Validator validator;

    @Override
    final public CompletableFuture<JsonObject> response(JsonObject request) {
        return response(validator.validateName(request.getString("name")), request);
    }

    abstract protected CompletableFuture<JsonObject> response(String name, JsonObject request);
}
