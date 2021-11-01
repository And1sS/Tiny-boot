package org.and1ss.java_lab_1.web;

public enum ResponseStatus {

    OK(200),
    BAD_REQUEST(400),
    INTERNAL_SERVER_ERROR(500);

    private int statusCode;

    ResponseStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
