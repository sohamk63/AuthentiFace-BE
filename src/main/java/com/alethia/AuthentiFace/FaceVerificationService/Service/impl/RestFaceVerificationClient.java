package com.alethia.AuthentiFace.FaceVerificationService.Service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import com.alethia.AuthentiFace.FaceVerificationService.Config.FaceProperties;
import com.alethia.AuthentiFace.FaceVerificationService.Exception.FaceVerificationFailedException;
import com.alethia.AuthentiFace.FaceVerificationService.Service.interfaces.FaceVerificationClient;

@Service
public class RestFaceVerificationClient implements FaceVerificationClient {

    private final WebClient.Builder webClientBuilder;
    private final FaceProperties faceProperties;

    public RestFaceVerificationClient(WebClient.Builder webClientBuilder, FaceProperties faceProperties) {
        this.webClientBuilder = webClientBuilder;
        this.faceProperties = faceProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String generateEmbedding(List<MultipartFile> frames) {

        try {
            String baseUrl = faceProperties.getVerification().getBaseUrl();
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            for (MultipartFile frame : frames) {
                builder.part(
                        "frames",
                        new ByteArrayResource(frame.getBytes()) {
                            @Override
                            public String getFilename() {
                                return frame.getOriginalFilename();
                            }
                        }
                ).contentType(MediaType.APPLICATION_OCTET_STREAM);
            }

            Map<String, Object> response = webClient.post()
                    .uri("/generate-embedding")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("embedding")) {
                return (String) response.get("embedding");
            }

            throw new FaceVerificationFailedException(
                    "No embedding returned from face service");

        } catch (Exception e) {
            throw new FaceVerificationFailedException(
                    "Failed to generate embedding: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verify(String storedEmbedding, List<MultipartFile> frames) {
        try {
            String baseUrl = faceProperties.getVerification().getBaseUrl();
            WebClient webClient = webClientBuilder.baseUrl(baseUrl).build();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("stored_embedding", storedEmbedding);

            int frameIndex = 0;
            for (MultipartFile frame : frames) {
                builder.part("frames", 
                    new ByteArrayResource(frame.getBytes()),
                    MediaType.APPLICATION_OCTET_STREAM)
                    .filename(frame.getOriginalFilename());
                frameIndex++;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClient.post()
                .uri("/verify")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("verified")) {
                Object verified = response.get("verified");
                if (verified instanceof Boolean) {
                    return (Boolean) verified;
                }
                // Handle case where it might be a string "true"/"false"
                return Boolean.parseBoolean(verified.toString());
            }

            throw new FaceVerificationFailedException(
                "Failed to verify face: no verification result in response");
        } catch (FaceVerificationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new FaceVerificationFailedException(
                "Failed to verify face: " + e.getMessage(), e);
        }
    }
}
