import core.Request;
import core.Response;
import helpers.HttpRouter;
import helpers.Status;
import helpers.StreamParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class HttpServer {


    private ServerSocket serverSocket;
    private Socket clientSocket;
    private int port;
    private String rootDir;

    public HttpServer() {
        this(80);
    }

    public HttpServer(int port) {
        this(port, "./");
    }

    public HttpServer(int port, String rootDir) {
        this.port = port;
        this.rootDir = rootDir;

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

    //Peak Engineering btw
    public void returnFiles(String path, Response response){
        File f = new File(path);

        if(f.isDirectory()) {
            String[] filenames = f.list();
            for (int i=0; i<filenames.length; i++) {
                if (filenames[i].contains(".txt")) {
                    //System.out.println("TEXT" +filenames[i]);
                    response.setBody(response.getBody() + f.getPath() + "\\" + filenames[i] +"\n");
                }
                else {
                    //System.out.println("FOLDER"+filenames[i]);
                    returnFiles(path +"/"+filenames[i], response);
                }
            }
        }
    }

    private Response handleRequest(Request request) {

        //handle method
        System.out.println("Processing Request: ");
        Response response = new Response();
        response.setVersion(request.getVersion());
        response.addHeader("Host", serverSocket.getInetAddress().getHostAddress());
        response.addHeader("Date", java.util.Calendar.getInstance().getTime() + "");
        if (serverSocket.isClosed())
            response.addHeader("Connection", "closed");
        else
            response.addHeader("Connection", "open");

        //Validate request headers
        if (request.getMethod().equals("GET")) {
            String[] path = request.getResource().split("\\.");

            File f = new File(rootDir, path[0]);

            if (request.getResource().contains("txt")) {
                response.addHeader("Content-Type","text/html");
            }
            else if(f.isDirectory()) {
                returnFiles(rootDir+request.getResource(), response);
                response.setStatusCode(Status.OK);
                System.out.println(response.toString());
                return response;
            }
            else {
                response.setStatusCode(Status.BAD_REQUEST);
                response.toString();
                return response;
            }

            //System.out.println(request.getResource());
            String body="";
            try {
                File file = new File(rootDir, request.getResource());

                //Redundant, file should throw it automatically
                if (!file.exists()) {
                    throw new FileNotFoundException();
                }
                else if(file.canRead()) {
                    Scanner sc = new Scanner(file);
                    while (sc.hasNextLine()) {
                        body = sc.nextLine();
                    }
                }
                else {
                    response.setStatusCode(Status.UNAUTHORIZED);
                    response.setBody("Client doesn't have permission to read file");
                }

            } catch (FileNotFoundException e) {
                response.setStatusCode(Status.NOT_FOUND);
                response.setBody("File has not been found");
            }

            response.setBody(body);
            response.addHeader("Content-Length", body.getBytes().length +"");
        }

        else if (request.getMethod().equals("POST")) {

            try {
                System.out.println(request.getResource());
                String dir = "";
                File file = null;
                if (request.getResource().contains("/")) {
                    dir += request.getResource().substring(0, request.getResource().lastIndexOf('/')+1);
                    System.out.println(dir);
                    file = new File(rootDir, dir);
                    System.out.println(file.mkdirs());
                }
                file = new File(rootDir, request.getResource());
                //file.setReadOnly();
                boolean fileCreated = file.createNewFile();
                System.out.println(fileCreated);

                FileWriter w = new FileWriter(file);
                if (fileCreated) {
                    if (file.canRead() && file.canWrite()) {
                        response.setStatusCode(Status.CREATED);
                    }
                    else {
                        response.setStatusCode(Status.UNAUTHORIZED);
                        response.setBody("Client does not have read or write access to the file");

                    }
                }
                else {
                    response.setStatusCode(Status.OK);
                }
                w.write(request.getBody());
                w.close();
                response.addHeader("Content-Length", request.getBody().getBytes().length+"");
                response.addHeader("Content-Type", "text/html");

            } catch (IOException e) {
                e.getStackTrace();
                response.setStatusCode(Status.NOT_FOUND);
                response.setBody("File has not been found");
            }

        }
        else {
            response.setStatusCode(Status.METHOD_NOT_ALLOWED);
            response.setBody("Invalid method was provided, please choose 'GET' or 'POST'");
        }

        //Build response object
        response.toString();
        System.out.println(response);

        return response;
    }

    public static void main(String[] args)
    {

        HttpServer server = new HttpServer(80, "./doc");
        server.start();
    }
}
