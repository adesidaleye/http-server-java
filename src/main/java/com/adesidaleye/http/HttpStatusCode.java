package com.adesidaleye.http;

// fixed set of http status codes and messages
public enum HttpStatusCode {
    // Client Errors (4xx)
    CLIENT_ERROR_400_BAD_REQUEST(400, "Bad Request"),
    CLIENT_ERROR_405_METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CLIENT_ERROR_414_URI_TOO_LONG(414, "URI Too Long"),
    CLIENT_ERROR_404_NOT_FOUND(404, "Not Found"),

    // Server Errors (5xx)
    SERVER_ERROR_500_INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVER_ERROR_501_NOT_IMPLEMENTED(501, "Not Implemented"),
    SERVER_505_HTTP_VERSION_NOT_SUPPORTED(505, "Version Not Supported");

    // defined field for listed enums
    public final int STATUS_CODE;
    public final String MESSAGE;

    HttpStatusCode(int STATUS_CODE, String MESSAGE) {
        this.STATUS_CODE = STATUS_CODE;
        this.MESSAGE = MESSAGE;
    }
}
