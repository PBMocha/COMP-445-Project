import core.HttpMethod;
import core.Request;
import core.Response;
import core.Url;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class HttpClient
{
    private Socket clientSocket;
    private OutputStreamWriter out;
    private BufferedReader in;

    /**
     * Initialize client socket and connect to target host using TCP
     * Initialize request and response streams
     * @param url
     */
    public void startHttpConnection(String url)
    {
        try {

            clientSocket = new Socket(url, 80);

            out = new OutputStreamWriter(clientSocket.getOutputStream());

            //out.write(Response )
            out.write("GET /status/418 HTTP/1.0\r\nUser-Agent: Hello\r\n\r\n");
            out.flush();

            Response response = new Response(clientSocket.getInputStream());
            System.out.println(response);

        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }
    }

    public void closeConnection()
    {
        try{    
            out.close();
            clientSocket.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a custom request
     * @param request
     * @return Response
     */
    public Response send(Request request)
    {
        try {

            clientSocket = new Socket(request.getHost(), 80);
            out = new OutputStreamWriter(clientSocket.getOutputStream());
            out.write(request.toString());
            out.flush();

            //Will parse to Response object
            return new Response(clientSocket.getInputStream());

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally {
            closeConnection();
        }

        return null;
    }

    private String[] divideUrlString(String url)
    {
        //Split url to {host, resource},
        // eg. "www.localhost.com/getresource" => {"www.localhost.com", "getResource"}
        /*
            Cases to consider from left to right:
            1. url contains "http://" or "https://"
            2. url contains port; "localhost.com:8000"
            3.
         */

        //Ignore http and https
        if (url.startsWith("http://") || url.startsWith("https://")) {
            //System.out.println("Removing Http(s) extension");
            url = url.replace("https://", "");
            //System.out.println(url);
        }

        //Take in account for url port

        //
        String[] urlSet = url.split("/", 2);

        urlSet[1] = "/" + urlSet[1];

        if (urlSet.length != 2)
        {
            return null;
        }

        return urlSet;
    }

    public void printUrl(String url)
    {
        String[] urlParts = divideUrlString(url);

        if (urlParts == null)
        {
            System.out.println("Invalid url");
            return;
        }

        for (String part : urlParts)
        {
            System.out.println(part);
        }
    }


    //Http Methods
    public Response get(String request)
    {
        String[] parsedUrl = divideUrlString(request);

        String host = parsedUrl[0];
        String resource = parsedUrl[1];

        return this.send(new Request(host, resource));
    }

    public Response post(String request)
    {
        return null;
    }

    public static void main(String[] args)
    {
        //Parse command line arguments

        HttpClient client = new HttpClient();
        Request request = new Request.RequestBuilder(HttpMethod.GET, new Url("www.httpbin.org/status/418")).build();
        request.addHeader("User-Message", "Hello");

        System.out.println(request);

        Response response = client.send(request);
        System.out.println(response);


    }

}