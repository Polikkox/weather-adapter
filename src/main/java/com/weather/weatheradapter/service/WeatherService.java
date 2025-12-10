package com.weather.weatheradapter.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.weatheradapter.model.WeatherResponseDto;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String weatherApiKey;

    @Value("${weather.api.base-url}")
    private String weatherApiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public WeatherResponseDto checkWeatherByCity(String city) {
        try {
            String url = String.format("%s/v1/current.json?key=%s&q=%s&aqi=no", weatherApiBaseUrl, weatherApiKey, city);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            WeatherResponseDto weatherResponse = new WeatherResponseDto();
            weatherResponse.setCity(jsonNode.get("location").get("name").asText());
            weatherResponse.setTemperature(jsonNode.get("current").get("temp_c").asText() + "°C");
            weatherResponse.setDate(LocalDate.now());

            return weatherResponse;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data for city: " + city, e);
        }
    }
}
