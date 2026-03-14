package com.johan.http;

public class HttpRequest extends HttpMessage {

    private HttpMethod method;
    private String requestTarget;
    private String originalHttpVersion;
    private HttpVersion bestCompatibleVersion;

    HttpRequest() {
    }

    public HttpMethod getMethod() {
        return method;
    }

    void setMethod(String methodName) throws HttpParsingException {
        for (HttpMethod method: HttpMethod.values()) {
            if(methodName.equals(method.name())){
                this.method = method;
                return;
            }
        }
        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
    }

    public void setRequestTarget(String requestTarget) {
        if (requestTarget == null || requestTarget.isEmpty()){

        }
        this.requestTarget = requestTarget;
    }

    public void setHttpVersion(String originalHttpVersion) throws BadHttpVersionException, HttpParsingException {
        this.originalHttpVersion = originalHttpVersion;
        this.bestCompatibleVersion = HttpVersion.getBestCompatibleVersion(originalHttpVersion);
        if(this.bestCompatibleVersion == null){
            throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_505_HTTP_VERSION_NOT_SUPPORTED);
        }
    }
}
