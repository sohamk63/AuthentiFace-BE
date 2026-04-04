package com.alethia.events;

public enum EventType {
    // Auth events
    USER_LOGIN_SUCCESS,
    USER_LOGIN_FAILED,
    PASSWORD_CHANGED,
    NEW_DEVICE_LOGIN,

    // Mail events
    MAIL_SENT,
    MAIL_RECEIVED,
    MAIL_DELIVERY_FAILED,
    CONFIDENTIAL_MAIL_RECEIVED,

    // Face events
    FACE_ENROLL_SUCCESS,
    FACE_ENROLL_FAILED,
    FACE_VERIFICATION_FAILED
}
