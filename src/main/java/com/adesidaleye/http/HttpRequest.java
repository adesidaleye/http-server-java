package com.adesidaleye.http;

import java.util.HashMap;
import java.util.Set;

public class HttpRequest extends HttpMessage{
    private HttpMethod method;
    private String requestTarget;
    private String originalHttpVersion; // literal from request
    private HttpVersion bestCompatibleHttpVersion;

    private HashMap<String, String> headers = new HashMap<>();

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

    // just the header names, for checking how many headers were parsed
    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    // lowercase the lookup key so for example "Host", "host", "HOST" all find the same entry
    public String getHeader(String headerName) {
        return headers.get(headerName.toLowerCase());
    }

    // called by HttpParser.processSingleHeaderField for every valid header line found
    public void addHeader(String headerName, String headerValue) {
        headers.put(headerName.toLowerCase(), headerValue);
    }
}