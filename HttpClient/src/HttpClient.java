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

    //Http Methods

    /**
     * @param urlStr
     * @return
     */
    public Response trace(String urlStr) {
    	Url url = new Url(urlStr);
    	return send(new Request.RequestBuilder(HttpMethod.GET, url).build());
    }
    
    
    /**
     *
     * @param urlStr
     * @return
     */
    public Response get(String urlStr)
    {
        Url url = new Url(urlStr);

        return send(new Request.RequestBuilder(HttpMethod.GET, url).build());
    }

    /**
     *
     * @param urlStr
     * @return
     */
    public Response post(String urlStr, String data)
    {
        Url url = new Url(urlStr);

        Request request = new Request
                .RequestBuilder(HttpMethod.GET, url)
                .body(data)
                .build();

        return send(request);
    }

}