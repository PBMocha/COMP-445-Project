package core;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Request {

    private HttpMethod method;
    private String host;
    private String resource;
    private String httpVersion;
    private HashMap<String, String> headers;
    private String messageBody;

    public static class RequestBuilder {

        private final HttpMethod method;
        private final String host; // www.google.com/get
        private final String resource;

        //Optional parameters
        private HashMap<String, String> headers;
        private String body;
        private String httpVersion;

        public RequestBuilder(HttpMethod method, Url url)
        {
            this.method = method;
            this.host = url.getHost();
            this.resource = url.getResource();
            this.httpVersion = "HTTP/1.0";
            this.headers = new HashMap<>();
            this.body = "";
        }

        public RequestBuilder version(String version)
        {
            this.httpVersion = version;
            return this;
        }

        public RequestBuilder header(String key, String value)
        {

            headers.put(key, value);

            return this;
        }
        public RequestBuilder body(String body)
        {
            this.body = body;
            return this;
        }


        public Request build()
        {
            return new Request(this);
        }

    }

    public Request(RequestBuilder builder)
    {
        setHttpVersion(builder.httpVersion);
        setMethod(builder.method);
        setHost(builder.host);
        setResource(builder.resource);
        setHeaders(builder.headers);
        setMessageBody(builder.body);
    }

    public Request(String host, String resource)
    {
        setMethod(HttpMethod.GET);
        setHost(host);
        setResource(resource);
    }

    /**
     *
     * @return http request string
     */
    public String toString()
    {
        StringBuilder requestString = new StringBuilder();

        requestString.append(method + " " + resource + " " + httpVersion);
        requestString.append(System.lineSeparator());
        requestString.append("Host: " + host);
        requestString.append(System.lineSeparator());

        if (messageBody.length() > 0) {
            addHeader("Content-Length", String.valueOf(messageBody.length()));
        }

        for(String key : headers.keySet())
        {
            requestString.append(key + ": " + headers.get(key));
            requestString.append(System.lineSeparator());
        }
        requestString.append(System.lineSeparator());
        requestString.append(messageBody);
        //requestString.append(System.lineSeparator());

        return requestString.toString();
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

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }


}
