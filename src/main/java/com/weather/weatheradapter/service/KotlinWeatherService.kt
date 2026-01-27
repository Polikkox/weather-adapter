package com.weather.weatheradapter.service;

import com.fasterxml.jackson.databind.ObjectMapper
import com.weather.weatheradapter.model.WeatherResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import org.springframework.web.client.getForObject

@Service
class KotlinWeatherService(
    @Value("\${weather.api.key}")
    private val weatherApiKey: String,

    @Value("\${weather.api.base-url}")
    private val weatherApiBaseUrl: String,

    ) {

    private val restTemplate = RestTemplate()
    private val objectMapper = ObjectMapper()

    fun checkWeatherByCity(city: String): WeatherResponseDto {
        return try {
            val url = "$weatherApiBaseUrl/v1/current.json?key=$weatherApiKey&q=$city&aqi=no"

            val response = restTemplate.getForObject<String>(url)
            val jsonNode = objectMapper.readTree(response)

            WeatherResponseDto().apply {
                this.city = jsonNode["location"]["name"].asText()
                this.temperature = jsonNode["current"]["temp_c"].asText() + "°C"
                this.date = LocalDate.now()
            }

        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch weather data for city: $city", e)
        }
    }
}
