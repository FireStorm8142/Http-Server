package com.johan.httpserver;

import com.johan.httpserver.config.configuration;
import com.johan.httpserver.config.configmanager;

public class httpserver {
    public static void main(String[] args)
    {
        System.out.println("Server starting...");

        configmanager.getInstance().loadConfigFile(("httpserver/src/main/resources/http.json"));
        configuration conf = configmanager.getInstance().getCurrentConfig();

        System.out.println("Using port: "+conf.getPort());
        System.out.println("Using WebRoot: "+conf.getWebroot());
    }
}