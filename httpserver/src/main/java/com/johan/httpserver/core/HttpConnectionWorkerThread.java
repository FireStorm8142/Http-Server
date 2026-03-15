package com.johan.httpserver.core;

import com.johan.http.HttpParser;
import com.johan.http.HttpParsingException;
import com.johan.http.HttpRequest;
import com.johan.http.HttpStatusCode;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;

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
            HttpRequest req = null;
            try {
                req = HttpParser.parseHttpReq(ipStream);
            }catch (HttpParsingException e){
                throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_400_BAD_REQ);
            }

            //Get the file that the user wants
            //finish working on this later
            String path = req.getRequestTarget();
            if (path.equals("/")){
                path="/Index.html";
            }

            File file = new File(System.getProperty("user.dir") + "/httpserver/WebRoot" + path);
            final String CRLF = "\r\n"; //13, 10

            //File Not Found
            if(!file.exists()){
                String body = "404 Not Found";
                String noresponse =
                        "HTTP/1.1 404 Not Found" + CRLF +
                        "Content-Length: " + body.length() + CRLF +
                        CRLF +
                        body;
                opStream.write(noresponse.getBytes());
                return;
            }

            //Response
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String response =
                    "HTTP/1.1 200 OK" + CRLF + // Status Line : HTTP Version, Response_code, Response_msg
                    "Content-Length: " + fileBytes.length + CRLF + // Header
                    "Content-Type: text/html"+ CRLF + //Add MIME Files later
                    CRLF;

            opStream.write(response.getBytes());
            opStream.write(fileBytes);

            LOGGER.info("Connection Processing Finished");
        }catch(IOException | HttpParsingException e){
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
