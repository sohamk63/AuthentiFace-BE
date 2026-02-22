package com.alethia.AuthentiFace.MailService.Controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alethia.AuthentiFace.AuthService.Service.interfaces.UserPrincipal;
import com.alethia.AuthentiFace.MailService.DTO.MailResponse;
import com.alethia.AuthentiFace.MailService.DTO.PageResponse;
import com.alethia.AuthentiFace.MailService.DTO.SendMailRequest;
import com.alethia.AuthentiFace.MailService.DTO.SendMailResponse;
import com.alethia.AuthentiFace.MailService.DTO.SentMailResponse;
import com.alethia.AuthentiFace.MailService.Service.MailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mail")
public class MailController {

    private final MailService mailService;

    @Autowired
    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Mail Service is up and running!");
    }

    /**
     * Send a new mail to one or multiple recipients
     *
     * @param request Mail details and recipients
     * @return Response with mail ID confirmation
     */
    @PostMapping("/send")
    public ResponseEntity<SendMailResponse> sendMail(
            @Valid @RequestBody SendMailRequest request
    ) {
        UUID senderId = getAuthenticatedUserId();
        SendMailResponse response = mailService.sendMail(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get inbox for the authenticated user
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of mails
     */
    @GetMapping("/inbox")
    public ResponseEntity<PageResponse<MailResponse>> getInbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID recipientId = getAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<MailResponse> response = mailService.getInbox(recipientId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get sent mails for the authenticated user
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of sent mails
     */
    @GetMapping("/sent")
    public ResponseEntity<PageResponse<SentMailResponse>> getSentMails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        UUID senderId = getAuthenticatedUserId();
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<SentMailResponse> response = mailService.getSentMails(senderId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a mail as read
     *
     * @param mailId UUID of the mail to mark as read
     * @return 204 No Content on success
     */
    @PutMapping("/{mailId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID mailId
    ) {
        UUID recipientId = getAuthenticatedUserId();
        mailService.markAsRead(mailId, recipientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get count of unread mails for the authenticated user
     *
     * @return Count of unread mails
     */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        UUID recipientId = getAuthenticatedUserId();
        long unreadCount = mailService.getUnreadCount(recipientId);
        return ResponseEntity.ok(new UnreadCountResponse(unreadCount));
    }

    /**
     * Extract authenticated user ID from SecurityContext
     *
     * @return UUID of authenticated user
     */
    private UUID getAuthenticatedUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return principal.getUserId();
    }

    /**
     * Response class for unread count
     */
    public static class UnreadCountResponse {
        private long unreadCount;

        public UnreadCountResponse(long unreadCount) {
            this.unreadCount = unreadCount;
        }

        public long getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(long unreadCount) {
            this.unreadCount = unreadCount;
        }
    }
}
