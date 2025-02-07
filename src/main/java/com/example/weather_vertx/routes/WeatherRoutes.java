package com.example.weather_vertx.routes;

import com.example.weather_vertx.controllers.WeatherController;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.sqlclient.Pool;


public class WeatherRoutes {

  private final Router router;
  private final Pool pool;
  private final WeatherController weatherController;

  public WeatherRoutes(Vertx vertx, Pool pool) {
    this.router = Router.router(vertx);
    this.pool = pool;
    this.weatherController = new WeatherController(pool);

    router.route().handler(BodyHandler.create());

    router.put("/weather").handler(weatherController.updateWeatherData());
    router.delete("/weather").handler(weatherController.deleteWeatherData());
  }

  public Router getRouter() {
    return router;
  }
}
