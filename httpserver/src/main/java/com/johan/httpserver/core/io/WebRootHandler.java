package com.johan.httpserver.core.io;

import com.johan.http.HttpParsingException;
import com.johan.http.HttpStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;

public class WebRootHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebRootHandler.class);

    private final File webRoot;

    public WebRootHandler(String webRootPath) throws WebRootNotFoundException{
        webRoot = new File(webRootPath);
        if(!webRoot.exists() || !webRoot.isDirectory()){
            throw new WebRootNotFoundException("Error Fetching WebRoot");
        }
    }

    //This method normalizes the path and checks whether
    //User is attempting a directory traversal attack
    private File resolveSafeFile(String relativePath) throws IOException {
        File file = new File(webRoot, relativePath);

        String rootPath = webRoot.getCanonicalPath();
        String filePath = file.getCanonicalPath();

        if (!filePath.startsWith(rootPath)) {
            throw new SecurityException("Directory Traversal Attempted");
        }

        return file;
    }

    public String GetFileType(String relativePath) throws FileNotFoundException, HttpParsingException{
        try {
            File file;
            try {
                file = resolveSafeFile(relativePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!file.exists()){
                throw new FileNotFoundException("File not found at: "+relativePath);
            }
            String mimeType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
            if (mimeType == null)   return "application/octet-stream";
            else return mimeType;
        }catch (SecurityException e){
            LOGGER.error("Directory Traversal Attempted",e);
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_403_FORBIDDEN);
        }
    }

    public byte[] GetFileByteArrayData(String relativePath) throws IOException, HttpParsingException {
        try{
            File file = resolveSafeFile(relativePath);
            if(!file.exists()){
                throw new FileNotFoundException();
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] fileBytes = fis.readAllBytes();
            fis.close();
            return fileBytes;
        }catch (SecurityException e){
            LOGGER.error("Directory Traversal Attempted",e);
            throw new HttpParsingException(HttpStatusCode.CLIENT_ERROR_403_FORBIDDEN);
        }
    }
}
