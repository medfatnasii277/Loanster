package com.pm.apigateway.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class OfficerValidationGatewayFilterFactory extends AbstractGatewayFilterFactory {

    private final WebClient webClient;

    public OfficerValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                                 @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            System.out.println("Officer Validation Filter - Token received: " + (token != null ? "Present" : "Missing"));
            
            if (token == null || !token.startsWith("Bearer ")) {
                System.out.println("Officer Validation Filter - Invalid token format");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            System.out.println("Officer Validation Filter - Validating OFFICER role for admin endpoint");
            
            return webClient.get()
                    .uri("/validate/officer")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity()
                    .then(chain.filter(exchange))
                    .onErrorResume(WebClientResponseException.class, e -> {
                        // Log the error for debugging
                        System.err.println("Officer auth validation error: " + e.getStatusCode() + " - " + e.getMessage());
                        if (e.getStatusCode().value() == 401 || e.getStatusCode().value() == 403) {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        } else {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        }
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(Exception.class, e -> {
                        // Handle any other exceptions (network issues, etc.)
                        System.err.println("Officer auth service connection error: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
