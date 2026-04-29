package com.stu.socialnetworkapi.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class GsonConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "gson")
    public Gson gson() {
        return new GsonBuilder()
                .serializeNulls()
                .create();
    }
}