import core.Request;
import core.Response;
import helpers.HttpRouter;
import helpers.Status;
import helpers.StreamParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.*;


public class HttpServer {


    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port;

    public HttpServer() {
        this(80);
    }

    public HttpServer(int port) {
        this.port = port;
    }

    //Start the http server and wait for client requests
    public void start() {
        try {

            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
            System.out.println("Waiting for client ...");

            //Wait for a client to connect
            while (true) {

                Socket client = serverSocket.accept();
                System.out.println("Client connected:\t" + client.getInetAddress());

                OutputStream outputStream = client.getOutputStream();
                PrintWriter out = new PrintWriter(outputStream, true);

                //DataInputStream input = new DataInputStream(new BufferedInputStream(client.getInputStream()));
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //String rawRequest = StreamParser.readStream(input);
                Request req = StreamParser.buildHttpRequest(input);
                //input.close();

                //String rawRequest = input.readUTF();
                System.out.println("Request Received:" + client.toString());

                //Send back response to client

                Response response = handleRequest(req);

                out.print(response.toString() + "\r\n");
                out.flush();

                input.close();
                out.close();
                client.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        try {
            //reader.close();
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addRoute(String method, String location, File file) {


    }

    private Response handleRequest(Request request) {

        //handle method
        System.out.println("Processing Request: ");
        System.out.println(request.toString());

        //Validate request headers


        //Build response object
        Response response = new Response();
        response.addHeader("Host", serverSocket.getInetAddress().getHostName());
        response.setBody("LMAO\r\nThis is List!");

        return response;
    }

    public static void main(String[] args)
    {

        HttpServer server = new HttpServer(80);
        server.start();
    }
}
