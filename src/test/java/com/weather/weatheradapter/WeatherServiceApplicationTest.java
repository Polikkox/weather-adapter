package com.weather.weatheradapter;

import com.weather.weatheradapter.controller.WeatherAdapterController;
import com.weather.weatheradapter.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WeatherServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherAdapterController weatherAdapterController;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

    @Test
    void weatherServiceBeanExists() {
        assertNotNull(weatherService);
        assertTrue(applicationContext.containsBean("weatherService"));
    }

    @Test
    void weatherAdapterControllerBeanExists() {
        assertNotNull(weatherAdapterController);
        assertTrue(applicationContext.containsBean("weatherAdapterController"));
    }

    @Test
    void applicationStartsSuccessfully() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertTrue(beanNames.length > 0);
    }

    @Test
    void controllerHasWeatherServiceInjected() {
        assertNotNull(weatherAdapterController.weatherService);
        assertSame(weatherService, weatherAdapterController.weatherService);
    }
}