package com.example.isogateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ISO 8583 Payment Gateway API")
                        .version("1.0.0")
                        .description("High-performance payment gateway for ISO 8583 protocol communication with banking systems")
                        .contact(new Contact()
                                .name("Thiago Grigoleti")
                                .url("https://github.com/ThiagoGrigoleti"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
