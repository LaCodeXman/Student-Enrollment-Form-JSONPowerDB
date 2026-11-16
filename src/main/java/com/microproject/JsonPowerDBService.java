
package com.microproject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class JsonPowerDBService {

    private static final String BASE_URL =
            "https://api.login2explore.com:5577";

    private static final String DATABASE = "SCHOOL_DB";
    private static final String TABLE = "STUDENT_TABLE";

    private final String token;
    private final HttpClient client;

    public JsonPowerDBService() {

        token = System.getenv("JPDB_TOKEN");

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "JPDB_TOKEN environment variable is missing."
            );
        }
        System.setProperty(
        "javax.net.ssl.trustStore",
        "jpdb-truststore.p12"
        );

        System.setProperty(
        "javax.net.ssl.trustStorePassword",
        "changeit"
        );
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    private JsonObject sendRequest(String endpoint, JsonObject body)
            throws IOException, InterruptedException {

        body.addProperty("token", token);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(
                    "JSONPowerDB HTTP error: "
                            + response.statusCode()
                            + " - " + response.body()
            );
        }

        try {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (Exception ex) {
            throw new IOException(
                    "Invalid response from JSONPowerDB: " + response.body(), ex
            );
        }
    }

    public JsonObject getStudent(int rollNo)
            throws IOException, InterruptedException {

        JsonObject body = new JsonObject();
        body.addProperty("cmd", "GET_BY_KEY");
        body.addProperty("dbName", DATABASE);
        body.addProperty("rel", TABLE);

        JsonObject key = new JsonObject();
        key.addProperty("Roll_No", rollNo);

        body.add("jsonStr", key);

        return sendRequest("/api/irl", body);
    }

    public JsonObject insertStudent(JsonObject student)
            throws IOException, InterruptedException {

        JsonObject body = new JsonObject();

        body.addProperty("cmd", "SET");
        body.addProperty("dbName", DATABASE);
        body.addProperty("rel", TABLE);
        body.addProperty("type", "PUT");
        body.addProperty("primaryKey", "Roll_No");

        body.add("jsonStr", student);

        return sendRequest("/api/iml/set", body);
    }

    public JsonObject updateStudent(JsonObject student)
            throws IOException, InterruptedException {

        JsonObject body = new JsonObject();

        body.addProperty("cmd", "SET");
        body.addProperty("dbName", DATABASE);
        body.addProperty("rel", TABLE);
        body.addProperty("type", "UPDATE");
        body.addProperty("primaryKey", "Roll_No");

        body.add("jsonStr", student);

        return sendRequest("/api/iml/set", body);
    }
}
