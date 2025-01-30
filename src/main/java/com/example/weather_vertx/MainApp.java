package com.example.weather_vertx;

import io.vertx.reactivex.core.Vertx;

public class MainApp{

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("HttpRouterVerticle deployed successfully.");
            } else {
                System.err.println("Failed to deploy HttpRouterVerticle: " + res.cause());
            }
        });
    }
}
