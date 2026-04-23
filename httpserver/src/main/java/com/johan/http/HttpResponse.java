package com.johan.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.FileWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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

        //Names.html is a dynamic site, we have to replace the placeholder {{names}} with Data.txt names
        if ("/Names.html".equals(path)) {
            String html = Files.readString(
                    Paths.get("httpserver/WebRoot/Names.html")
            );
            List<String> names = Files.readAllLines(
                    Paths.get("httpserver/WebRoot/Data.txt")
            );
            StringBuilder list = new StringBuilder();
            for (String name : names) {
                list.append("<li>").append(name).append("</li>");
            }
            html = html.replace("{{names}}", list.toString());
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            response(bytes, "text/html");
            return;
        }

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
        response(fileBytes, contentType);
    }

    private void handlePost() throws HttpParsingException, IOException {
        if ("/sign".equals(req.getRequestTarget())) {
            String body = req.getBody();
            if (body.length()>100000) throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_413_PAYLOAD_TOO_LARGE);
            String[] pair = body.split("=", 2);
            try(FileWriter writer = new FileWriter("httpserver/WebRoot/Data.txt", true)) {
                String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                value = sanitiseHtml(value);
                writer.write(value + CRLF);
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String sanitiseHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void response(byte[] fileBytes, String contentType) throws IOException {
        //Response to the client
        String response =
                req.getBestCompatibleVersion().literal + " 200 OK" + CRLF + // Status Line : HTTP Version, Response_code, Response_msg
                        "Content-Length: " + fileBytes.length + CRLF + // Header
                        "Content-Type: "+contentType+ CRLF + //Add MIME Files later
                        CRLF;

        opStream.write(response.getBytes());
        opStream.write(fileBytes);
    }
}
