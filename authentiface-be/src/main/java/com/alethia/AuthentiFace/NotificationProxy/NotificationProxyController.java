package com.alethia.AuthentiFace.NotificationProxy;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/notifications")
public class NotificationProxyController {

    private final WebClient webClient;

    public NotificationProxyController(@Value("${notification.service.url}") String notificationServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }

    @GetMapping
    public ResponseEntity<JsonNode> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UUID userId = getCurrentUserId();
        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/notifications")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("X-Internal-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<JsonNode> getUnreadCount() {
        UUID userId = getCurrentUserId();
        JsonNode response = webClient.get()
                .uri("/api/notifications/unread-count")
                .header("X-Internal-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        UUID userId = getCurrentUserId();
        webClient.put()
                .uri("/api/notifications/{notificationId}/read", notificationId)
                .header("X-Internal-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        UUID userId = getCurrentUserId();
        webClient.put()
                .uri("/api/notifications/read-all")
                .header("X-Internal-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        return ResponseEntity.ok().build();
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUser().getUserId();
    }
}
