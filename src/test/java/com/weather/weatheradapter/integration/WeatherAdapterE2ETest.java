package com.weather.weatheradapter.integration;

import com.weather.weatheradapter.model.WeatherResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "weather.api.key=ddacdad06f6641a7834131114251509"
})
class WeatherAdapterE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void checkWeather_ValidCity_ReturnsWeatherData() {
        // Given
        String city = "Warsaw";
        String url = "http://localhost:" + port + "/api/v1/weather?city=" + city;

        // When
        ResponseEntity<WeatherResponseDto> response = restTemplate.getForEntity(url, WeatherResponseDto.class);

        // Then - With real API key, should work successfully
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCity());
        assertNotNull(response.getBody().getTemperature());
        assertNotNull(response.getBody().getDate());
        assertTrue(response.getBody().getTemperature().contains("°C"));

        // Log the actual response for verification
        System.out.println("City: " + response.getBody().getCity());
        System.out.println("Temperature: " + response.getBody().getTemperature());
        System.out.println("Date: " + response.getBody().getDate());
    }

    @Test
    void checkWeather_DifferentCity_ReturnsWeatherData() {
        // Given
        String city = "London";
        String url = "http://localhost:" + port + "/api/v1/weather?city=" + city;

        // When
        ResponseEntity<WeatherResponseDto> response = restTemplate.getForEntity(url, WeatherResponseDto.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("London", response.getBody().getCity());
        assertTrue(response.getBody().getTemperature().contains("°C"));
        assertNotNull(response.getBody().getDate());

        System.out.println("London Temperature: " + response.getBody().getTemperature());
    }

    @Test
    void checkWeather_EmptyCity_ReturnsError() {
        // Given
        String url = "http://localhost:" + port + "/api/v1/weather?city=";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then - Empty city should cause an error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void checkWeather_MissingCityParameter_ReturnsBadRequest() {
        // Given
        String url = "http://localhost:" + port + "/api/v1/weather";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void actuatorHealth_ReturnsUp() {
        // Given
        String url = "http://localhost:" + port + "/actuator/health";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"status\":\"UP\""));
    }

    @Test
    void swaggerUI_IsAccessible() {
        // Given
        String url = "http://localhost:" + port + "/swagger-ui.html";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void apiDocs_IsAccessible() {
        // Given
        String url = "http://localhost:" + port + "/api-docs";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("\"openapi\":") ||
                  response.getBody().contains("\"swagger\":"));
        // API docs should contain some reference to weather endpoint
        assertTrue(response.getBody().contains("weather") ||
                  response.getBody().contains("WeatherApp"));
    }

    @Test
    void applicationInfo_ReturnsCorrectPort() {
        // Given
        assertTrue(port > 0);
        System.out.println("Application started on port: " + port);

        // When & Then - Just verify the application started correctly
        String url = "http://localhost:" + port + "/actuator/info";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Info endpoint might not be enabled by default, but application should be running
        assertTrue(response.getStatusCode().is2xxSuccessful() ||
                  response.getStatusCode() == HttpStatus.NOT_FOUND);
    }
}