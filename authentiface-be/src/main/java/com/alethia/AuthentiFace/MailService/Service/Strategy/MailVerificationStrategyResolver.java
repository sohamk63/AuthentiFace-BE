package com.alethia.AuthentiFace.MailService.Service.Strategy;

import org.springframework.stereotype.Component;

import com.alethia.AuthentiFace.MailService.Entity.Mail;

/**
 * DESIGN PATTERN: Factory (used alongside Strategy)
 * 
 * Selects the correct MailVerificationStrategy based on the mail's properties.
 * This keeps the selection logic in one place rather than scattered in service methods.
 * 
 * Usage in MailServiceImpl:
 *     MailVerificationStrategy strategy = strategyResolver.resolve(mail);
 *     strategy.verify(userId, faceFrames);
 */
@Component
public class MailVerificationStrategyResolver {

    private final NoVerificationStrategy noVerificationStrategy;
    private final FaceVerificationMailStrategy faceVerificationMailStrategy;

    public MailVerificationStrategyResolver(
            NoVerificationStrategy noVerificationStrategy,
            FaceVerificationMailStrategy faceVerificationMailStrategy) {
        this.noVerificationStrategy = noVerificationStrategy;
        this.faceVerificationMailStrategy = faceVerificationMailStrategy;
    }

    /**
     * Resolve which verification strategy to use based on the mail's confidentiality.
     *
     * @param mail the mail being accessed
     * @return the appropriate verification strategy
     */
    public MailVerificationStrategy resolve(Mail mail) {
        if (mail.getIsConfidential() != null && mail.getIsConfidential()) {
            return faceVerificationMailStrategy;
        }
        return noVerificationStrategy;
    }
}
