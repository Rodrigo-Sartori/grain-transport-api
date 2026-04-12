package br.com.test.graintransport.grain_transport_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String API_KEY_SCHEME = "X-Api-Key";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Grain Transport API")
                        .description("API de transporte de grãos com estabilização de leituras de balança, " +
                                     "controle de transações e cálculo de custo com margem dinâmica.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Grain Transport Team")
                                .email("dev@graintransport.com")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .name(API_KEY_SCHEME)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Chave de autenticação da balança. Obrigatória apenas para POST /api/ingest")));
    }
}
