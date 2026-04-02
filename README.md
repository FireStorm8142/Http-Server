





# 🌐 HTTP Server in Java : Low-Level HTTP/1.1 Implementation

This is a minimal, Multi-threaded HTTP/1.1 server I built from scratch in Java. This project focuses on understanding **raw socket communication, HTTP parsing, Basic security features and Response generation** without using any frameworks like Spring.      
This server implements some core HTTP/1.1 parsing and error handling principles based on [RFC 7230](https://datatracker.ietf.org/doc/html/rfc7230) (HTTP Protocol).

## 🏗️ System Architecture

* ***HttpConnectionWorkerThread*** : Creates a thread and handles each client requests in it.
* ***HttpParser*** : Parses the raw HTTP requests (Request line, Headers and Body) into structured data.
* ***HttpRequest*** : Stores method, target, version, headers and body.
* ***HttpResponse*** : Generates Responses packets or Error response packets and serves files.
* ***WebRootHandler*** : Normalizes, Resolves and Validates the path requested by client. Safely reads and serves files .
* ***Custom Exception Classes*** : Exception classes like HttpParsingException, BadHttpVersionException, HttpConfigException are custom-built classes to handle and log the various exceptions that can show up.

## 🎯 What This Project Demonstrates

* Low-level socket programming
* Multi-threaded server design
* Designing modular, extensible server architecture
* Stream-based I/O handling using `InputStream` / `OutputStream`
* HTTP protocol parsing and validation
* Defensive parsing of malformed HTTP requests
* Secure file serving and path validation

## ⚙️ Key Features

* Byte-level custom-built HTTP parser
* Thread-per-connection concurrency
* Proper Logging of server operations (using slf4j Logger)
* Directory Traversal Attack Protection
* Static file serving from WebRoot
* Proper HTTP error handling (Client Error Codes(4xx), Server Error Codes (5xx))

## 🚀 Installation & Usage

### 1. Clone & Run
``` 1. git clone https://github.com/FireStorm8142/Http-Server ```

````2. cd httpserver````

````3. Use your IDE or terminal to compile and run (httpserver.java is the entry point of the server) ````

### 2. Web Root
Place static files inside:       
``` /httpserver/WebRoot/ ```
I've already placed a few sample files inside the folder.

### 3. Test using Postman or Curl
Open up postman or a Linux terminal, here are some sample commands to see the response generation:

````curl http://localhost:8080/```` this will return file not found since we haven't specified a target.        
````curl http://localhost:8080/<your-file-name>```` this will currently serve HTML, Text, JSON files.        
````curl http://localhost:8080/../../etc/passwd```` this is a Directory Traversal Attack, server will reject the request.

you can play around with different variations of the URL and try different Method name's. If you discover any security vulnerabilities or system flaws, please do feel free to raise an Issue or open a Pull Request on GitHub.
## 🔄 Request Flow

1. Client connects
2. New Thread created
3. Request parsed
4. Request object created
5. Response generated
6. File served or error response returned
7. Thread closed

## ⚠️ Current Limitations

* Blocking I/O
* Model is inefficient for large no. of users
* No keep-alive
* No HTTPS
* No Binary file serving


## 📌 Notes

* This project is designed for learning purposes and is not production-ready. It's just a simple implementation of how servers like Apache or Nginx work at its core. I am still working on adding new features and functionality regularly.

* This project was built in IntelliJ IDEA using Maven as a project manager, there might be some issues if you try to run it in VSCode or other code editor's, ensure that you have the necessary extensions (**[Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-maven)**) installed, VSCode should then detect the pom.xml file and automatically set up the environment.