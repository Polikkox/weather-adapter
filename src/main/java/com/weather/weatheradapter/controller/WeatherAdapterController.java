package com.weather.weatheradapter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.weather.weatheradapter.api.DefaultApiDelegate;
import com.weather.weatheradapter.model.WeatherResponseDto;
import com.weather.weatheradapter.service.WeatherService;

@Service
public class WeatherAdapterController implements DefaultApiDelegate {

    public WeatherService weatherService;

    @Autowired
    public WeatherAdapterController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public ResponseEntity<WeatherResponseDto> checkWeather(String city) {
        return ResponseEntity.ok(weatherService.checkWeatherByCity(city));
    }
}

