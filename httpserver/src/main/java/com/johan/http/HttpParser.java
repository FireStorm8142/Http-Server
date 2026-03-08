package com.johan.http;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpParser {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpParser.class);

    private static final int SP = 0x20; //32
    private static final int CR = 0x0D; //13
    private static final int LF = 0x0A; //10

    public HttpRequest parseHttpReq(InputStream iptStream) {
        InputStreamReader isr = new InputStreamReader(iptStream, StandardCharsets.US_ASCII);

        HttpRequest request = new HttpRequest();

        parseRequestLine(isr, request);
        parseHeader(isr, request);
        parseBody(isr, request);

        return request;
    }
    private void parseRequestLine(InputStreamReader isr, HttpRequest req) throws IOException {
        int _byte;
        while ((_byte=isr.read()) >=0){
            if(_byte==CR){
                _byte=isr.read();
                if(_byte==LF){
                    return;
                }
            }
        }
    }

    private void parseHeader(InputStreamReader isr, HttpRequest req) {

    }

    private void parseBody(InputStreamReader isr, HttpRequest req) {

    }
}


