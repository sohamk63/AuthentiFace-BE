package com.alethia.AuthentiFace.FaceVerificationService.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "face")
@Data
public class FaceProperties {

    private StorageProperties storage;
    private VerificationProperties verification;

    @Data
    public static class StorageProperties {
        private LocalProperties local;

        @Data
        public static class LocalProperties {
            private String path;
        }
    }

    @Data
    public static class VerificationProperties {
        private String baseUrl;
    }
}
