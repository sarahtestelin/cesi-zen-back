package com.cesi_zen_back.cesi_zen_back.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI cesizenOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("CESIZen API")
                .version("v1")
                .description("Documentation interactive de l'API CESIZen"));
  }
}
