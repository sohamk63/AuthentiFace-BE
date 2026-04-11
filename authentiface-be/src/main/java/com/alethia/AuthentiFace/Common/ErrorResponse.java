package com.alethia.AuthentiFace.Common;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DESIGN PATTERN: Builder (via Lombok @Builder)
 * 
 * Instead of creating an ErrorResponse with multiple setter calls:
 *     ErrorResponse err = new ErrorResponse();
 *     err.setStatus(404);
 *     err.setError("Not Found");
 *     ...
 * 
 * We can now build it fluently:
 *     ErrorResponse.builder()
 *         .status(404)
 *         .error("Not Found")
 *         .message("User not found")
 *         .build();
 * 
 * The timestamp defaults to now() via the @Builder.Default annotation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

