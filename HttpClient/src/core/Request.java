package core;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Request {

    private HttpMethod method;
    private String host;
    private String resource;
    private String httpVersion;
    private String payload;
    private HashMap<String, String> headers;
    private String messageBody;

    public static class Builder {

        private final HttpMethod method;
        private final String url; // www.google.com/get

        //Optional parameters
        private HashMap<String, String> headers;

        public Builder(HttpMethod method, String url)
        {
            this.method = method;
            this.url = url;
        }

        public Builder addHeader(String key, String value)
        {
            if (headers == null){
                headers = new HashMap<>();
            }

            headers.put(key, value);

            return this;
        }
    }

    public Request(String host) throws IOException
    {
        setMethod(HttpMethod.GET);
    }

    public Request(HttpMethod method, String host, String resource)
    {

    }

    public Request(HttpMethod method, String host, String resource, String httpVersion)
    {

    }

    public String toString()
    {
        StringBuilder requestString = new StringBuilder();



        return "Testing";
    }


    public Response send()
    {


        return null;
    }

    public Request addHeader(String key, String value)
    {
        headers.put(key, value);
        return this;
    }

    /*
    GETTERS and SETTERS
     */

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
