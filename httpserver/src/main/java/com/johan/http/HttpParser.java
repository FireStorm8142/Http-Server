package com.johan.http;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HttpParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    private static final int SP = 0x20; //32
    private static final int CR = 0x0D; //13
    private static final int LF = 0x0A; //10

    public static HttpRequest parseHttpReq(InputStream iptStream) throws HttpParsingException {
        InputStreamReader isr = new InputStreamReader(iptStream, StandardCharsets.US_ASCII);

        HttpRequest request = new HttpRequest();
        try {
            parseRequestLine(isr, request);
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            parseHeader(isr, request);
        }catch(IOException e){
            e.printStackTrace();
        }
        parseBody(isr, request);

        return request;
    }
    private static void parseRequestLine(InputStreamReader isr, HttpRequest req) throws IOException, HttpParsingException {
        boolean methodParsed = false;
        boolean reqTargetParsed = false;
        StringBuilder processDataBuffer = new StringBuilder();
        int _byte;
        while ((_byte=isr.read()) >=0){
            if(_byte==CR){
                _byte=isr.read();
                if(_byte==LF){
                    LOGGER.debug("Request Line VERSION to process : {}", processDataBuffer.toString());
                    if (!methodParsed || !reqTargetParsed) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
                    }
                    try {
                        req.setHttpVersion(processDataBuffer.toString());
                    }catch (BadHttpVersionException e) {
                        throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
                    }
                    return;
                }else{
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
                }
            }
            if(_byte==SP){
                if (!methodParsed) {
                    LOGGER.debug("Request Line METHOD to process : {}", processDataBuffer.toString());
                    req.setMethod(processDataBuffer.toString());
                    methodParsed = true;
                }else if (!reqTargetParsed) {
                    LOGGER.debug("Request Line REQ TARGET to process : {}", processDataBuffer.toString());
                    req.setRequestTarget(processDataBuffer.toString());
                    reqTargetParsed = true;
                }
                else{
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_401_METHOD_NOT_ALLOWED);
                }
                processDataBuffer.delete(0, processDataBuffer.length());
            }else{
                processDataBuffer.append((char)_byte);
                if(!methodParsed){
                    if(processDataBuffer.length()>HttpMethod.MAX_LENGTH){
                        throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_501_NOT_IMPLEMENTED);
                    }
                }
            }
        }
    }

    private static void parseHeader(InputStreamReader isr, HttpRequest req) throws HttpParsingException, IOException {
        StringBuilder processDataBuffer = new StringBuilder();
        boolean headerParsed = false;
        int _byte;
        while ((_byte=isr.read()) >=0){
            if(_byte==CR){
                _byte=isr.read();
                if(_byte==LF) {
                        String line = processDataBuffer.toString().trim();
                        if (line.isEmpty()) {
                            if(!headerParsed){
                                throw new  HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
                            }
                            return;
                        }
                        processingHeaderField(processDataBuffer, req);
                        headerParsed = true;
                        processDataBuffer.delete(0, processDataBuffer.length());
                } else{
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
                }
            } else{
                processDataBuffer.append((char)_byte);
            }
        }
    }

    private static void processingHeaderField(StringBuilder processDataBuffer, HttpRequest req) throws HttpParsingException {
        String rawHeaderField = processDataBuffer.toString();
        if (rawHeaderField.length() > 8192) {
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_414_URI_TOO_LONG);
        }
        int colonIndex = rawHeaderField.indexOf(':');
        if(colonIndex == -1){
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
        }
        String fieldName = rawHeaderField.substring(0, colonIndex).trim();
        String fieldValue = rawHeaderField.substring(colonIndex+1).trim();
        if(fieldName.isEmpty()){
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
        }
        req.addHeader(fieldName, fieldValue);
    }

    private static void parseBody(InputStreamReader isr, HttpRequest req) {

    }
}
