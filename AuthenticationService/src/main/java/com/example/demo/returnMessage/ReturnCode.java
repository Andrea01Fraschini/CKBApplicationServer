package com.example.demo.returnMessage;

public enum ReturnCode {
    NOT_FORMAT_REQUEST(400),
    NOT_WORK_HASHING(500),

    SUCCESS(200),

    ALREADY_EXISTS(207),

    FAILD(205);

    private final int defaultMessage;

    ReturnCode(int defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public int getDefaultMessage() {
        return defaultMessage;
    }
}
