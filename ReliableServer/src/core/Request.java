package core;

import helpers.Method;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.util.HashMap;

public class Request {

    private String method;
    private String resource;
    private String version;
    private HashMap<String, String> headers;
    private String body;

    public Request(String method, String resource) {
        this(method, resource, "HTTP/1.0");
    }

    public Request(String method, String resource, String version) {
        this.method = method;
        this.resource = resource;
        this.version = version;
        this.body = "";
        this.headers = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getResource() {
        return resource;
    }

    public String getVersion() {
        return version;
    }

    public Request addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String toString() {

        StringBuilder httpBuilder = new StringBuilder();
        httpBuilder.append(method + " " + resource + " " + version + System.lineSeparator());

        headers.forEach((k, v) -> httpBuilder.append(k + ": " + v + System.lineSeparator()));
        httpBuilder.append(System.lineSeparator());
        httpBuilder.append(body);
        return httpBuilder.toString();
    }

    public static Request fromBuffer(BufferedReader reader) throws IOException {

        String line = reader.readLine();
        String[] requestLine = line.split(" ");

        Request request = new Request(requestLine[0], requestLine[1], requestLine[2]);

        //Parse Headers
        line = reader.readLine();
        while (!line.isEmpty()) {
            String[] headerContent = line.split(":", 2);
            request.addHeader(headerContent[0].trim(), headerContent[1].trim());
            line = reader.readLine();
        }

        if (request.getHeader("Content-Length") != null) {
            int contentLength = Integer.parseInt(request.getHeader("Content-Length"));
            char[] bodyBytes = new char[contentLength];

            reader.read(bodyBytes);
            request.setBody(String.valueOf(bodyBytes));
        }

        //Checks for body

        return request;


    }
}
