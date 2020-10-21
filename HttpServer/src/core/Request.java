package core;

import helpers.Method;

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

    public String toString() {

        StringBuilder httpBuilder = new StringBuilder();
        httpBuilder.append(method + " " + resource + " " + version + System.lineSeparator());

        headers.forEach((k, v) -> httpBuilder.append(k + ": " + v + System.lineSeparator()));
        httpBuilder.append(System.lineSeparator());
        httpBuilder.append(body);
        return httpBuilder.toString();
    }

}
