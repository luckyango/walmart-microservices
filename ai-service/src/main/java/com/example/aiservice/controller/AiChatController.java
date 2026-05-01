package com.example.aiservice.controller;

import com.example.aiservice.dto.ChatRequest;
import com.example.aiservice.dto.ChatResponse;
import com.example.aiservice.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request,
                                             @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        try {
            return ResponseEntity.ok(aiChatService.chat(request, authorizationHeader));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ChatResponse("OpenAI call failed: " + ex.getMessage(), "error"));
        }
    }
}
