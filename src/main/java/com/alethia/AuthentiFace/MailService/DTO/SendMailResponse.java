package com.alethia.AuthentiFace.MailService.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMailResponse {

    private String message;

    private String mailId;

    public SendMailResponse(String message) {
        this.message = message;
    }
}
