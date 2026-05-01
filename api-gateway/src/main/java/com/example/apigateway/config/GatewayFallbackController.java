package com.example.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class GatewayFallbackController {

    @RequestMapping("/fallback/account")
    public ResponseEntity<Map<String, Object>> accountFallback() {
        return fallback("Account service is temporarily unavailable");
    }

    @RequestMapping("/fallback/items")
    public ResponseEntity<Map<String, Object>> itemFallback() {
        return fallback("Item service is temporarily unavailable");
    }

    @RequestMapping("/fallback/orders")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        return fallback("Order service is temporarily unavailable");
    }

    @RequestMapping("/fallback/payments")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return fallback("Payment service is temporarily unavailable");
    }

    @RequestMapping("/fallback/ai")
    public ResponseEntity<Map<String, Object>> aiFallback() {
        return fallback("AI assistant service is temporarily unavailable");
    }

    private ResponseEntity<Map<String, Object>> fallback(String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                "message", message
        ));
    }
}
