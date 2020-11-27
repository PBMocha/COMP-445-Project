import core.HttpMethod;
import core.Request;
import core.Response;
import core.Url;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

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



    private int[] ACK = {0,0,0,0,0,0,0,0};
    /*
     * Not only ACKs other stuff we need and might need
     * First 0: Congestion Window
     * Second 0: ECN-Echo
     * Third 0: Urgent
     * Fourth 0: Acknowledgement
     * Fifth 0: Push
     * Sixth 0: Reset
     * Seventh 0: Syn(chronize)
     * Eighth 0: Fin(al) */
    private enum PckType{
        NACK((byte)0),
        ACK((byte)1),
        SENT((byte)2),
        SYN((byte)3),
        SYN_ACK((byte)4),
        FIN((byte)5),
        DATA((byte)6);

        private byte value;

        private PckType (byte value) {
            this.value = value;
        }
        private byte raw() {
            return  value;
        }
    }


    public class Countdown { //when activated tracks time delay until having to resend linked packet
        Timer timer;

        public Countdown(DatagramPacket  p, int[] inTransit, int sec) {
            timer = new Timer();
            timer.schedule(new RemindTask(p, inTransit, sec), sec*1000);
        }

        private class RemindTask extends TimerTask {
            DatagramPacket p;
            int sec;
            int[] inTransit;
            Packet p2;

            public RemindTask(DatagramPacket p, int[] inTransit, int sec) {
                this.p = p;
                this.sec = sec;
                this.inTransit = inTransit;
                try {
                    this.p2 = Packet.fromBytes(ByteBuffer.wrap(p.getData()));
                } catch (IOException e) {
                    System.out.println("Packet is invalid, might be empty.");
                    e.printStackTrace();
                }
            }
            public void run() {
                timer.cancel();
                if (IntStream.of(inTransit).anyMatch(x -> x == p2.getSeqNumber())) { //if its here it doesnt have an ACK
                    try {
                        clientSocket.send(p);
                        timer.schedule(new RemindTask(p, inTransit, sec), sec*1000);

                    } catch (IOException e) {
                        System.out.println("Timer was not given a valid packet");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
//    public Packet sendto(Request request, short port) {
//
//        try {
//            InetAddress address = InetAddress.getByName(request.getHost());
//            clientSocket = new DatagramSocket();
//
//            byte[] requestByte = request.toString().getBytes();
//            System.out.println("test request byte length: " + requestByte.length);
//
//            handshake(address, port);
//
//            //byte[] messagebody = request.getMessageBody().getBytes();
//            Packet[] packetList = null;
//            Packet p = null;
//            int numPackets = 0;
//
//            if (requestByte.length > Packet.MAX_PAYLOAD) {
//
//                numPackets = requestByte.length / Packet.MAX_PAYLOAD+1;
//                System.out.println("TEST" + numPackets);
//                System.out.println("REQUEST BYTE" +requestByte);
//
//                byte[][] allPackets = new byte[numPackets][Packet.MAX_PAYLOAD];
//
//                for (int i=0; i<numPackets; i++) {
//                    allPackets[i] = Arrays.copyOfRange(requestByte, i*Packet.MAX_PAYLOAD, i*Packet.MAX_PAYLOAD+Packet.MAX_PAYLOAD); //do if else statement here in case final bit is less than 1013?
//                }
//
//                packetList = new Packet[numPackets];
//                for (int i=0; i<numPackets; i++) {
//                    packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).setType((byte)0).build();
//                }
//            }
//            else {
//                p = new Packet.PacketBuilder().setPayload(requestByte).setPeerAddress(address).setPeerPort(port).setSeqNumber(0).setType((byte)0).build();
//                System.out.println(p);
//
//            }
//
//            int counter = (int)(packetList[packetList.length - 1].getSeqNumber()/2)-1; //counter to keep track of what is being sent
//            System.out.println(counter);
//            DatagramPacket dgp;
//            int[] inTransit;
//            int[] allACKed = new int[numPackets];
//            int window = (int)packetList[packetList.length - 1].getSeqNumber()/2; //naturally rounded down due to being an int
//            inTransit = new int[window];
//
//
//            if (p == null){
//
//                //System.out.println("WINDOW " +window);
//
//                //send initial bundle
//                for (int i=0; i<window; i++) {
//                    //byte[] bytes = packetList[i].toBytes();
//                    //packetList[i].toBuffer().get(bytes);
//                    inTransit[i] = i;
//                    dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, address, port);
//                    clientSocket.send(dgp); //check that packet contains info once sent
//                    new Countdown(dgp, inTransit, 30); //timer + resend mechanism when timer expires
//                }
//
//                for (int i=0; i<inTransit.length; i++) {
//                    System.out.println("inTransit: " + inTransit[i]);
//                }
//            }
//
//            while(true) {
//
//                //wait for ACKS
//                byte[] buf = new byte[Packet.MAX_BYTES];
//                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
//                clientSocket.receive(receivedPacket);
//
//                Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
//                if (receivedStuff.getType() == 0) { //requested data
//                    System.out.print("Received requested packet: " + receivedStuff.toString());
//                }
//                else {
//                    if (receivedStuff.getType() == 1) { //received ACK
//                        if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if ACK is valid
//                            allACKed[receivedStuff.getSeqNumber()] = receivedStuff.getSeqNumber(); //record successful packet ACK
//                            if ((counter+1 < packetList.length)) { //check if there are other packets to send
//                                System.out.println("COUNTER: " + counter + "SEQUENCE NUMBER: " + receivedStuff.getSeqNumber());
//                                counter++;
//                                inTransit[receivedStuff.getSeqNumber()] = counter;
//                                System.out.println("inTransit value " + inTransit[receivedStuff.getSeqNumber()]);
//                                dgp = new DatagramPacket(packetList[counter].toBytes(), packetList[counter].toBytes().length, address, port);
//                                clientSocket.send(dgp);
//                                new Countdown(dgp, inTransit, 30);
//                            }
//                            else {
//                                for (int i=0; i< allACKed.length; i++) {
//                                    if (!(allACKed[i] ==packetList[i].getSeqNumber())) {
//                                        System.out.println("Missing an ACK for packet" + packetList[i].getSeqNumber());
//                                    }
//                                }
//                            }
//                        }
//                        else {
//                            System.out.println("Unrecognized ACK has been received, possible duplicate.");
//                        }
//                    }
//                    else if (receivedStuff.getType() == 2) { //received NACK
//                        if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if NACK is valid
//                            dgp = new DatagramPacket(packetList[receivedStuff.getSeqNumber()].toBytes(), packetList[receivedStuff.getSeqNumber()].toBytes().length, address, port);
//                            clientSocket.send(dgp);
//                            new Countdown(dgp, inTransit, 30);
//                        }
//                        else {
//                            System.out.println("Unrecognized NACK has been received, possible duplicate.");
//                        }
//                    }
//                    else {
//                        System.out.println("Unidentifiable packet received, please ask server to check type.");
//                    }
//                }
//                String data = new String(receivedPacket.getData(),0, receivedPacket.getLength());
//
//
//
//            }
//
//
//
//            //host use request.getHost() CHECK
//            //assume port is 80 unless given otherwise CHECK
//            //sendto (data, host:port) ARGUMENT PART DONE
//
//            //create bundle of packets CHECK
//            //find sequence number of last one (max sequence number)
//            //set window to floor half of that
//            //send however many packets fit in the window
//            //set timer for each bundle
//            //if timer expires with no ACK resend the unACKED ones
//            //if bad is received resend specifc one
//            //send bundle of packets
//            //
//
//
//            //Breakdown request into packet(s)
//
////            DatagramPacket packet = new DatagramPacket(requestByte, requestByte.length, address, 80);
////
////            //Lewd Handshake
////            //Send request
////            clientSocket.send(packet);
////
////            //Recieve response
////            clientSocket.receive(packet);
////
////            System.out.println(new String(packet.getData()));
//            //Will parse to Response object
//
//
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//        } finally {
//            System.out.println("closing");
//            closeConnection();
//        }
//        return null;
//    }

    public Packet sendto(Request request, short port) {

        try {
            InetAddress address = InetAddress.getByName(request.getHost());
            clientSocket = new DatagramSocket();

            byte[] requestByte = request.toString().getBytes();
            System.out.println("request byte length: " + requestByte.length);

            handshake(address, port);

            Packet[] packetList = new Packet[10];
            Packet p = null;

            //Breaks down the request if its too large for 1 packet
            if (requestByte.length > Packet.MAX_PAYLOAD) {
                System.out.println("Breaking down request...");

                //Get total number of partitions for the request
                int numPackets = requestByte.length / Packet.MAX_PAYLOAD;
                System.out.println("" + numPackets);

                byte[][] allPackets = new byte[numPackets][Packet.MAX_PAYLOAD];

                for (int i=0; i<numPackets; i++) {
                    allPackets[i] = Arrays.copyOfRange(requestByte, i*Packet.MAX_PAYLOAD, i*Packet.MAX_PAYLOAD+Packet.MAX_PAYLOAD); //do if else statement here in case final bit is less than 1013?
                }

                //packetList = new Packet[numPackets];
                for (int i=0; i<numPackets; i++) {
                    //packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).build();
                    packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).setType(PckType.DATA.value).build();
                }

                //Fill the rest of the packets in the buffer
                for (int i=numPackets; i < packetList.length; i++) {
                    packetList[i] = new Packet.PacketBuilder().setPayload("".getBytes()).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).setType(PckType.DATA.value).build();
                }
            }
            else {
                //Store the packet in the first position of the buffer
                packetList[0] = new Packet.PacketBuilder().setPayload(requestByte).setPeerAddress(address).setPeerPort(port).setSeqNumber(0).setType(PckType.DATA.value).build();

                //Fill it with packets
                for(int i = 1; i < packetList.length; i++) {
                    packetList[i] = new Packet.PacketBuilder()
                            .setPeerAddress(address)
                            .setPeerPort(port)
                            .setSeqNumber(i)
                            .setType(PckType.DATA.value)
                            .setPayload("".getBytes())
                            .build();
                }
            }

            //int counter = (int)(packetList[packetList.length - 1].getSeqNumber()/2)-1; //counter to keep track of what is being sent
            DatagramPacket dgp;

//            for (int i=0; i < packetList.length; i++) {
//                System.out.println("pck: " + packetList[i].toString());
//            }

            int winSize = packetList.length / 2;
            int winBase = 0;
            int winEnd = winBase + winSize;

            //Keep looping until all packets are ACK; Or if something bad happens
            int iter = 0;

            for (int i=winBase; i<winEnd; i++) {

                if (packetList[i].getType() != PckType.SENT.value){
                    packetList[i].setType(PckType.SENT.value);
                    dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, address, port);
                    System.out.println("sending: " + packetList[i].toString());
                    clientSocket.send(dgp); //check that packet contains info once sent

                    //new Countdown(dgp, 30); //timer + resend mechanism when timer expires
                }
            }

            while (!fullyAck(packetList)) {

                //if (iter>30) break;
                System.out.println("Iter: " + (iter++));
                //Send packet if not already sent


                //wait for ACKS
                byte[] buf = new byte[Packet.MAX_BYTES];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(receivedPacket);
                Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
                //System.out.println("received: " + receivedStuff.toString());

                //Process ACK packet
                if (receivedStuff.getType() == PckType.ACK.value) { //received ACK

                    packetList[receivedStuff.getSeqNumber()].setType(PckType.ACK.value); //Mark packet with ACK

                    System.out.println("received ACK for: " + receivedStuff.toString());
                    //Shift window of packet at beg of window is ACK
                    if (receivedStuff.getSeqNumber() == winBase && winEnd != packetList.length) {
                        System.out.println("Shifting window");
                        int curSeqN = receivedStuff.getSeqNumber();

                        while(packetList[curSeqN].getType() == PckType.ACK.value && winEnd < packetList.length) {
                            curSeqN++;
                            winBase = curSeqN;
                            winEnd = winBase + winSize;
                        }
                        System.out.println("window: " + winBase + ", " + winEnd);
                        for (int i=winBase; i<winEnd; i++) {

                            if (packetList[i].getType() != PckType.SENT.value){
                                packetList[i].setType(PckType.SENT.value);
                                dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, address, port);
                                System.out.println("sending: " + packetList[i].toString());
                                clientSocket.send(dgp); //check that packet contains info once sent

                                //new Countdown(dgp, 30); //timer + resend mechanism when timer expires
                            }
                        }
                    }
                }
                else if (receivedStuff.getType() == 2) { //received NACK
//                  if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if NACK is valid
                    dgp = new DatagramPacket(packetList[receivedStuff.getSeqNumber()].toBytes(), packetList[receivedStuff.getSeqNumber()].toBytes().length, address, port);
                    clientSocket.send(dgp);
                    //new Countdown(dgp, 30);
                }
                else {
                    System.out.println("Unidentifiable packet received, please ask server to check type.");
                }

                //String data = new String(receivedPacket.getData(),0, receivedPacket.getLength());
                //host use request.getHost() CHECK
                //assume port is 80 unless given otherwise CHECK
                //sendto (data, host:port) ARGUMENT PART DONE

                //create bundle of packets CHECK
                //find sequence number of last one (max sequence number)
                //set window to floor half of that
                //send however many packets fit in the window
                //set timer for each bundle
                //if timer expires with no ACK resend the unACKED ones
                //if bad is received resend specifc one
                //send bundle of packets
                //
            }
            Arrays.asList(packetList).forEach(pck -> System.out.println("Packets " + pck.toString()));

            //Breakdown request into packet(s)

//            DatagramPacket packet = new DatagramPacket(requestByte, requestByte.length, address, 80);
//
//            //Lewd Handshake
//            //Send request
//            clientSocket.send(packet);
//
//            //Recieve response
//            clientSocket.receive(packet);
//
//            System.out.println(new String(packet.getData()));
            //Will parse to Response object


        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
        return null;
    }

    private void handshake(InetAddress address, int port) throws IOException{

        Packet synPck = new Packet.PacketBuilder()
                .setType((byte)1)
                .setSeqNumber(0)
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

    private boolean fullyAck(Packet[] packets) {

        for (Packet p : packets) {
            if(p.getType() != PckType.ACK.value) {
                //System.out.println("P:" + p.getSeqNumber() + " NACK");
                return false;
            }
        }
        return true;
    }

}