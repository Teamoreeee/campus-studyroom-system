package com.campus.gateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpenApiAggregatorController {

    private final ObjectMapper objectMapper;

    private static final Map<String, String> SERVICE_URLS = Map.of(
            "auth", "http://localhost:8001",
            "user", "http://localhost:8002",
            "room", "http://localhost:8003",
            "reservation", "http://localhost:8004",
            "attendance", "http://localhost:8005",
            "ai", "http://localhost:8006"
    );

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> aggregate() {
        return Mono.fromCallable(this::doAggregate)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String doAggregate() {
        List<JsonNode> specs = new ArrayList<>();
        for (Map.Entry<String, String> entry : SERVICE_URLS.entrySet()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(entry.getValue() + "/v3/api-docs"))
                        .timeout(Duration.ofSeconds(3))
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .GET()
                        .build();
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                    specs.add(objectMapper.readTree(response.body()));
                } else {
                    log.warn("获取 {} 的 OpenAPI 文档失败，状态码：{}", entry.getKey(), response.statusCode());
                }
            } catch (Exception e) {
                log.warn("获取 {} 的 OpenAPI 文档失败：{}", entry.getKey(), e.getMessage());
            }
        }
        return merge(specs);
    }

    private String merge(List<JsonNode> specs) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("openapi", "3.0.1");

        ObjectNode info = root.putObject("info");
        info.put("title", "校园自习室预约系统 API");
        info.put("description", "网关聚合后的微服务 OpenAPI 文档");
        info.put("version", "1.0.0");

        ArrayNode servers = root.putArray("servers");
        ObjectNode gatewayServer = servers.addObject();
        gatewayServer.put("url", "http://localhost:8000");
        gatewayServer.put("description", "Gateway server url");

        ObjectNode paths = root.putObject("paths");
        ArrayNode tags = root.putArray("tags");
        Set<String> tagNames = new LinkedHashSet<>();
        ObjectNode components = root.putObject("components");
        ObjectNode schemas = components.putObject("schemas");

        for (JsonNode spec : specs) {
            if (spec == null || !spec.isObject()) {
                continue;
            }

            JsonNode specPaths = spec.get("paths");
            if (specPaths != null && specPaths.isObject()) {
                specPaths.fields().forEachRemaining(entry -> {
                    if (!paths.has(entry.getKey())) {
                        paths.set(entry.getKey(), entry.getValue());
                    }
                });
            }

            JsonNode specTags = spec.get("tags");
            if (specTags != null && specTags.isArray()) {
                specTags.forEach(tag -> {
                    JsonNode nameNode = tag.get("name");
                    if (nameNode != null && tagNames.add(nameNode.asText())) {
                        tags.add(tag);
                    }
                });
            }

            JsonNode specSchemas = spec.path("components").path("schemas");
            if (specSchemas.isObject()) {
                specSchemas.fields().forEachRemaining(entry -> {
                    if (!schemas.has(entry.getKey())) {
                        schemas.set(entry.getKey(), entry.getValue());
                    }
                });
            }
        }

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("聚合 OpenAPI 文档失败", e);
        }
    }
}
