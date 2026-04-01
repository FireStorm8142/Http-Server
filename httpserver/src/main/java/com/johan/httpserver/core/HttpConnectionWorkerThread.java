package com.johan.httpserver.core;

import com.johan.http.HttpParser;
import com.johan.http.HttpParsingException;
import com.johan.http.HttpRequest;
import com.johan.httpserver.core.io.WebRootHandler;
import com.johan.http.HttpResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread{
    private final WebRootHandler handler;
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
    private final Socket socket;
    public HttpConnectionWorkerThread(Socket socket, WebRootHandler handler) {
        this.socket=socket;
        this.handler=handler;
    }

    @Override
    public void run(){
        InputStream ipStream = null;
        OutputStream opStream = null;
        try {
            ipStream = socket.getInputStream();
            opStream = socket.getOutputStream();
            HttpRequest req;

            //Each Thread Sends Request to HttpParser and stores parsed data in HttpRequest class
            try {
                req = HttpParser.parseHttpReq(ipStream);
            }catch (HttpParsingException e){
                HttpResponse.writeErrorResponse(e.getErrorCode(), opStream);
                return;
            }

            //Create HttpResponse packet if everything went well
            HttpResponse resp = new HttpResponse(opStream, req, handler);
            resp.writeResponse();

            LOGGER.info("Connection Processing Finished");
        }catch(IOException e){
            LOGGER.error("Problem with communication", e);
        }catch(HttpParsingException e){
            try {
                HttpResponse.writeErrorResponse(e.getErrorCode(), opStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        finally {
            if(ipStream!=null){
                try {
                    ipStream.close();
                }catch (IOException ignored) {}
            }
            if(opStream!=null){
                try{
                    opStream.close();
                }catch (IOException ignored){}
            }
            if(socket!=null){
                try{
                    socket.close();
                }catch (IOException ignored){}
            }
        }
    }
}
