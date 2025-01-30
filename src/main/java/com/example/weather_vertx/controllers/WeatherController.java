package com.example.weather_vertx.controllers;

import com.example.weather_vertx.pojo.WeatherPojo;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.sqlclient.Pool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherController implements IWeatherController {
    
    private final Pool mySQLPool;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public WeatherController(Pool mySQLPool) {
        this.mySQLPool = mySQLPool;
    }

    @Override
    public Handler<RoutingContext> updateWeatherData() {
        return routingContext -> {
            try {
                Double latitude = Double.parseDouble(routingContext.request().getParam("latitude"));
                Double longitude = Double.parseDouble(routingContext.request().getParam("longitude"));
                String forecastTimeStr = routingContext.request().getParam("forecastTime");
                Double newTemperature = Double.parseDouble(routingContext.request().getParam("temperature"));

                // Convert String to LocalDateTime
                LocalDateTime forecastTime = LocalDateTime.parse(forecastTimeStr, FORMATTER);

                String selectQuery = "SELECT * FROM weather WHERE latitude = ? AND longitude = ? AND forecastTime = ?";
                String updateQuery = "UPDATE weather SET temperature = ? WHERE latitude = ? AND longitude = ? AND forecastTime = ?";
                String fetchUpdatedQuery = "SELECT * FROM weather WHERE latitude = ? AND longitude = ? AND forecastTime = ?";

                mySQLPool.preparedQuery(selectQuery)
                    .rxExecute(Tuple.of(latitude, longitude, forecastTime))
                    .flatMap(rowSet -> {
                        if (rowSet.rowCount() == 0) {
                            routingContext.response().setStatusCode(404).end("Weather data not found.");
                            return null;
                        }
                        return mySQLPool.preparedQuery(updateQuery)
                            .rxExecute(Tuple.of(newTemperature, latitude, longitude, forecastTime));
                    })
                    .flatMap(result -> mySQLPool.preparedQuery(fetchUpdatedQuery)
                        .rxExecute(Tuple.of(latitude, longitude, forecastTime)))
                    .subscribe(
                        updatedRowSet -> {
                            if (updatedRowSet.rowCount() > 0) {
                                Row row = updatedRowSet.iterator().next();
                                WeatherPojo updatedWeather = new WeatherPojo(
                                    row.getDouble("latitude"),
                                    row.getDouble("longitude"),
                                    row.getLocalDateTime("forecastTime").format(FORMATTER),
                                    row.getDouble("temperature")
                                );
                                JsonObject responseJson = JsonObject.mapFrom(updatedWeather);
                                responseJson.put("message", "Temperature updated successfully.");
                                routingContext.response()
                                    .putHeader("Content-Type", "application/json")
                                    .end(responseJson.encodePrettily());
                            } else {
                                routingContext.response().setStatusCode(500).end("Error fetching updated data.");
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

                // Convert String to LocalDateTime
                LocalDateTime forecastTime = LocalDateTime.parse(forecastTimeStr, FORMATTER);

                String deleteQuery = "DELETE FROM weather WHERE latitude = ? AND longitude = ? AND forecastTime = ?";

                mySQLPool.preparedQuery(deleteQuery)
                    .rxExecute(Tuple.of(latitude, longitude, forecastTime))
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
