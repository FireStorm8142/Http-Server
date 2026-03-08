package com.johan.http;

public class HttpStatusCode {
    /* ---Client Errors --- */
    CLIENT_ERROR_400_BAD_REQ(400, "Bad Request");
    CLIENT_ERROR_401_METHOD_NOT_ALLOWED(401, "Method not allowed");
    CLIENT_ERROR_400_BAD_REQ(414, "URI too long");
    /* ---Server Errors --- */
    SERVER_ERROR_500_INTERNAL_SERVER_ERROR(500, "Internal server error");
    SERVER_ERROR_500_NOT_IMPLEMENTED(501, "Not implemented");

    public final int STATUS_CODE;
    public final String MESSAGE;

    HttpStatusCode(int STATUS_CODE, String MESSAGE) {
        this.STATUS_CODE=STATUS_CODE;
        this.MESSAGE = MESSAGE;
    }
}
