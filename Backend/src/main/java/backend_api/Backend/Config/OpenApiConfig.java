package backend_api.Backend.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .openapi("3.0.3")
                .info(new Info()
                        .title("Backend API - Sistema de Pagos y Facturación")
                        .description("API REST para el sistema de pagos y facturación. " +
                                "Incluye funcionalidades de autenticación JWT, gestión de pagos, " +
                                "facturación, reembolsos y reportes.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("dev@backend.com")
                                .url("https://github.com/backend-dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.backend.com")
                                .description("Servidor de Producción")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT para autenticación. " +
                                        "Incluye 'Bearer ' antes del token."))
                        .addSchemas("ErrorResponse", new ObjectSchema()
                                .addProperty("timestamp", new DateTimeSchema()
                                        .description("Timestamp del error"))
                                .addProperty("status", new NumberSchema()
                                        .description("Código de estado HTTP"))
                                .addProperty("error", new StringSchema()
                                        .description("Tipo de error"))
                                .addProperty("message", new StringSchema()
                                        .description("Mensaje de error"))
                                .addProperty("path", new StringSchema()
                                        .description("Ruta donde ocurrió el error")))
                        .addSchemas("ValidationError", new ObjectSchema()
                                .addProperty("field", new StringSchema()
                                        .description("Campo con error de validación"))
                                .addProperty("rejectedValue", new StringSchema()
                                        .description("Valor rechazado"))
                                .addProperty("message", new StringSchema()
                                        .description("Mensaje de error de validación")))
                        .addSchemas("PageResponse", new ObjectSchema()
                                .addProperty("content", new ArraySchema()
                                        .description("Lista de elementos"))
                                .addProperty("pageable", new ObjectSchema()
                                        .description("Información de paginación"))
                                .addProperty("totalElements", new NumberSchema()
                                        .description("Total de elementos"))
                                .addProperty("totalPages", new NumberSchema()
                                        .description("Total de páginas"))
                                .addProperty("size", new NumberSchema()
                                        .description("Tamaño de página"))
                                .addProperty("number", new NumberSchema()
                                        .description("Número de página actual"))
                                .addProperty("first", new BooleanSchema()
                                        .description("Es la primera página"))
                                .addProperty("last", new BooleanSchema()
                                        .description("Es la última página"))
                                .addProperty("numberOfElements", new NumberSchema()
                                        .description("Número de elementos en la página actual"))));
    }
}
