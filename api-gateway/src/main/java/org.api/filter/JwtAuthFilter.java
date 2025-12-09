package org.api.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GatewayFilter {

    @Value("${jwt.secret}")
    private String secret;

    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (request.getURI().getPath().contains("/auth")) {
            return chain.filter(exchange);
        }

        // Check Authorization header
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = validateToken(token);
            Long tenantId = claims.get("tenantId", Long.class);

            // Forward tenantId to downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-Tenant-ID", String.valueOf(tenantId))
                    .header("X-User-Email", claims.getSubject())
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception ex) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

}
