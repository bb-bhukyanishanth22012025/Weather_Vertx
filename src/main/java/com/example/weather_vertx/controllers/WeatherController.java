package com.example.weather_vertx.controllers;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.sqlclient.Pool;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class WeatherController implements IWeatherController {

  private final Pool pool;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  public WeatherController(Pool pool) {
    this.pool = pool;
  }

  @Override
  public Handler<RoutingContext> updateWeatherData() {
    return routingContext -> {
      try {
        Double latitude = validateLatitude(routingContext.request().getParam("latitude"));
        Double longitude = validateLongitude(routingContext.request().getParam("longitude"));
        LocalDateTime forecastTime = validateForecastTime(routingContext.request().getParam("forecastTime"));

        JsonObject requestBody = routingContext.getBodyAsJson();
        if (requestBody == null) {
          sendErrorResponse(routingContext, 400, "Invalid JSON body.");
          return;
        }

        Double newTemperature = requestBody.getDouble("temperature");
        if (newTemperature == null) {
          sendErrorResponse(routingContext, 400, "Temperature is required.");
          return;
        }

        String updateQuery = "UPDATE weather_weatherforecast SET temperature = ? WHERE latitude = ? AND longitude = ? AND forecast_time = ?";

        pool.preparedQuery(updateQuery)
          .rxExecute(Tuple.of(newTemperature, latitude, longitude, forecastTime.format(FORMATTER)))
          .subscribe(
            updateResult -> {
              if (updateResult.rowCount() == 0) {
                sendErrorResponse(routingContext, 404, "Weather data not found.");
              } else {
                sendSuccessResponse(routingContext, "Weather data updated successfully.");
              }
            },
            error -> sendErrorResponse(routingContext, 500, "Database error: " + error.getMessage())
          );

      } catch (IllegalArgumentException e) {
        sendErrorResponse(routingContext, 400, e.getMessage());
      } catch (Exception e) {
        sendErrorResponse(routingContext, 400, "Invalid input: " + e.getMessage());
      }
    };
  }

  @Override
  public Handler<RoutingContext> deleteWeatherData() {
    return routingContext -> {
      try {
        Double latitude = validateLatitude(routingContext.request().getParam("latitude"));
        Double longitude = validateLongitude(routingContext.request().getParam("longitude"));
        LocalDateTime forecastTime = validateForecastTime(routingContext.request().getParam("forecastTime"));

        String deleteQuery = "DELETE FROM weather_weatherforecast WHERE latitude = ? AND longitude = ? AND forecast_time = ?";

        pool.preparedQuery(deleteQuery)
          .rxExecute(Tuple.of(latitude, longitude, forecastTime.format(FORMATTER)))
          .subscribe(
            result -> {
              if (result.rowCount() == 0) {
                sendErrorResponse(routingContext, 404, "Weather data not found.");
              } else {
                sendSuccessResponse(routingContext, "Weather data deleted successfully.");
              }
            },
            error -> sendErrorResponse(routingContext, 500, "Database error: " + error.getMessage())
          );

      } catch (IllegalArgumentException e) {
        sendErrorResponse(routingContext, 400, e.getMessage());
      } catch (Exception e) {
        sendErrorResponse(routingContext, 400, "Invalid input: " + e.getMessage());
      }
    };
  }

  /**
   * Validates and parses latitude.
   */
  private Double validateLatitude(String latitudeStr) {
    if (latitudeStr == null) throw new IllegalArgumentException("Latitude is required.");
    try {
      double latitude = Double.parseDouble(latitudeStr);
      if (latitude < -90 || latitude > 90) throw new IllegalArgumentException("Latitude must be between -90 and 90.");
      return latitude;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid latitude format.");
    }
  }


  private Double validateLongitude(String longitudeStr) {
    if (longitudeStr == null) throw new IllegalArgumentException("Longitude is required.");
    try {
      double longitude = Double.parseDouble(longitudeStr);
      if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("Longitude must be between -180 and 180.");
      return longitude;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid longitude format.");
    }
  }


  private LocalDateTime validateForecastTime(String forecastTimeStr) {
    if (forecastTimeStr == null) throw new IllegalArgumentException("Forecast time is required.");
    try {
      return LocalDateTime.parse(forecastTimeStr, FORMATTER);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid forecastTime format.");
    }
  }


  private void sendErrorResponse(RoutingContext routingContext, int statusCode, String message) {
    routingContext.response()
      .setStatusCode(statusCode)
      .putHeader("Content-Type", "application/json")
      .end(new JsonObject().put("error", message).encodePrettily());
  }


  private void sendSuccessResponse(RoutingContext routingContext, String message) {
    routingContext.response()
      .setStatusCode(200)
      .putHeader("Content-Type", "application/json")
      .end(new JsonObject().put("message", message).encodePrettily());
  }
}
