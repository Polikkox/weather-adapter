package com.weather.weatheradapter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.weatheradapter.model.WeatherResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonNode rootNode;

    @Mock
    private JsonNode locationNode;

    @Mock
    private JsonNode currentNode;

    @Mock
    private JsonNode nameNode;

    @Mock
    private JsonNode tempNode;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService();
        ReflectionTestUtils.setField(weatherService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(weatherService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(weatherService, "weatherApiKey", "test-api-key");
        ReflectionTestUtils.setField(weatherService, "weatherApiBaseUrl", "http://api.weatherapi.com");
    }

    @Test
    void checkWeatherByCity_ValidCity_ReturnsWeatherData() throws Exception {
        String city = "Warsaw";
        String expectedUrl = "http://api.weatherapi.com/v1/current.json?key=test-api-key&q=Warsaw&aqi=no";
        String apiResponse = "{\"location\":{\"name\":\"Warsaw\"},\"current\":{\"temp_c\":25.5}}";

        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(apiResponse);
        when(objectMapper.readTree(apiResponse)).thenReturn(rootNode);
        when(rootNode.get("location")).thenReturn(locationNode);
        when(locationNode.get("name")).thenReturn(nameNode);
        when(nameNode.asText()).thenReturn("Warsaw");
        when(rootNode.get("current")).thenReturn(currentNode);
        when(currentNode.get("temp_c")).thenReturn(tempNode);
        when(tempNode.asText()).thenReturn("25.5");

        WeatherResponseDto result = weatherService.checkWeatherByCity(city);

        assertNotNull(result);
        assertEquals("Warsaw", result.getCity());
        assertEquals("25.5°C", result.getTemperature());
        assertEquals(LocalDate.now(), result.getDate());

        verify(restTemplate).getForObject(expectedUrl, String.class);
        verify(objectMapper).readTree(apiResponse);
    }

    @Test
    void checkWeatherByCity_RestTemplateThrowsException_ThrowsRuntimeException() {
        String city = "InvalidCity";
        String expectedUrl = "http://api.weatherapi.com/v1/current.json?key=test-api-key&q=InvalidCity&aqi=no";

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenThrow(new RuntimeException("Network error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> weatherService.checkWeatherByCity(city));

        assertEquals("Failed to fetch weather data for city: InvalidCity", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Network error", exception.getCause().getMessage());
    }

    @Test
    void checkWeatherByCity_JsonParsingError_ThrowsRuntimeException() throws Exception {
        String city = "London";
        String expectedUrl = "http://api.weatherapi.com/v1/current.json?key=test-api-key&q=London&aqi=no";
        String invalidJsonResponse = "invalid json";

        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(invalidJsonResponse);
        when(objectMapper.readTree(invalidJsonResponse))
                .thenThrow(new RuntimeException("JSON parsing error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> weatherService.checkWeatherByCity(city));

        assertEquals("Failed to fetch weather data for city: London", exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("JSON parsing error", exception.getCause().getMessage());
    }

    @Test
    void constructor_InitializesComponents_Correctly() {
        WeatherService service = new WeatherService();

        assertNotNull(ReflectionTestUtils.getField(service, "restTemplate"));
        assertNotNull(ReflectionTestUtils.getField(service, "objectMapper"));
    }

    @Test
    void checkWeatherByCity_EmptyCity_StillProcesses() throws Exception {
        String city = "";
        String expectedUrl = "http://api.weatherapi.com/v1/current.json?key=test-api-key&q=&aqi=no";
        String apiResponse = "{\"location\":{\"name\":\"Unknown\"},\"current\":{\"temp_c\":0.0}}";

        when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(apiResponse);
        when(objectMapper.readTree(apiResponse)).thenReturn(rootNode);
        when(rootNode.get("location")).thenReturn(locationNode);
        when(locationNode.get("name")).thenReturn(nameNode);
        when(nameNode.asText()).thenReturn("Unknown");
        when(rootNode.get("current")).thenReturn(currentNode);
        when(currentNode.get("temp_c")).thenReturn(tempNode);
        when(tempNode.asText()).thenReturn("0.0");

        WeatherResponseDto result = weatherService.checkWeatherByCity(city);

        assertNotNull(result);
        assertEquals("Unknown", result.getCity());
        assertEquals("0.0°C", result.getTemperature());
    }
}