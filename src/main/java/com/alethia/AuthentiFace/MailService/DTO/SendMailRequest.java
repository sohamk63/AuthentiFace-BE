package com.alethia.AuthentiFace.MailService.DTO;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMailRequest {

    @NotBlank(message = "Subject cannot be blank")
    @Size(min = 1, max = 255, message = "Subject must be between 1 and 255 characters")
    private String subject;

    @NotBlank(message = "Body cannot be blank")
    @Size(min = 1, max = 10000, message = "Body must be between 1 and 10000 characters")
    private String body;

    @NotEmpty(message = "At least one recipient is required")
    private List<RecipientRequest> recipients;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientRequest {
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Recipient must be a valid email")
        private String email;

        @NotBlank(message = "Recipient type cannot be blank")
        private String type; // TO, CC, BCC
    }
}
