package com.johan.httpserver.core.io;

public class WebRootNotFoundException extends Exception{
    final String Message;
    public WebRootNotFoundException(String s) {
        Message = s;
    }

    public String getMessage(){
        return Message;
    }
}
