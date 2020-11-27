package core;

import helpers.Status;
import java.util.HashMap;

public class Response2 {

    private Status statusCode;
    private String version;
    private HashMap<String, String> headers;
    private String body;

    public Response2() {
        this(Status.OK, "HTTP/1.0");
    }

    public Response2(Status statusCode) {
        this(statusCode, "HTTP/1.0");
    }

    public Response2(Status statusCode, String version) {

        this.statusCode = statusCode;
        this.version = version;
        this.body = "";
        this.headers = new HashMap<>();
    }

    public Response2 addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public Status getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Status statusCode) {
        this.statusCode = statusCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String toString() {

        StringBuilder httpBuilder = new StringBuilder();
        httpBuilder.append(version + " " + statusCode.getCode() + " " + statusCode.getReason() + System.lineSeparator());

        headers.forEach((k, v) -> httpBuilder.append(k + ": " + v + System.lineSeparator()));
        httpBuilder.append(System.lineSeparator() + body);
        return httpBuilder.toString();
    }

}
