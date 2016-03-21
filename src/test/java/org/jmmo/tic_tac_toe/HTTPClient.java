package org.jmmo.tic_tac_toe;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

public class HTTPClient  {
    private static final Logger log = LoggerFactory.getLogger(HTTPClient.class);

    private final URL url;

    public HTTPClient(String url) {
        try {
            this.url = new URL(url);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL=" + url, e);
        }
    }

    public JsonObject connect(JsonObject jsonObject) {
        final byte[] requestBody;
        try {
            requestBody = jsonObject.toString().getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Exception during request body encoding", e);
            return null;
        }

        HttpURLConnection connection = null;
        try {
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Content-Length", String.valueOf(requestBody.length));
            } catch (IOException e) {
                log.error("Exception during opening connection", e);
                return null;
            }

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody);
            } catch (IOException e) {
                log.error("Exception during request body writing", e);
                return null;
            }

            final int responseStatus;
            try {
                responseStatus = connection.getResponseCode();
            } catch (IOException e) {
                log.error("Exception during response status reading", e);
                return null;
            }

            try (InputStream inputStream = responseStatus < 400 ? connection.getInputStream() : connection.getErrorStream()) {

                final String responseBody = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
                final JsonObject responseJson = new JsonObject(responseBody).put("status", responseStatus);

                log.debug("Server response: " + responseJson);

                return responseJson;
            } catch (IOException e) {
                log.error("Exception during response body reading", e);
                return null;
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
