package com.campus.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class GatewayConfig {

    @Value("${gateway.direct:false}")
    private boolean directMode;

    @Value("${jwt.secret:campus-studyroom-jwt-secret-key-2026}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        if (directMode) {
            return directRoutes(builder);
        }
        return nacosRoutes(builder);
    }

    /**
     * 全局跨域配置：允许前端开发服务器（localhost:3000）及其他来源访问网关。
     * 浏览器预检请求 OPTIONS 必须被网关直接响应，否则会出现“被浏览器拒绝访问”。
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 生产环境建议改为具体域名，如 http://localhost:3000
        config.setAllowedOriginPatterns(Collections.singletonList("*"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    private RouteLocator directRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // 业务接口：保留 /api 前缀转发到各微服务（与后端 Controller 的 @RequestMapping("/api/xxx") 保持一致）
                .route("auth_route", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("http://localhost:8001"))
                .route("user_route", r -> r.path("/api/user/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("http://localhost:8002"))
                .route("room_route", r -> r.path("/api/room/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("http://localhost:8003"))
                .route("reservation_route", r -> r.path("/api/reservation/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("http://localhost:8004"))
                .route("attendance_route", r -> r.path("/api/attendance/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("http://localhost:8005"))
                .route("ai_route", r -> r.path("/api/ai/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("http://localhost:8006"))
                // 健康检查
                .route("health_route", r -> r.path("/actuator/health/**")
                        .uri("http://localhost:8001"))
                .build();
    }

    private RouteLocator nacosRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_route", r -> r.path("/api/auth/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("lb://campus-auth"))
                .route("user_route", r -> r.path("/api/user/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("lb://campus-user"))
                .route("room_route", r -> r.path("/api/room/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("lb://campus-room"))
                .route("reservation_route", r -> r.path("/api/reservation/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("lb://campus-reservation"))
                .route("attendance_route", r -> r.path("/api/attendance/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("lb://campus-attendance"))
                .route("ai_route", r -> r.path("/api/ai/**")
                        .filters(f -> f.filter(this::jwtAuthenticationFilter))
                        .uri("lb://campus-ai"))
                .route("health_route", r -> r.path("/actuator/health/**")
                        .uri("lb://campus-auth"))
                .build();
    }

    private Mono<Void> jwtAuthenticationFilter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 登录、注册、健康检查、Swagger 文档等接口放行
        if (path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/actuator") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/webjars") ||
            path.contains("/swagger-ui") ||
            path.contains("/v3/api-docs")) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return unauthorized(exchange.getResponse(), "未授权访问");
        }

        // 验证 token 是否有效（含过期检查）
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token.substring(7))
                    .getBody();
            // 可选：将 userId 放入 exchange attribute，供下游使用
            exchange.getAttributes().put("userId", claims.get("userId", Long.class));
        } catch (Exception e) {
            return unauthorized(exchange.getResponse(), "token 无效或已过期");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":401,\"message\":\"" + message + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
