package org.jmmo.tic_tac_toe;

import com.google.common.base.Throwables;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import org.jmmo.tic_tac_toe.controller.AsyncController;
import org.jmmo.tic_tac_toe.validate.GameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Router {
    private static final Logger log = LoggerFactory.getLogger(Router.class);

    public static final String JSON_MIME_TYPE = "application/json";

    protected Map<String, AsyncController> controllers;

    @Autowired
    public void setControllers(Collection<AsyncController> controllers) {
        this.controllers = controllers.stream().collect(Collectors.toMap(AsyncController::getType, Function.<AsyncController>identity()));
    }

    public void reply(HttpServerRequest request) {

        if (request.method() != HttpMethod.POST) {
            log.warn("Wrong HTTP method " + request.method());
            request.response().setStatusCode(405);
            request.response().end("Requires POST method");
            return;
        }

        if (!JSON_MIME_TYPE.equals(request.getHeader("Content-Type"))) {
            log.warn("Wrong MIME type " + request.getHeader("Content-Type"));
            request.response().setStatusCode(400);
            request.response().end("Requires " + JSON_MIME_TYPE + " MIME type");
            return;
        }

        request.bodyHandler(body -> {
            final JsonObject responseJson = new JsonObject();

            try {
                log.debug("Routing the request body:\n" + body.toString());

                final JsonObject requestJson = body.toJsonObject();

                final String requestType = requestJson.getString("request");
                responseJson.put("response", requestType);

                final JsonObject requestData = Optional.ofNullable(requestJson.getJsonObject("data")).orElseGet(JsonObject::new);
                responseJson.put("source", requestData);

                final AsyncController controller = controllers.get(requestType);
                if (controller == null) {
                    log.warn("Controller for request type " + requestType + " is not found");
                    writeJson(request.response(), 404, responseJson);
                    return;
                }

                log.debug("Routed to " + controller);

                controller.response(requestData).whenComplete(((response, throwable) -> {
                    if (throwable != null) {
                        handleException(throwable, request.response(), responseJson);
                    } else {
                        log.debug("Response: " + response);

                        if (response != null) {
                            responseJson.put("data", response);
                        }

                        writeJson(request.response(), 200, responseJson);
                    }
                }));
            }
            catch (Exception e) {
                handleException(e, request.response(), responseJson);
            }
        });
    }

    public void handleException(Throwable throwable, HttpServerResponse response, JsonObject jsonObject) {
        while (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }

        if (throwable instanceof GameException) {
            writeJson(response, 400, jsonObject.put("error", throwable.getMessage()));
        } else {
            writeException(response, throwable, jsonObject);
        }
    }

    public void writeJson(HttpServerResponse response, int statusCode, JsonObject jsonObject) {
        response.setStatusCode(statusCode);
        response.putHeader("Content-Type", JSON_MIME_TYPE);
        response.end(jsonObject.toString());
    }

    public void writeException(HttpServerResponse response, Throwable throwable, JsonObject jsonObject) {
        log.error("Exception during routing", throwable);
        writeJson(response, 500, jsonObject.put("exception", Throwables.getStackTraceAsString(throwable)));
    }
}
