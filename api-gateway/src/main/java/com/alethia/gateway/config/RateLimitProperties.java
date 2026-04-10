package com.alethia.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "gateway.rate-limit")
@Data
public class RateLimitProperties {

    private int defaultReplenishRate = 20;
    private int defaultBurstCapacity = 40;
    private int loginReplenishRate = 5;
    private int loginBurstCapacity = 10;
}
