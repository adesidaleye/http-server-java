package com.adesidaleye.http;

// extends Exception (checked), not RuntimeException, so callers are forced to handle a bad request
public class HttpParsingException extends Exception {
    private HttpStatusCode errorCode;

    public HttpParsingException(HttpStatusCode errorCode) {
        // reads MESSAGE off the object that was passed in
        super(errorCode.MESSAGE);
        this.errorCode = errorCode;
    }

    public HttpStatusCode getErrorCode() {
        return errorCode;
    }
}
