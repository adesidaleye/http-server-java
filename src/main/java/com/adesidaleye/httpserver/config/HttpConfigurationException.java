package com.adesidaleye.httpserver.config;

public class HttpConfigurationException extends RuntimeException{
    public HttpConfigurationException() {
    }

    public HttpConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpConfigurationException(Throwable cause) {
        super(cause);
    }

    public HttpConfigurationException(String message) {
        super(message);
    }
}
