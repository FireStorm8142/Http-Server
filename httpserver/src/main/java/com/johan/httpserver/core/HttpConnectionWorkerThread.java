package com.johan.httpserver.core;

import com.johan.http.HttpParser;
import com.johan.http.HttpParsingException;
import com.johan.http.HttpRequest;
import com.johan.http.HttpStatusCode;
import com.johan.httpserver.core.io.WebRootHandler;
import com.johan.httpserver.core.io.WebRootNotFoundException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnectionWorkerThread extends Thread{

    final String CRLF = "\r\n"; //13, 10
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
    private final Socket socket;
    public HttpConnectionWorkerThread(Socket socket){
        this.socket=socket;
    }

    //For sending the Error code and Message to Client
    private void sendErrorResponse(HttpStatusCode code, OutputStream op) throws IOException {
        String body = code.MESSAGE;

        String response =
                "HTTP/1.1 " + code.STATUS_CODE + " " + code.MESSAGE + CRLF +
                "Content-Length: " + body.length() + CRLF +
                CRLF +
                body;

        op.write(response.getBytes());
    }

    @Override
    public void run(){
        InputStream ipStream = null;
        OutputStream opStream = null;
        try {
            ipStream = socket.getInputStream();
            opStream = socket.getOutputStream();
            HttpRequest req;
            try {
                req = HttpParser.parseHttpReq(ipStream);
            }catch (HttpParsingException e){
                sendErrorResponse(e.getErrorCode(), opStream);
                return;
            }

            String path = req.getRequestTarget();
            WebRootHandler handler;
            try {
                handler = new WebRootHandler("httpserver/WebRoot");
            } catch (WebRootNotFoundException e) {
                throw new HttpParsingException(HttpStatusCode.SERVER_ERROR_500_INTERNAL_SERVER_ERROR);
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
            //Response
            String response =
                    "HTTP/1.1 200 OK" + CRLF + // Status Line : HTTP Version, Response_code, Response_msg
                    "Content-Length: " + fileBytes.length + CRLF + // Header
                    "Content-Type: "+contentType+ CRLF + //Add MIME Files later
                    CRLF;

            opStream.write(response.getBytes());
            opStream.write(fileBytes);

            LOGGER.info("Connection Processing Finished");
        }catch(IOException e){
            LOGGER.error("Problem with communication", e);
        }catch(HttpParsingException e){
            try {
                sendErrorResponse(e.getErrorCode(), opStream);
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
