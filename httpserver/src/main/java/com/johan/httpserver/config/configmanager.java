package com.johan.httpserver.config;

public class configmanager {
    private static configmanager myconfigmanager;
    private static configuration mycurrentconfig;
    
    private configmanager(){
        
    }

    public static configmanager getInstance(){
        if(myconfigmanager==null)
            myconfigmanager= new configmanager();
        return myconfigmanager;
    }

    //method for loading a config file by the path provided
    public void loadConfigFile(String filePath){

    }

    //method for returning currently loaded config
    public void getCurrentConfig(){

    }
}
