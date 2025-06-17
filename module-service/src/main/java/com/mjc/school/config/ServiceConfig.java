package com.mjc.school.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-service.properties")
public class ServiceConfig {
}
