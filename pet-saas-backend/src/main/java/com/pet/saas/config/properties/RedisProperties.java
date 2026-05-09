package com.pet.saas.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedisProperties {

    private String host = "localhost";

    private int port = 6379;

    private int database = 0;

    private String password;
}
