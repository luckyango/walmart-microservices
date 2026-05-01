package com.example.aiservice.service;

import com.example.aiservice.dto.AiActionPlan;
import com.example.aiservice.dto.ChatRequest;
import com.example.aiservice.dto.ChatResponse;
import com.example.aiservice.dto.ItemDto;
import com.example.aiservice.dto.OrderDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${services.gateway-base-url}")
    private String gatewayBaseUrl;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.model}")
    private String openaiModel;

    public ChatResponse chat(ChatRequest request, String authorizationHeader) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return new ChatResponse("Please type a message so I can help.", "reply_only");
        }

        List<ItemDto> items = loadItems();
        List<OrderDto> orders = request.getUserId() != null && authorizationHeader != null && !authorizationHeader.isBlank()
                ? loadOrders(request.getUserId(), authorizationHeader)
                : List.of();

        AiActionPlan plan = planAction(request.getMessage(), items, orders);
        return executePlan(plan, request.getUserId(), authorizationHeader, items, orders);
    }

    private AiActionPlan planAction(String userMessage, List<ItemDto> items, List<OrderDto> orders) {
        if (openaiApiKey == null || openaiApiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is missing for ai-service");
        }

        String catalogSummary = items.stream()
                .map(item -> String.format(Locale.US, "%s | id=%s | upc=%s | price=%.2f | inventory=%d",
                        item.getName(), item.getId(), item.getUpc(), item.getPrice(), item.getInventory()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No items available.");

        String orderSummary = orders.isEmpty()
                ? "No orders available."
                : orders.stream()
                .map(order -> String.format(Locale.US, "orderId=%d | itemId=%s | qty=%d | status=%s | total=%.2f",
                        order.getId(), order.getItemId(), order.getQuantity(), order.getStatus(), order.getTotalPrice()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No orders available.");

        String prompt = """
                You are an assistant for an online shopping website.
                Decide the best next action based on the user request.

                Allowed actions:
                - list_items
                - list_orders
                - create_order
                - reply_only

                Rules:
                - If user asks to browse, search, or recommend products, use list_items.
                - If user asks about their orders, use list_orders.
                - If user asks to buy, order, or place an order, use create_order.
                - For create_order, fill itemQuery with the product name or keywords and quantity with a positive integer.
                - If request is ambiguous, use reply_only and ask a concise clarifying question.
                - Return valid JSON only with fields: action, itemQuery, quantity, reply.
                - Do not wrap the JSON in markdown.

                Catalog:
                %s

                Current orders:
                %s

                User message:
                %s
                """.formatted(catalogSummary, orderSummary, userMessage);

        Map<String, Object> payload = Map.of(
                "model", openaiModel,
                "input", prompt,
                "text", Map.of(
                        "format", Map.of("type", "json_object")
                )
        );

        try {
            JsonNode response = webClient.post()
                    .uri("https://api.openai.com/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String rawJson = extractOutputText(response);
            return objectMapper.readValue(rawJson, AiActionPlan.class);
        } catch (WebClientResponseException ex) {
            log.error("OpenAI request failed in ai-service. Status: {}, body: {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new IllegalStateException("OpenAI request failed: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            log.error("OpenAI request failed in ai-service", ex);
            throw new IllegalStateException("OpenAI request failed: " + safeMessage(ex), ex);
        }
    }

    private ChatResponse executePlan(AiActionPlan plan, Long userId, String authorizationHeader, List<ItemDto> items, List<OrderDto> orders) {
        if (plan == null || plan.getAction() == null) {
            return new ChatResponse("I couldn't understand that request yet. Please try again.", "reply_only");
        }

        return switch (plan.getAction()) {
            case "list_items" -> new ChatResponse(buildItemReply(plan.getItemQuery(), items), "list_items");
            case "list_orders" -> new ChatResponse(buildOrderReply(orders, authorizationHeader), "list_orders");
            case "create_order" -> createOrderReply(plan, userId, authorizationHeader, items);
            default -> new ChatResponse(plan.getReply(), "reply_only");
        };
    }

    private String buildItemReply(String itemQuery, List<ItemDto> items) {
        List<ItemDto> matched = filterItems(items, itemQuery);
        if (matched.isEmpty()) {
            return "I couldn't find a matching product right now. Try asking for duck, bunny, panda, dino, or kitty.";
        }

        StringBuilder reply = new StringBuilder("Here are the matching products:\n");
        matched.stream().limit(4).forEach(item ->
                reply.append("- ").append(item.getName())
                        .append(" | $").append(String.format(Locale.US, "%.2f", item.getPrice()))
                        .append(" | inventory ").append(item.getInventory())
                        .append('\n'));
        return reply.toString().trim();
    }

    private String buildOrderReply(List<OrderDto> orders, String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return "Please sign in first, and then I can check your orders.";
        }
        if (orders.isEmpty()) {
            return "You don't have any orders yet.";
        }

        StringBuilder reply = new StringBuilder("Here are your latest orders:\n");
        orders.stream().sorted(Comparator.comparing(OrderDto::getId).reversed()).limit(5).forEach(order ->
                reply.append("- Order #").append(order.getId())
                        .append(" | status ").append(order.getStatus())
                        .append(" | qty ").append(order.getQuantity())
                        .append(" | total $").append(String.format(Locale.US, "%.2f", order.getTotalPrice()))
                        .append('\n'));
        return reply.toString().trim();
    }

    private ChatResponse createOrderReply(AiActionPlan plan, Long userId, String authorizationHeader, List<ItemDto> items) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || userId == null) {
            return new ChatResponse("Please sign in first, and then I can place an order for you.", "create_order");
        }

        int quantity = plan.getQuantity() == null || plan.getQuantity() <= 0 ? 1 : plan.getQuantity();
        List<ItemDto> matched = filterItems(items, plan.getItemQuery());
        if (matched.isEmpty()) {
            return new ChatResponse("I couldn't find that product in the catalog. Please mention the product name again.", "create_order");
        }

        ItemDto chosen = matched.get(0);

        try {
            OrderDto created = webClient.post()
                    .uri(gatewayBaseUrl + "/api/orders?userId={userId}&itemId={itemId}&qty={qty}", userId, chosen.getId(), quantity)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .bodyToMono(OrderDto.class)
                    .block();

            return new ChatResponse(
                    "I placed the order for " + chosen.getName() + ". Order #" + created.getId() + " is now " + created.getStatus() + ".",
                    "create_order"
            );
        } catch (Exception ex) {
            return new ChatResponse("I tried to place the order, but it failed. The item may be sold out or temporarily unavailable.", "create_order");
        }
    }

    private List<ItemDto> loadItems() {
        ItemDto[] items = webClient.get()
                .uri(gatewayBaseUrl + "/api/items")
                .retrieve()
                .bodyToMono(ItemDto[].class)
                .block();
        return items == null ? List.of() : Arrays.asList(items);
    }

    private List<OrderDto> loadOrders(Long userId, String authorizationHeader) {
        try {
            OrderDto[] orders = webClient.get()
                    .uri(gatewayBaseUrl + "/api/orders/user/{userId}", userId)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .retrieve()
                    .bodyToMono(OrderDto[].class)
                    .block();
            return orders == null ? List.of() : Arrays.asList(orders);
        } catch (Exception ex) {
            return List.of();
        }
    }

    private List<ItemDto> filterItems(List<ItemDto> items, String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>(items);
        }

        String normalized = query.toLowerCase(Locale.US);
        return items.stream()
                .filter(item -> {
                    String name = item.getName() == null ? "" : item.getName().toLowerCase(Locale.US);
                    String upc = item.getUpc() == null ? "" : item.getUpc().toLowerCase(Locale.US);
                    return name.contains(normalized) || normalized.contains(name) || upc.contains(normalized);
                })
                .toList();
    }

    private String extractOutputText(JsonNode response) {
        JsonNode outputText = response.path("output_text");
        if (!outputText.isMissingNode() && !outputText.isNull() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }
        JsonNode textNode = response.path("output").get(0).path("content").get(0).path("text");
        if (textNode.isMissingNode() || textNode.isNull()) {
            throw new IllegalStateException("AI response text is missing");
        }
        return textNode.asText();
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message;
    }
}
