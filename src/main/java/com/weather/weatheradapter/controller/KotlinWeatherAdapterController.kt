package com.weather.weatheradapter.controller;

import com.weather.weatheradapter.api.DefaultApiDelegate
import com.weather.weatheradapter.model.WeatherResponseDto
import com.weather.weatheradapter.service.KotlinWeatherService
import org.springframework.context.annotation.Primary
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
@Primary
class KotlinWeatherAdapterController(
    private val weatherService: KotlinWeatherService
) : DefaultApiDelegate {

    override fun checkWeather(city: String): ResponseEntity<WeatherResponseDto> = ResponseEntity.ok(weatherService.checkWeatherByCity(city))

}





