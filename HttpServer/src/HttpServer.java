import core.Request2;
import core.Response2;
import helpers.HttpRouter;
import helpers.Status;
import helpers.StreamParser;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;


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
                //System.out.println("test2 req received"); //reaches here

                //String rawRequest = StreamParser.readStream(input);
                Request2 req = StreamParser.buildHttpRequest(input);
                //input.close();
                //System.out.println("pre req received");

                //String rawRequest = input.readUTF();
                System.out.println("Request Received:" + client.toString());
                //System.out.println("after req received");
                //Send back response to client

                Response2 response = handleRequest(req);
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

    private Response2 handleRequest(Request2 request) {

        //handle method
        System.out.println("Processing Request: ");
        //System.out.println(request.getMethod().equals("GET"));
        Response2 response = new Response2();
        response.setVersion(request.getVersion());
        response.addHeader("Host", serverSocket.getInetAddress().getHostAddress());
        response.addHeader("Date", java.util.Calendar.getInstance().getTime() + "");
        if (serverSocket.isClosed())
        	response.addHeader("Connection", "closed");
        else
        	response.addHeader("Connection", "open");

        
        //Validate request headers
        if (request.getMethod().equals("GET")) {
        	//Build response object
            //response.addHeader("Host", serverSocket.getInetAddress().getHostName());            
            String[] path = request.getResource().split("\\.");

            if (path[1].equals("txt")) {
                response.addHeader("Content-Type","text/html");
            }
            else {
            	response.setStatusCode(Status.BAD_REQUEST);
            	response.toString();
            	return response;
            }
        	System.out.println(request.getResource());
            //System.out.println(request);
            String body="";
            try {
            	File file = new File(request.getResource());
                Scanner sc = new Scanner(file);
                while (sc.hasNextLine()) {
                  body = sc.nextLine();
                }
            } catch (FileNotFoundException e) {         	
            	response.setStatusCode(Status.NOT_FOUND);
            	response.setBody("File has not been found");
            }
            
            response.setBody(body);            
            response.addHeader("Content-Length", body.getBytes().length +"");            
        }
        
        else if (request.getMethod().equals("POST")) {
        	//check content-length
        	//body set in stream parser?

        	try {
        		File file = new File(request.getResource());
    			FileWriter w = new FileWriter(file);
    			//System.out.println(file.createNewFile());
				if (file.createNewFile()) {
					w.write(request.getBody());
					w.close();
					System.out.println("check");
					//response.setBody(request.getBody());
					response.setStatusCode(Status.CREATED);
				}
				else {
					
				}
			} catch(FileNotFoundException e) {
				
				System.out.println("YOHBHB");
			}
        	catch (IOException e) {
				e.getStackTrace();
				response.setStatusCode(Status.NOT_FOUND);
            	response.setBody("File has not been found");
			}
        }
        else {
        	System.out.println("Invalid method was provided, please choose 'GET' or 'POST'");
        }

        //Build response object
        //response.addHeader("Host", serverSocket.getInetAddress().getHostName());
        //response.setBody("LMAO\r\nThis is List!");
        response.toString();
        System.out.println(response);

        return response;
    }

	//String[] args = {"get", "hello"};

    public static void main(String[] args) throws UnknownHostException, IOException
    {
       
    	
    	HttpServer server = new HttpServer(80);
        server.start();
    }
}
