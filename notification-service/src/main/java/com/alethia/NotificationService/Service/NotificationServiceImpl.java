package com.alethia.NotificationService.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alethia.events.AuthentiFaceEvent;
import com.alethia.NotificationService.DTO.NotificationPageResponse;
import com.alethia.NotificationService.DTO.NotificationResponse;
import com.alethia.NotificationService.Entity.Notification;
import com.alethia.NotificationService.Entity.NotificationStatus;
import com.alethia.NotificationService.Factory.EventHandlerFactory;
import com.alethia.NotificationService.Repository.NotificationRepository;
import com.alethia.NotificationService.Strategy.NotificationChannel;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final EventHandlerFactory eventHandlerFactory;
    private final List<NotificationChannel> channels;
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(
            EventHandlerFactory eventHandlerFactory,
            List<NotificationChannel> channels,
            NotificationRepository notificationRepository) {
        this.eventHandlerFactory = eventHandlerFactory;
        this.channels = channels;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void processEvent(AuthentiFaceEvent event) {
        try {
            var handler = eventHandlerFactory.getHandler(event);
            Notification notification = handler.handle(event);

            for (NotificationChannel channel : channels) {
                if (channel.supports(notification)) {
                    channel.send(notification);
                    log.debug("Notification dispatched via channel: {}", channel.getChannelName());
                }
            }
        } catch (Exception ex) {
            log.error("Failed to process event {}: {}", event.getEventType(), ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<NotificationResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return NotificationPageResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .build();
    }

    @Override
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(UUID userId) {
        Page<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged());

        unread.getContent().stream()
                .filter(n -> n.getStatus() == NotificationStatus.UNREAD)
                .forEach(n -> {
                    n.setStatus(NotificationStatus.READ);
                    notificationRepository.save(n);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType().name())
                .status(n.getStatus().name())
                .sourceService(n.getSourceService())
                .createdAt(n.getCreatedAt())
                .metadata(n.getMetadata())
                .build();
    }
}
