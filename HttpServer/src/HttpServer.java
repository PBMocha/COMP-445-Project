<<<<<<< HEAD
import core.Request;
import core.Response;
=======
import core.Request2;
import core.Response2;
import helpers.HttpRouter;
>>>>>>> ccd1ef884f89b0d5d1d1cd4c9f6621bcc782ea52
import helpers.Status;
import helpers.StreamParser;

import java.io.*;
<<<<<<< HEAD
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
=======
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

>>>>>>> ccd1ef884f89b0d5d1d1cd4c9f6621bcc782ea52

public class HttpServer {


    private ServerSocket serverSocket;
    private int port;
    private String rootDir;
    private boolean isVerbose = false;

    public HttpServer() {
        this(80);
    }

    public HttpServer(int port) {
        this(port, "./");
    }

    public HttpServer(int port, String rootDir) {
        this.port = port;
        this.rootDir = rootDir;
        this.isVerbose = false;

    }
    public HttpServer(int port, String rootDir, boolean isVerbose) {
        this.port = port;
        this.rootDir = rootDir;
        this.isVerbose = isVerbose;
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
                Request2 req = StreamParser.buildHttpRequest(input);
                //input.close();

<<<<<<< HEAD
                //Send back response to client

                Response response = handleRequest(req);
                if (isVerbose) {
                    System.out.println("Recieved Request ... \n" + req.toString() + "\n");
                    System.out.println("Responded to " + client.getInetAddress() + " with ...\n" + response.toString());
                }

=======
                //String rawRequest = input.readUTF();
                System.out.println("Request Received:" + client.toString());
                //Send back response to client

                Response2 response = handleRequest(req);
>>>>>>> ccd1ef884f89b0d5d1d1cd4c9f6621bcc782ea52
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

<<<<<<< HEAD
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

        //System.out.println("Processing Request: ");
        Response response = new Response();
=======
    /*public void addRoute(String method, String location, File file) {
    }*/
    
    public void returnFiles(String path, Response2 response){
    	File f = new File(path);
        
        if(f.isDirectory()) {
        	String[] filenames = f.list();
        	for (int i=0; i<filenames.length; i++) {        		
        		if (filenames[i].contains(".txt")) {
            		//System.out.println("TEXT" +filenames[i]);
        			response.setBody(filenames[i] +"\n");
        		}
        		else {
            		//System.out.println("FOLDER"+filenames[i]);
        			returnFiles(path +"/"+filenames[i], response);
        		}
        	}
        }        
    }
    
    private Response2 handleRequest(Request2 request) {
    	
        //handle method
        System.out.println("Processing Request: ");
        Response2 response = new Response2();
>>>>>>> ccd1ef884f89b0d5d1d1cd4c9f6621bcc782ea52
        response.setVersion(request.getVersion());
        response.addHeader("Host", serverSocket.getInetAddress().getHostAddress());
        response.addHeader("Date", java.util.Calendar.getInstance().getTime() + "");
        if (serverSocket.isClosed())
<<<<<<< HEAD
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
                //System.out.println(response.toString());
                return response;
            }
            else {
                response.setStatusCode(Status.BAD_REQUEST);
                //response.toString();
                return response;
            }
=======
        	response.addHeader("Connection", "closed");
        else
        	response.addHeader("Connection", "open");

        
        //Validate request headers
        if (request.getMethod().equals("GET")) {
            String[] path = request.getResource().split("\\.");
            
            File f = new File(path[0]);
            
            if (request.getResource().contains("txt")) {
                response.addHeader("Content-Type","text/html");
            }
            else if(f.isDirectory()) {
            	returnFiles(request.getResource(), response);
            	response.setStatusCode(Status.OK);
            	response.toString();
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
            	File file = new File(request.getResource());
            	if(file.canRead()) {
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
                String dir="./";
                File file = null;
                if (request.getResource().contains("/")) {
                	dir += request.getResource().substring(0, request.getResource().lastIndexOf('/')+1);
                	System.out.println(dir);
            		file = new File(dir);
            		System.out.println(file.mkdirs());
                }
                file = new File("./", request.getResource());
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
        	System.out.println("Invalid method was provided, please choose 'GET' or 'POST'");
        }

        //Build response object
        response.toString();
        System.out.println(response);
>>>>>>> ccd1ef884f89b0d5d1d1cd4c9f6621bcc782ea52

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
                        body += sc.nextLine();
                        if (sc.hasNextLine()) {
                            body += "\n";
                        }
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
                //System.out.println(request.getResource());
                String dir = "";
                File file = null;
                if (request.getResource().contains("/")) {
                    dir += request.getResource().substring(0, request.getResource().lastIndexOf('/')+1);
                    System.out.println(dir);
                    file = new File(rootDir, dir);
                    //System.out.println(file.mkdirs());
                }
                file = new File(rootDir, request.getResource());
                //file.setReadOnly();
                boolean fileCreated = file.createNewFile();
                //System.out.println(fileCreated);

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

<<<<<<< HEAD
        }
        else {
            response.setStatusCode(Status.METHOD_NOT_ALLOWED);
            response.setBody("Invalid method was provided, please choose 'GET' or 'POST'");
        }

        //Build response object
        //response.toString();

        return response;
=======

    public static void main(String[] args) throws UnknownHostException, IOException
    {
       
    	
    	HttpServer server = new HttpServer(80);
        server.start();
>>>>>>> ccd1ef884f89b0d5d1d1cd4c9f6621bcc782ea52
    }
}
