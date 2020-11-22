import core.HttpMethod;
import core.Request;
import core.Response;
import core.Url;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class HttpClient
{
    private DatagramSocket clientSocket;

    public void closeConnection()
    {

        clientSocket.close();
    }

    /**
     * Send a custom request
     * @param request
     * @return Response
     */
    public Response send(Request request)
    {
        try {
            InetAddress address = InetAddress.getByName(request.getHost());
            clientSocket = new DatagramSocket();

            byte[] requestByte = request.toString().getBytes();
            handshake(address, 80);

            //Breakdown request into packet(s)
            

//            DatagramPacket packet = new DatagramPacket(requestByte, requestByte.length, address, 80);
//
//            //Lewd Handshake
//
//
//
//            //Send request
//            clientSocket.send(packet);
//
//            //Recieve response
//            clientSocket.receive(packet);
//
//            System.out.println(new String(packet.getData()));
            //Will parse to Response object


            return null;

        } catch (IOException e)
        {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }

        return null;
    }

    private void handshake(InetAddress address, int port) throws IOException{

        Packet synPck = new Packet.PacketBuilder()
                .setType((byte)1)
                .setSeqNumber(1)
                .setPeerAddress(address)
                .setPeerPort((short)port)
                .setPayload(new byte[Packet.MAX_PAYLOAD])
                .build();

        DatagramPacket dgPck = new DatagramPacket(synPck.toBytes(), synPck.toBytes().length, address, port);

        //1: Send SYN
        System.out.println("sending (1): " + synPck.toString());
        clientSocket.send(dgPck);

        //2: Recieve SYN+ACK
        clientSocket.receive(dgPck);
        synPck = Packet.fromBytes(ByteBuffer.wrap(dgPck.getData()));
        System.out.println("received (2): " + synPck.toString());

        synPck.setType((byte)3);
        dgPck.setData(synPck.toBytes());
        //3: Send ACK
        clientSocket.send(dgPck);
        System.out.println("sending (3): " + synPck.toString());

    }

}