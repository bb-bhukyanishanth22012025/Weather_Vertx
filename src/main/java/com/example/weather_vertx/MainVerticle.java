package com.example.weather_vertx;

import com.example.weather_vertx.db.DatabaseConfig;
import com.example.weather_vertx.routes.WeatherRoutes;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.sqlclient.Pool;

import io.vertx.core.http.HttpMethod;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {

  private static final int PORT = 8888;
  private Pool pool;

  @Override
  public void start() {

    pool = DatabaseConfig.getPool(vertx);

    Router mainRouter = Router.router(vertx);

    mainRouter.route().handler(CorsHandler.create("*")
      .allowedMethods(Set.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE))
      .allowedHeaders(Set.of("Content-Type", "Authorization"))
    );

    mainRouter.get("/").handler(ctx -> ctx.response().setStatusCode(200).end("Hello Pookie!"));

    WeatherRoutes weatherRoutes = new WeatherRoutes(vertx, pool);

    mainRouter.mountSubRouter("/api", weatherRoutes.getRouter());

    vertx.createHttpServer()
      .requestHandler(mainRouter)
      .rxListen(PORT)
      .subscribe(httpServer -> {
        System.out.println("🚀 Weather Server running on port: " + PORT);
      }, throwable -> {
        System.err.println("❌ Failed to start the server: " + throwable.getMessage());
        throwable.printStackTrace();
      });
  }

  @Override
  public void stop() {
    // Close the database pool when stopping
    DatabaseConfig.closePool();
  }
}
