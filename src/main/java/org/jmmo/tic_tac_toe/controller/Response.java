package org.jmmo.tic_tac_toe.controller;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class Response {
    private final boolean success;
    private final JsonObject data;

    public Response(boolean success, JsonObject data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public JsonObject getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Response response = (Response) o;
        return Objects.equals(success, response.success) &&
                Objects.equals(data, response.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, data);
    }

    @Override
    public String toString() {
        return "Response{" +
                "success=" + success +
                ", data=" + data +
                '}';
    }

    public static Response success(JsonObject jsonObject) {
        return new Response(true, jsonObject);
    }

    public static Response success() {
        return success(null);
    }

    public static Response fail(JsonObject jsonObject) {
        return new Response(false, jsonObject);
    }

    public static Response fail() {
        return fail(null);
    }
}
