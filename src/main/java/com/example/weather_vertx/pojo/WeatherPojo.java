package com.example.weather_vertx.pojo;

public class WeatherPojo {
    
    private Double latitude;
    private Double longitude;
    private String forecastTime;
    private Double temperature;

    public WeatherPojo() {
    }

    public WeatherPojo(Double latitude, Double longitude, String forecastTime, Double temperature) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.forecastTime = forecastTime;
        this.temperature = temperature;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getForecastTime() {
        return forecastTime;
    }

    public void setForecastTime(String forecastTime) {
        this.forecastTime = forecastTime;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    @Override
    public String toString() {
        return "WeatherPojo{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", forecastTime='" + forecastTime + '\'' +
                ", temperature=" + temperature +
                '}';
    }
}
