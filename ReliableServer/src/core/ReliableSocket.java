package core;

import org.omg.CORBA.TIMEOUT;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class ReliableSocket {

    private static int RESEND_TIMEOUT = 1;
    private InetAddress router;
    private int routerPort;

    private DatagramSocket socket;
    private BufferedReader inputBuffer;

    //For connected client
    private InetAddress clientAddress;
    private int clientPort;

    private enum PckType{
        NACK((byte)0),
        ACK((byte)1),
        SENT((byte)2),
        SYN((byte)3),
        SYN_ACK((byte)4),
        FIN((byte)5),
        FIN_ACK((byte)6),
        DATA((byte)7),
        ED_BUF((byte)8);;

        private byte value;

        private PckType (byte value) {
            this.value = value;
        }
        private byte raw() {
            return  value;
        }
    }



    /**
     * Datagram socket wrapper for reliable data transfer using
     * Selective repeat protocol
     * @param socket
     */
    public ReliableSocket(DatagramSocket socket) throws IOException{
        this.socket = socket;
        this.router = InetAddress.getByName("127.0.0.1");
        this.routerPort = (short)3000;
    }

    //Uses Selective Repeat protocol to receive packets
    //
    public void receive() throws IOException {
        byte[] buf = new byte[Packet.MAX_BYTES];
        DatagramPacket dg = new DatagramPacket(buf, buf.length, router, routerPort);

        //socket.receive(dg);

        //handshake(dg);

        //ArrayList for managing seq numbers and window frame
        //True if packet with seq number i is received
        List<Boolean> seqN = createSeqNFrame(10);
        int[] dupCnt = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        boolean recDup = false;
        List<Packet> buffer = new ArrayList<>(); // just to store the packets in here

        //Window frame represented using index pointers: interval = [winBeg, winEnd)
        int windowSize = (int)Math.floor(seqN.size() / 2);
        int winBeg = 0;
        int winEnd = winBeg + windowSize;
        //Keep receiving packets until all packets have been ACK
        while(true) {

            //Receive packet
            Packet packet = null;
            int recSeqNum;
            try {
                socket.setSoTimeout(2000);
                socket.receive(dg);
                packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
                recSeqNum = packet.getSeqNumber();
                if (seqN.get(packet.getSeqNumber())) {
                    dupCnt[packet.getSeqNumber()]++;
                    packet.setType(PckType.ACK.value);
                    dg.setData(packet.toBytes());
                    socket.send(dg);
                    recDup = true;
                    continue;
                }
                //System.out.println("received pkt: " + packet.toString());
                //System.out.println("payload-contents: " + new String(packet.getPayload()));
            }
            catch (SocketTimeoutException e) {
                break;
            }
            catch (IOException e) {

                //Send back NACK if something goes wrong
                Packet nack = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
                nack.setType(PckType.NACK.value);
                dg.setData(nack.toBytes());
                socket.send(dg);
                continue;
            }
            //Send NACK if packet was not received correctly

            //If packet was already ack by receiver, resend ack
            //Send back ACK
            packet.setType(PckType.ACK.value);
            dg.setData(packet.toBytes());
            socket.send(dg);
            seqN.set(recSeqNum, true);
            //System.out.println("sent pkt: " + packet.toString());
            if (!buffer.stream().anyMatch( pck -> pck.getSeqNumber() == recSeqNum)) {
                buffer.add(packet);
            }

            if (seqN.get(recSeqNum) && winEnd < seqN.size()) {
                //Shift window if curSeqNumber is oldest packet
                int i = recSeqNum;
                //Keep moving window to next unreceived packet
                while (seqN.get(i) && winEnd < seqN.size()) {
                    winBeg = i++;
                    winEnd = winBeg + windowSize;
                }
                //System.out.println("New Window: " + winBeg + ", " + winEnd);
            }

        }

        //If received duplicate ACKS, accept more PACKETS for a bit
        if (recDup) {

        }


        //Sort packets by sequence number sequence number
        buffer.sort(Comparator.comparingInt(Packet::getSeqNumber));

        StringBuilder stringBuilder = new StringBuilder();

        for (Packet p : buffer) {

            if (p.getPayload().length > 0) {
                stringBuilder.append(new String(p.getPayload()));
            }
        }
        //System.out.println("FROM BYTES:\n" + stringBuilder.toString());

        StringReader strReader = new StringReader(stringBuilder.toString());
        inputBuffer = new BufferedReader(strReader);
    }


    public void send(String message) throws IOException{

//        InetAddress router = InetAddress.getByName("127.0.0.1");
//        short routerPort = 3000;

        try {
            InetAddress address = InetAddress.getByName("127.0.0.1");
            //socket = new DatagramSocket();

            byte[] messageBytes = message.getBytes();
            System.out.println("request byte length: " + messageBytes.length);

            Packet[] packetList = new Packet[10];

            //Breaks down the request if its too large for 1 packet
            if (messageBytes.length > Packet.MAX_PAYLOAD) {
                //System.out.println("Breaking down request...");

                //Get total number of partitions for the request
                int numPackets = messageBytes.length / Packet.MAX_PAYLOAD + 1;
                //System.out.println("" + numPackets);

                byte[][] allPackets = new byte[numPackets][Packet.MAX_PAYLOAD];

                for (int i=0; i<numPackets; i++) {
                    allPackets[i] = Arrays.copyOfRange(messageBytes, i*Packet.MAX_PAYLOAD, i*Packet.MAX_PAYLOAD+Packet.MAX_PAYLOAD); //do if else statement here in case final bit is less than 1013?
                }

                //packetList = new Packet[numPackets];
                for (int i=0; i<numPackets; i++) {
                    //packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).build();
                    packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(clientPort).setSeqNumber(i).setType(PckType.DATA.value).build();
                }

                //Fill the rest of the packets in the buffer
                for (int i=numPackets; i < packetList.length; i++) {
                    packetList[i] = new Packet.PacketBuilder().setPayload("".getBytes()).setPeerAddress(address).setPeerPort(clientPort).setSeqNumber(i).setType(PckType.DATA.value).build();
                }
            }
            else {
                //Store the packet in the first position of the buffer
                packetList[0] = new Packet.PacketBuilder().setPayload(messageBytes).setPeerAddress(address).setPeerPort(clientPort).setSeqNumber(0).setType(PckType.DATA.value).build();
                //System.out.println(new String(packetList[0].getPayload()));
                //Fill it with packets
                for(int i = 1; i < packetList.length; i++) {
                    packetList[i] = new Packet.PacketBuilder()
                            .setPeerAddress(address)
                            .setPeerPort(clientPort)
                            .setSeqNumber(i)
                            .setType(PckType.DATA.value)
                            .setPayload("".getBytes())
                            .build();
                    //System.out.println(new String(packetList[i].getPayload()));
                }

            }

            for (Packet p : packetList) {
                System.out.println(p.toString());
            }

            //int counter = (int)(packetList[packetList.length - 1].getSeqNumber()/2)-1; //counter to keep track of what is being sent
            DatagramPacket dgp;

            int winSize = packetList.length / 2;
            int winBase = 0;
            int winEnd = winBase + winSize;


            HashMap<Integer, Countdown> packetTimers = new HashMap<>();

            //Send first batch of packets
            for (int i=winBase; i<winEnd; i++) {

                if (packetList[i].getType() != PckType.SENT.value){
                    packetList[i].setType(PckType.SENT.value);
                    dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, router, routerPort);
                    System.out.println("sending: " + packetList[i].toString());
                    socket.send(dgp); //check that packet contains info once sent

                    //Set timer to resend packet in 2 seconds
                    packetTimers.put(packetList[i].getSeqNumber(), new Countdown(packetList[i], RESEND_TIMEOUT));
                }
            }

            //Keep looping until all packets are ACK; Or if something bad happens
            while (!fullyAck(packetList)) {

                //if (iter>30) break;
                //System.out.println("Iter: " + (iter++));
                //Send packet if not already sent

                //wait for ACKS
                byte[] buf = new byte[Packet.MAX_BYTES];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                socket.receive(receivedPacket);
                Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
                //System.out.println("received: " + receivedStuff.toString());

                //Process ACK packet
                if (receivedStuff.getType() == PckType.ACK.value) { //received ACK

                    //Cancel this packets timer
                    packetTimers.get(receivedStuff.getSeqNumber()).timer.cancel();
                    packetTimers.remove(receivedStuff.getSeqNumber());

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
                                dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, router, routerPort);
                                System.out.println("sending: " + packetList[i].toString());
                                socket.send(dgp); //check that packet contains info once sent

                                //new Countdown(dgp, 30); //timer + resend mechanism when timer expires
                                packetTimers.put(packetList[i].getSeqNumber(), new Countdown(packetList[i], RESEND_TIMEOUT));
                            }
                        }
                    }
                }
                else if (receivedStuff.getType() == 2) { //received NACK
                    dgp = new DatagramPacket(packetList[receivedStuff.getSeqNumber()].toBytes(), packetList[receivedStuff.getSeqNumber()].toBytes().length, router, routerPort);
                    socket.send(dgp);
                    new Countdown(packetList[receivedStuff.getSeqNumber()], 30);
                }
                else {
                    System.out.println("Unidentifiable packet received");
                }
                if (fullyAck(packetList)) {


                }
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

    }

    public BufferedReader getInputBuffer() {
        return this.inputBuffer;
    }


    // 3-Handshake
    // After establishing connection, return initial seq number for first packet
    public boolean handshake() {

        //System.out.println("Establishing Handshake...");
        byte[] buf = new byte[Packet.MAX_BYTES];
        DatagramPacket dg = new DatagramPacket(buf, buf.length, router, routerPort);
        try {

            //1: SYN from client
            socket.receive(dg);
            Packet packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            System.out.println("received (1): " + packet.toString());

            //2: SYN+ACK
            packet.setType(PckType.SYN_ACK.value);
            System.out.println("sending (2): " + packet.toString());
            dg.setData(packet.toBytes());
            socket.send(dg);
            Countdown synAckTO = new Countdown(packet, RESEND_TIMEOUT, 3);

            //3: ACK from client
            socket.setSoTimeout(2*1000);
            socket.receive(dg);
            synAckTO.timer.cancel();
            packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            System.out.println("received (3): " + packet.toString());

            //Set current client to be served
            clientAddress = packet.getPeerAddress();
            clientPort = packet.getPeerPort();

            //Not sure if we are supposed to do this lol
            //socket.connect(clientAddress, clientPort);

        } catch (SocketTimeoutException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        System.out.println("CONNECTED WITH: " + clientAddress + ":" + clientPort);
        return true;
    }

    public void disconnectClient() throws IOException{

        byte[] buf = new byte[Packet.MAX_BYTES];
        DatagramPacket dcDg = new DatagramPacket(buf, buf.length, router, routerPort);

        socket.receive(dcDg);
        Packet fin = Packet.fromBytes(ByteBuffer.wrap(dcDg.getData()));

        fin.setType(PckType.FIN_ACK.value);
        dcDg.setData(fin.toBytes());
        socket.send(dcDg);

        socket.receive(dcDg);
        System.out.println("Disconnected: " + clientAddress+":"+clientPort);

        clientAddress = null;
        clientPort = -1;
    }

    private ArrayList<Boolean> createSeqNFrame(int size) {

        ArrayList<Boolean> frame = new ArrayList<>(size);
        for (int cnt = 0; cnt < size; cnt++) {
            frame.add(false);
        }

        return frame;
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

    public class Countdown { //when activated tracks time delay until having to resend linked packet
        Timer timer;
        int maxResend;
        int resendCounter = 0;

        //No max resends
        public Countdown(Packet  p, int sec) {
            timer = new Timer();
            timer.schedule(new RemindTask(p, sec), sec*1000);
            resendCounter = -1;
            maxResend = 1;
        }

        //Max resends
        public Countdown(Packet  p, int sec, int maxResend) {
            timer = new Timer();
            timer.schedule(new RemindTask(p, sec), sec*1000);
            this.maxResend = maxResend;
        }


        private class RemindTask extends TimerTask {
            Packet p;
            int sec;

            public RemindTask(Packet p, int sec) {
                this.p = p;
                this.sec = sec;

            }
            public void run() {

                //timer.cancel();
                try {
                    if (resendCounter < maxResend) {
                        DatagramPacket dg = new DatagramPacket(p.toBytes(), p.toBytes().length, router, routerPort);
                        socket.send(dg);
                        System.out.println("resending: " + p.toString());
                        timer.schedule(new RemindTask(p, sec), sec * 1000);
                        if (resendCounter != -1) {
                            resendCounter++;
                        }
                    } else {

                    }
                } catch (IOException e) {
                    System.out.println("Timer was not given a valid packet");
                    e.printStackTrace();
                }
            }
        }
    }

}
