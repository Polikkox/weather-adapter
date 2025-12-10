package com.weather.weatheradapter.controller;

import com.weather.weatheradapter.model.WeatherResponseDto;
import com.weather.weatheradapter.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherAdapterControllerUnitTest {

    @Mock
    private WeatherService weatherService;

    private WeatherAdapterController weatherAdapterController;

    @BeforeEach
    void setUp() {
        weatherAdapterController = new WeatherAdapterController(weatherService);
    }

    @Test
    void checkWeather_ValidCity_ReturnsOkWithWeatherData() {
        WeatherResponseDto mockResponse = new WeatherResponseDto();
        mockResponse.setCity("Warsaw");
        mockResponse.setTemperature("25.5°C");
        mockResponse.setDate(LocalDate.of(2023, 10, 15));

        when(weatherService.checkWeatherByCity("Warsaw")).thenReturn(mockResponse);

        ResponseEntity<WeatherResponseDto> result = weatherAdapterController.checkWeather("Warsaw");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Warsaw", result.getBody().getCity());
        assertEquals("25.5°C", result.getBody().getTemperature());
        assertEquals(LocalDate.of(2023, 10, 15), result.getBody().getDate());
    }

    @Test
    void checkWeather_ServiceThrowsException_PropagatesException() {
        when(weatherService.checkWeatherByCity("InvalidCity"))
                .thenThrow(new RuntimeException("External API error"));

        assertThrows(RuntimeException.class,
                () -> weatherAdapterController.checkWeather("InvalidCity"));
    }

    @Test
    void checkWeather_EmptyCity_CallsServiceAndReturnsResult() {
        WeatherResponseDto mockResponse = new WeatherResponseDto();
        mockResponse.setCity("Unknown");
        mockResponse.setTemperature("0.0°C");
        mockResponse.setDate(LocalDate.now());

        when(weatherService.checkWeatherByCity("")).thenReturn(mockResponse);

        ResponseEntity<WeatherResponseDto> result = weatherAdapterController.checkWeather("");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Unknown", result.getBody().getCity());
        assertEquals("0.0°C", result.getBody().getTemperature());
    }

    @Test
    void checkWeather_NullResponseFromService_ReturnsOkWithNull() {
        when(weatherService.checkWeatherByCity("TestCity")).thenReturn(null);

        ResponseEntity<WeatherResponseDto> result = weatherAdapterController.checkWeather("TestCity");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
    }

    @Test
    void checkWeather_SpecialCharactersInCity_HandlesProperly() {
        String cityWithSpecialChars = "New York";
        WeatherResponseDto mockResponse = new WeatherResponseDto();
        mockResponse.setCity("New York");
        mockResponse.setTemperature("15.0°C");
        mockResponse.setDate(LocalDate.of(2023, 10, 15));

        when(weatherService.checkWeatherByCity(cityWithSpecialChars)).thenReturn(mockResponse);

        ResponseEntity<WeatherResponseDto> result = weatherAdapterController.checkWeather(cityWithSpecialChars);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("New York", result.getBody().getCity());
        assertEquals("15.0°C", result.getBody().getTemperature());
    }

    @Test
    void constructor_ValidService_InitializesCorrectly() {
        WeatherAdapterController controller = new WeatherAdapterController(weatherService);

        assertNotNull(controller);
        assertSame(weatherService, controller.weatherService);
    }
}