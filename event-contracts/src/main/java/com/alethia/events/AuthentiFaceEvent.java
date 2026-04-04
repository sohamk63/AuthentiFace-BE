package com.alethia.events;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthentiFaceEvent {

    private String eventId;
    private EventType eventType;
    private UUID userId;
    private String sourceService;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata;

    public static AuthentiFaceEvent create(EventType type, UUID userId, String source, Map<String, Object> metadata) {
        return AuthentiFaceEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(type)
                .userId(userId)
                .sourceService(source)
                .timestamp(LocalDateTime.now())
                .metadata(metadata)
                .build();
    }
}
