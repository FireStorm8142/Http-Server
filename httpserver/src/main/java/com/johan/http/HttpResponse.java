package com.johan.http;

import java.io.IOException;
import java.io.OutputStream;

import com.johan.httpserver.core.io.WebRootHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {

    private final OutputStream opStream;
    private final HttpRequest req;
    private final WebRootHandler handler;

    public HttpResponse(OutputStream opStream, HttpRequest req, WebRootHandler handler) throws HttpParsingException {
        this.opStream = opStream;
        this.req = req;
        this.handler = handler;
    }

    final static String CRLF = "\r\n"; //13, 10
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpResponse.class);

    public void writeResponse() throws HttpParsingException, IOException {
        if (req.getMethod() == HttpMethod.GET) {
            handleGet();
        }
        else if (req.getMethod() == HttpMethod.POST) {
            handlePost();
        }
    }

    //Method for sending the Error code and Message to Client
    public static void writeErrorResponse(HttpStatusCode code, OutputStream opStream) throws IOException {
        String body = code.MESSAGE;

        String response =
                "HTTP/1.1 " + code.STATUS_CODE + " " + code.MESSAGE + CRLF +
                        "Content-Length: " + body.length() + CRLF +
                        CRLF +
                        body;
        opStream.write(response.getBytes());
    }

    private void handleGet() throws HttpParsingException, IOException {
        //Try fetching Target that user has specified
        String path = req.getRequestTarget();
        byte[] fileBytes;
        String contentType;
        try {
            fileBytes = handler.GetFileByteArrayData(path);
            contentType = handler.GetFileType(path);
        }catch(IOException e){
            LOGGER.error("Error fetching File/Content-type", e);
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_404_NOT_FOUND);
        }catch (HttpParsingException e){
            throw e;
        }
        //Response to the client
        String response =
                "HTTP/1.1 200 OK" + CRLF + // Status Line : HTTP Version, Response_code, Response_msg
                        "Content-Length: " + fileBytes.length + CRLF + // Header
                        "Content-Type: "+contentType+ CRLF + //Add MIME Files later
                        CRLF;

        opStream.write(response.getBytes());
        opStream.write(fileBytes);
    }

    private void handlePost() {
        if ("/sign".equals(req.getRequestTarget())) {

        }
    }
}
