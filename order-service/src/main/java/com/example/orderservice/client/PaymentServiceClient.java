package com.example.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "payment-service-client", url = "${services.payment-service-url}")
public interface PaymentServiceClient {

    @PostMapping("/payments/order/{orderId}/refund")
    void refundPaymentByOrderId(@PathVariable("orderId") Long orderId,
                                @RequestHeader("Authorization") String authorizationHeader);
}
