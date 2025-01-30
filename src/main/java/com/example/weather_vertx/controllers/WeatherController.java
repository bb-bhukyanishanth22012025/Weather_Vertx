package com.example.weather_vertx.controllers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.sqlclient.Pool;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherController implements IWeatherController {

  private final Pool pool;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

  public WeatherController(Pool pool) {
    this.pool = pool;
  }

  @Override
  public Handler<RoutingContext> updateWeatherData() {
    return routingContext -> {
      try {

        Double latitude = Double.parseDouble(routingContext.request().getParam("latitude"));
        Double longitude = Double.parseDouble(routingContext.request().getParam("longitude"));
        String forecastTimeStr = routingContext.request().getParam("forecastTime");


        JsonObject requestBody = routingContext.getBodyAsJson();
        if (requestBody == null) {
          routingContext.response().setStatusCode(400).end("Invalid JSON body.");
          return;
        }


        Double newTemperature = requestBody.getDouble("temperature");
        if (newTemperature == null) {
          routingContext.response().setStatusCode(400).end("Temperature is required.");
          return;
        }


        LocalDateTime forecastTime = LocalDateTime.parse(forecastTimeStr, FORMATTER);


        String formattedForecastTime = forecastTime.format(FORMATTER);

        String updateQuery = "UPDATE weather_weatherforecast SET temperature = ? WHERE latitude = ? AND longitude = ? AND forecast_time = ?";

        pool.preparedQuery(updateQuery)
          .rxExecute(Tuple.of(newTemperature, latitude, longitude, formattedForecastTime))
          .subscribe(
            updateResult -> {
              if (updateResult.rowCount() == 0) {
                routingContext.response().setStatusCode(404).end("Weather data not found.");
              } else {
                JsonObject responseJson = new JsonObject()
                  .put("message", "Weather data updated successfully.");
                routingContext.response()
                  .putHeader("Content-Type", "application/json")
                  .end(responseJson.encodePrettily());
              }
            },
            error -> routingContext.response().setStatusCode(500).end("Database error: " + error.getMessage())
          );

      } catch (Exception e) {
        routingContext.response().setStatusCode(400).end("Invalid input: " + e.getMessage());
      }
    };
  }

  @Override
  public Handler<RoutingContext> deleteWeatherData() {
    return routingContext -> {
      try {
        Double latitude = Double.parseDouble(routingContext.request().getParam("latitude"));
        Double longitude = Double.parseDouble(routingContext.request().getParam("longitude"));
        String forecastTimeStr = routingContext.request().getParam("forecastTime");


        LocalDateTime forecastTime = LocalDateTime.parse(forecastTimeStr, FORMATTER);

        String formattedForecastTime = forecastTime.format(FORMATTER);

        String deleteQuery = "DELETE FROM weather_weatherforecast WHERE latitude = ? AND longitude = ? AND forecast_time = ?";

        pool.preparedQuery(deleteQuery)
          .rxExecute(Tuple.of(latitude, longitude, formattedForecastTime))
          .subscribe(
            result -> {
              if (result.rowCount() == 0) {
                routingContext.response().setStatusCode(404).end("Weather data not found.");
              } else {
                JsonObject responseJson = new JsonObject()
                  .put("message", "Weather data deleted successfully.");
                routingContext.response()
                  .putHeader("Content-Type", "application/json")
                  .end(responseJson.encodePrettily());
              }
            },
            error -> routingContext.response().setStatusCode(500).end("Database error: " + error.getMessage())
          );
      } catch (Exception e) {
        routingContext.response().setStatusCode(400).end("Invalid input: " + e.getMessage());
      }
    };
  }
}
