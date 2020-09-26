import core.Request;
import core.Response;

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
            //in.close();
            clientSocket.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Request request)
    {

    }

    public Response send(Request request)
    {
        return null;
    }

    private void parseUrl()
    {

    }

    //Http Methods
    public Response get(Request request)
    {
        return null;
    }

    public Response post(Request request)
    {
        return null;
    }

    public static void main(String[] args)
    {

        //Parse command line arguments


        HttpClient client = new HttpClient();

        client.startHttpConnection("www.httpbin.org");
    }

}