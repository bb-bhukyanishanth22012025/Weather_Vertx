package com.example.weather_vertx.controllers;

import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

public interface IWeatherController {
    
    public Handler<RoutingContext> updateWeatherData();
    public Handler<RoutingContext> deleteWeatherData();
}
