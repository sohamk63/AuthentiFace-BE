package com.alethia.NotificationService.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageResponse {
    private List<NotificationResponse> content;
    private int pageNumber;
    private int totalPages;
    private long totalElements;
    private boolean isLast;
}
