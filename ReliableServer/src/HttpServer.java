import core.Packet;
import core.ReliableSocket;
import core.RSRequest;
import core.RSResponse;
import helpers.Status;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class HttpServer {


    private DatagramSocket serverSocket;
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

            serverSocket = new DatagramSocket(port);
            System.out.println("Server started on port: " + port);
            System.out.println("Waiting for client ...");

            //Wait for a client to connect
            while (true) {

//                byte[] buf = new byte[Packet.MAX_BYTES];
//                DatagramPacket dgPck = new DatagramPacket(buf, buf.length);

                //Wraps the udp socket into reliable socket
                ReliableSocket reliableSocket = new ReliableSocket(serverSocket);

                //Receive request
                reliableSocket.receive();

                //Build response

                //serverSocket.send(packet);



                if (isVerbose) {

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {

        //reader.close();
        if (serverSocket != null) {
            serverSocket.close();
        }

    }

    private void handshake(DatagramPacket dg) {

        try {
            //1: SYN from client
            Packet packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            System.out.println("received (1): " + packet.toString());

            //2: SYN+ACK
            packet.setType((byte)2);
            System.out.println("sending (2): " + packet.toString());
            dg.setData(packet.toBytes());
            serverSocket.send(dg);

            //3: ACK from client
            serverSocket.receive(dg);
            packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            System.out.println("recieved (3): " + packet.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void selectiveRepeatRecieve() throws IOException {

        //ArrayList for managing seq numbers and window frame
        //True if packet with seq number i is received
        List<Boolean> seqN = createSeqNFrame(10);

        //Window frame represented using index pointers: interval = [winBeg, winEnd)
        int windowSize = (int)Math.floor(seqN.size() / 2);
        int winBeg = 0;

        //Keep receiving packets
        while(seqN.contains(false)) {

            int winEnd = winBeg + windowSize;

            //Receive and Parse packet
            byte[] buf = new byte[Packet.MAX_BYTES];
            DatagramPacket dg = new DatagramPacket(buf, buf.length);
            serverSocket.receive(dg);
            Packet packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            int curSeqNum = packet.getSeqNumber();
            System.out.println("received sn: " + curSeqNum);

            //Send back ACK
            packet.setType((byte)2);
            dg.setData(packet.toBytes());
            serverSocket.send(dg);
            System.out.println("ACK sent for packet " + curSeqNum);

            //Shift window if curSeqNumber is oldest packet
            if (curSeqNum == winBeg && winEnd != seqN.size()) {
                winBeg++;
            }
        }
    }



    private ArrayList<Boolean> createSeqNFrame(int size) {

        ArrayList<Boolean> frame = new ArrayList<>(size);
        for (int cnt = 0; cnt < size; cnt++) {
            frame.add(false);
        }

        return frame;
    }


    //Peak Engineering btw
    public void returnFiles(String path, RSResponse response){
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

    private RSResponse handleRequest(RSRequest request) {

        //System.out.println("Processing Request: ");
        RSResponse response = new RSResponse();
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
                //System.out.println(response.toString());
                return response;
            }
            else {
                response.setStatusCode(Status.BAD_REQUEST);
                //response.toString();
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

        }
        else {
            response.setStatusCode(Status.METHOD_NOT_ALLOWED);
            response.setBody("Invalid method was provided, please choose 'GET' or 'POST'");
        }

        //Build response object
        //response.toString();

        return response;
    }
}
