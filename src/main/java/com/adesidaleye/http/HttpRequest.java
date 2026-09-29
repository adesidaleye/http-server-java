package com.adesidaleye.http;

public class HttpRequest extends HttpMessage{
    private HttpMethod method;
    private String requestTarget;
    private String originalHttpVersion; // literal from request
    private HttpVersion bestCompatibleHttpVersion;

    // package-private
    HttpRequest() {}

    HttpMethod getMethod() {
        return method;
    }

    void setMethod(String methodName) throws HttpParsingException {
        /* unknown method throws HttpParsingException if
        said method is not in HttpMethod (Not Implemented) */
        for (HttpMethod method : HttpMethod.values()) {
            if (methodName.equals(method.name())) {
                this.method = method;
                return;
            }
        }

        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
    }

    public String getRequestTarget() {
        return requestTarget;
    }

     void setRequestTarget(String requestTarget) throws HttpParsingException {
         // catches the empty-request-line case
         if (requestTarget == null || requestTarget.isEmpty()) {
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQUEST);
        }

        this.requestTarget = requestTarget;
    }

    public HttpVersion getBestCompatibleHttpVersion() {
        return bestCompatibleHttpVersion;
    }

    public String getOriginalHttpVersion() {
        return originalHttpVersion;
    }

    // takes the raw version string from client, resolves it to a supported HttpVersion
    void setHttpVersion(String originalHttpVersion) throws BadHttpVersionException, HttpParsingException {
        // keep the exact string the client sent, separate from the resolved/fallback version
        this.originalHttpVersion = originalHttpVersion;
        this.bestCompatibleHttpVersion = HttpVersion.getBestCompatibleVersion(originalHttpVersion);

        // getBestCompatibleVersion returns null when no major version matches at all, example "HTTP/2.0"
        if (this.bestCompatibleHttpVersion == null) {
            throw new HttpParsingException(HttpStatusCode.SERVER_505_HTTP_VERSION_NOT_SUPPORTED);
        }
    }
}
