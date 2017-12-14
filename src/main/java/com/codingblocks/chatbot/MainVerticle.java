package com.codingblocks.chatbot;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;


public class MainVerticle extends AbstractVerticle {

    private String VERIFY_TOKEN;
    private String ACCESS_TOKEN;

    public static Cache<String, String> cache;

    @Override
    public void start() throws Exception {

        updateProperties();

        cache = setupCache();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/webhook").handler(this::verify);
        router.get("/images/:id").handler(this::images);
        router.post("/webhook").handler(this::message);

        vertx.createHttpServer().requestHandler(router::accept)
            .listen(
                Integer.getInteger("http.port", 8080), System.getProperty("http.address", "0.0.0.0"));
    }

    public Cache<String, String> setupCache() {
        DefaultCacheManager cacheManager = new DefaultCacheManager();
        cacheManager.defineConfiguration("local", new ConfigurationBuilder().build());
        return cacheManager.getCache("local");
    }

    private void images(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        String file = MainVerticle.cache.get(id);
        routingContext.response()
            .sendFile(file);
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }


    private void verify(RoutingContext routingContext) {
        String challenge = routingContext.request().getParam("hub.challenge");
        String token = routingContext.request().getParam("hub.verify_token");
        if (!VERIFY_TOKEN.equals(token)) {
            challenge = "fake";
        }

        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(challenge);
    }

    private void message(RoutingContext routingContext) {

        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end("done");

        final JsonObject hook = routingContext.getBodyAsJson();

        JsonArray entries = hook.getJsonArray("entry");

        entries.getList().forEach( (Object e) -> {

            Map entry = (Map) e ;
            ArrayList messagingList = (ArrayList) entry.get("messaging");

            if (messagingList != null) {
                messagingList.forEach((Object m) -> {
                    Map messaging = (Map) m ;
                    MessagingUtil.processMessaging(messaging, vertx, ACCESS_TOKEN);
                });
            }

        });
    }

    private void updateProperties() {

        VERIFY_TOKEN = System.getProperty("facebook.verify.token", "verify-token-default");
        ACCESS_TOKEN = "EAAVZCYbgrbjcBAL7EPGJElK4r78MUcZBUm1ZBsvqzC4NsafHAbgFZBbeocriKZBZCs4Wf0w0mfXsEZCYDqS1IDYu1sfZAcMW9uBKkmrtv2YQqhJyBpcpe4a2tz3Je2PPFrPnxZBzLBYQ3emxEMb7thM7oUzsIRD0yrDPDn0i7ZAZBZABYtMlJQ4aSYsS";

    }
}
