package com.codingblocks.chatbot;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Map;

public class MessagingUtil {

    public static void processMessaging(Map messaging, Vertx vertx, String token) {

        Map message = (Map) messaging.get("message");
        Map sender = (Map) messaging.get("sender");
        messaging.put("recipient", sender);

        messaging.put("message", MessageUtil.processMessage(message));

        WebClientOptions options = new WebClientOptions();
        options.setSsl(true).setLogActivity(true);
        WebClient client = WebClient.create(vertx, options);

        client
            .post(443, "graph.facebook.com", "/v2.6/me/messages/")
            .addQueryParam("access_token", token)
            .sendJsonObject(JsonObject.mapFrom(messaging), ar -> {
                if (ar.succeeded()) {
                    // Obtain response
                    HttpResponse<Buffer> res = ar.result();

                    System.out.println("Received response with status code" + res.bodyAsString());
                } else {
                    System.out.println("Something went wrong " + ar.cause().getMessage());
                }
            });
    }
}
