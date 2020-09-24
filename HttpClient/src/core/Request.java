package core;

import java.util.HashMap;

public class Request {

    private HttpMethod method;
    private String host;
    private String resource;
    private String httpVersion;
    private String payload;
    private HashMap<String, String> headers;
    private String messageBody;

    public Request(HttpMethod method, String host, String resource, String httpVersion)
    {

    }

    public String toString()
    {
        return "Testing";
    }

    private void handleRequestFormat() {




    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getHost() {
        return host;
    }

    public String getResource() {
        return resource;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public String getPayload() {
        return payload;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }


}
