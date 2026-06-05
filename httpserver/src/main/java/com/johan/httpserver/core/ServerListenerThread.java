package com.johan.httpserver.core;

import com.johan.httpserver.core.io.WebRootHandler;
import com.johan.httpserver.core.io.WebRootNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerListenerThread extends Thread{
    private final static Logger LOGGER = LoggerFactory.getLogger(ServerListenerThread.class);

    private final WebRootHandler handler;
    private final int port;
    private final ServerSocket serverSocket;

    public ServerListenerThread(int port, String webRoot) throws IOException, WebRootNotFoundException {
        this.port=port;
        this.serverSocket = new ServerSocket(this.port);
        this.handler = new WebRootHandler(webRoot);
    }

    @Override
    public void run(){

        try {
            while(serverSocket.isBound() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();

                LOGGER.info(" Connection accepted: " + socket.getInetAddress());

                Thread.startVirtualThread(new HttpConnectionWorkerThread(socket, handler));
            }

        }
        catch (IOException e) {
            LOGGER.error("Problem with setting socket", e);
        }
        finally {
            if(serverSocket!=null){
                try{
                    serverSocket.close();
                }catch (IOException ignore){}
            }
        }
    }
}
