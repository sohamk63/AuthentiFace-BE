package com.alethia.AuthentiFace.config;

/**
 * Central cache name constants to avoid magic strings across the codebase.
 */
public final class CacheNames {

    private CacheNames() {
    }

    public static final String FACE_EMBEDDINGS = "faceEmbeddings";
    public static final String FACE_ENROLLMENT_STATUS = "faceEnrollmentStatus";
    public static final String UNREAD_MAIL_COUNT = "unreadMailCount";
    public static final String USER_DETAILS = "userDetails";
}
