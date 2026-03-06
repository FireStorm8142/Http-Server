package com.johan.httpserver.core;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread{

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
    private Socket socket;
    public HttpConnectionWorkerThread(Socket socket){
        this.socket=socket;
    }

    @Override
    public void run(){
        InputStream ipStream = null;
        OutputStream opStream = null;
        try {
            ipStream = socket.getInputStream();
            opStream = socket.getOutputStream();

            String html = "<html><head><title>Http Server</title></head><body><h1>This page was served using my Java http server</h1></body></html>";
            final String CRLF = "\r\n"; //13, 10

            String response =
                    "HTTP/1.1 200 OK" + CRLF + // Status Line : HTTP Version Response_code Response_msg
                            "Content-Length: " + html.getBytes().length + CRLF + // Header
                            CRLF +
                            html +
                            CRLF + CRLF;

            opStream.write(response.getBytes());

            LOGGER.info("Connection Processing Finished");
        }catch(IOException e){
            LOGGER.error("Problem with communication", e);
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
