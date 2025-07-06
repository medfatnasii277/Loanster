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
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory {

    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder,
                                             @Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String token =
                    exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            System.out.println("JWT Validation Filter - Token received: " + (token != null ? "Present" : "Missing"));
            
            if(token == null || !token.startsWith("Bearer ")) {
                System.out.println("JWT Validation Filter - Invalid token format");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            System.out.println("JWT Validation Filter - Calling auth service at: " + webClient + "/validate/borrower");
            
            return webClient.get()
                    .uri("/validate/borrower")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity()
                    .then(chain.filter(exchange))
                    .onErrorResume(WebClientResponseException.class, e -> {
                        // Log the error for debugging
                        System.err.println("Auth service error: " + e.getStatusCode() + " - " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    })
                    .onErrorResume(Exception.class, e -> {
                        // Handle any other exceptions (network issues, etc.)
                        System.err.println("Auth service connection error: " + e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }


}
