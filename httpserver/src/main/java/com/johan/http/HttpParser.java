package com.johan.http;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    private static final int SP = 0x20; //32
    private static final int CR = 0x0D; //13
    private static final int LF = 0x0A; //10

    public static HttpRequest parseHttpReq(InputStream iptStream) throws HttpParsingException, IOException {
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
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
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
        boolean crlfFound = false;
        int _byte;
        while ((_byte=isr.read()) >=0){
            if(_byte==CR){
                _byte=isr.read();
                if(_byte==LF) {
                    if (!crlfFound) {
                        crlfFound = true;
                        processingHeaderField(processDataBuffer, req);
                        processDataBuffer.delete(0, processDataBuffer.length());
                    }
                    else{
                        return;
                    }
                } else{
                    throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
                }
            } else{
                crlfFound = false;
            }
        }
    }

    private static void processingHeaderField(StringBuilder processDataBuffer, HttpRequest req) throws HttpParsingException {
        String rawHeaderField = processDataBuffer.toString();
        Pattern pattern = Pattern.compile("^(?<fieldName>[!#$%&’*+\\-./^_‘|˜\\dA-Za-z]+):\\s?(?<fieldValue>[!#$%&’*+\\-./^_‘|˜(),:;<=>?@[\\\\]{}\" \\dA-Za-z]+)\\s?$");

        Matcher matcher = pattern.matcher(rawHeaderField);
        if(matcher.matches()){
            String fieldName = matcher.group("fieldName");
            String fieldValue =matcher.group("fieldValue");
            req.addHeader(fieldName, fieldValue);
        }else{
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
        }
    }

    private static void parseBody(InputStreamReader isr, HttpRequest req) {

    }
}
