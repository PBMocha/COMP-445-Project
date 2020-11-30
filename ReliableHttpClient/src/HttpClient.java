import core.Request;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class HttpClient
{
    private DatagramSocket clientSocket;
    private static int RESEND_TIMEOUT = 1;
    private BufferedReader inputBuffer;

    private InetAddress router;
    private short routerPort;

    private InetAddress serverAddress;
    private int serverPort;



    private enum PckType{
        NACK((byte)0),
        ACK((byte)1),
        SENT((byte)2),
        SYN((byte)3),
        SYN_ACK((byte)4),
        FIN((byte)5),
        FIN_SYN((byte)6),
        DATA((byte)7),
        ED_BUF((byte)8);

        private byte value;

        private PckType (byte value) {
            this.value = value;
        }
        private byte raw() {
            return  value;
        }
    }

    public HttpClient() throws IOException{
        router = InetAddress.getByName("127.0.0.1");
        routerPort = 3000;
    }

    public class Countdown { //when activated tracks time delay until having to resend linked packet
        Timer timer;

        public Countdown(Packet  p, int sec) {
            timer = new Timer();
            timer.schedule(new RemindTask(p, sec), sec*1000);
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
                    InetAddress router = InetAddress.getByName("127.0.0.1");
                    short routerPort = 3000;
                    DatagramPacket dg = new DatagramPacket(p.toBytes(), p.toBytes().length, router, routerPort);
                    clientSocket.send(dg);
                    System.out.println("resending: " + p.toString());
                    timer.schedule(new RemindTask(p, sec), sec*1000);

                } catch (IOException e) {
                    System.out.println("Timer was not given a valid packet");
                    e.printStackTrace();
                }
            }
        }
    }

    public Packet send(Request request, int port) throws IOException {

        try {
            InetAddress address = InetAddress.getByName(request.getHost());
            clientSocket = new DatagramSocket(6969);

            byte[] requestByte = request.toString().getBytes();
            System.out.println("request byte length: " + requestByte.length);

            handshake(address, port);

            Packet[] packetList = new Packet[10];

            //Breaks down the request if its too large for 1 packet
            if (requestByte.length > Packet.MAX_PAYLOAD) {
                System.out.println("Breaking down request...");

                //Get total number of partitions for the request
                int numPackets = requestByte.length / Packet.MAX_PAYLOAD + 1;
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
                    clientSocket.send(dgp); //check that packet contains info once sent

                    //Set timer to resend packet in 2 seconds
                    packetTimers.put(packetList[i].getSeqNumber(), new Countdown(packetList[i], RESEND_TIMEOUT));
                }
            }

            //Keep looping until all packets are ACK; Or if something bad happens
            //Send a finalized ACK to tell server that everything in buffer is ACK
            while (!fullyAck(packetList)) {

                //wait for ACKS
                byte[] buf = new byte[Packet.MAX_BYTES];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(receivedPacket);
                Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
                System.out.println("received: " + receivedStuff.toString());

                //Process ACK packet
                if (receivedStuff.getType() == PckType.ACK.value) { //received ACK

                    //Cancel this packets timer
                    packetTimers.get(receivedStuff.getSeqNumber()).timer.cancel();
                    packetTimers.remove(receivedStuff.getSeqNumber());

                    packetList[receivedStuff.getSeqNumber()].setType(PckType.ACK.value); //Mark packet with ACK

                    //System.out.println("received ACK for: " + receivedStuff.toString());
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
                                clientSocket.send(dgp); //check that packet contains info once sent

                                //new Countdown(dgp, 30); //timer + resend mechanism when timer expires
                                packetTimers.put(packetList[i].getSeqNumber(), new Countdown(packetList[i], RESEND_TIMEOUT));
                            }
                        }
                    }
                }
                else if (receivedStuff.getType() == 2) { //received NACK
//                  if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if NACK is valid
                    dgp = new DatagramPacket(packetList[receivedStuff.getSeqNumber()].toBytes(), packetList[receivedStuff.getSeqNumber()].toBytes().length, router, routerPort);
                    clientSocket.send(dgp);
                    new Countdown(packetList[receivedStuff.getSeqNumber()], RESEND_TIMEOUT);
                }
                else {
                    System.out.println("Unidentifiable packet received, please ask server to check type.");
                }

                if (fullyAck(packetList)) {


                }
            }
            //System.out.println("Finished Sending all packets");


        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            //closeConnection();
        }
        return null;
    }

    public void receive() throws IOException {
        System.out.println("Now Receiving Packets");
        byte[] buf = new byte[Packet.MAX_BYTES];
        DatagramPacket dg = new DatagramPacket(buf, buf.length);

        //clientSocket.receive(dg);

        //throwhands(dg);

        //ArrayList for managing seq numbers and window frame
        //True if packet with seq number i is received
        List<Boolean> seqN = createSeqNFrame(10);
        List<Packet> buffer = new ArrayList<>(); // just to store the packets in here

        //Window frame represented using index pointers: interval = [winBeg, winEnd)
        int windowSize = (int)Math.floor(seqN.size() / 2);
        int winBeg = 0;
        int winEnd = winBeg + windowSize;
        //Keep receiving packets until all packets have been ACK
        while(seqN.contains(false)) {

            //Receive packet
            Packet packet = null;
            int recSeqNum;
            try {
                clientSocket.receive(dg);
                packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
                recSeqNum = packet.getSeqNumber();
                if (seqN.get(packet.getSeqNumber())) {
                    packet.setType(PckType.ACK.value);
                    dg.setData(packet.toBytes());
                    clientSocket.send(dg);
                    continue;
                }
                System.out.println("received pkt: " + packet.toString());
                System.out.println("payload-contents: " + new String(packet.getPayload()));
            } catch (IOException e) {

                //Send back NACK if something goes wrong
                Packet nack = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
                nack.setType(PckType.NACK.value);
                dg.setData(nack.toBytes());
                clientSocket.send(dg);
                continue;
            }
            //Send NACK if packet was not received correctly

            //If packet was already ack by receiver, resend ack
            //Send back ACK
            packet.setType(PckType.ACK.value);
            dg.setData(packet.toBytes());
            clientSocket.send(dg);
            seqN.set(recSeqNum, true);
            System.out.println("sent pkt: " + packet.toString());
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
                System.out.println("New Window: " + winBeg + ", " + winEnd);
            }

        }
        //buffer.forEach(x -> System.out.println("payload-content: " + new String(x.getPayload())));

        //Sort packets by sequence number sequence number
        buffer.sort(Comparator.comparingInt(Packet::getSeqNumber));
        //inputBuffer = buffer;

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

    public BufferedReader getInputBuffer() {
        return inputBuffer;
    }

    public void handshake(InetAddress address, int port) throws IOException{

        boolean isConnected = false;
        Packet synPck = new Packet.PacketBuilder()
                .setType(PckType.SYN.value)
                .setSeqNumber(0)
                .setPeerAddress(address)
                .setPeerPort((short)port)
                .setPayload(new byte[Packet.MAX_PAYLOAD])
                .build();

        DatagramPacket dgPck = new DatagramPacket(synPck.toBytes(), synPck.toBytes().length, router, routerPort);

        //1: Send SYN
        System.out.println("sending (1): " + synPck.toString());
        clientSocket.send(dgPck);
        //Resend SYN
        Countdown resend = new Countdown(synPck, RESEND_TIMEOUT);

        //2: Recieve SYN+ACK
        clientSocket.receive(dgPck);
        resend.timer.cancel();

        synPck = Packet.fromBytes(ByteBuffer.wrap(dgPck.getData()));
        System.out.println("received (2): " + synPck.toString());

        serverAddress = synPck.getPeerAddress();
        serverPort = synPck.getPeerPort();

        synPck.setType(PckType.ACK.value);
        dgPck.setData(synPck.toBytes());
//        //3: Send ACK
//        clientSocket.send(dgPck);
//        System.out.println("sending (3): " + synPck.toString());
//        System.out.println("Connection established :3");

    }

    public void disconnect() throws IOException{


        Packet fin = (new Packet.PacketBuilder())
                .setType(PckType.FIN.value)
                .setPeerAddress(serverAddress)
                .setPeerPort(serverPort)
                .setPayload("".getBytes())
                .build();
        DatagramPacket dg = new DatagramPacket(fin.toBytes(), fin.toBytes().length, router, routerPort);

        clientSocket.send(dg);

        clientSocket.receive(dg);
        Packet fin_syn = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));

        fin_syn.setType(PckType.ACK.value);
        dg.setData(fin_syn.toBytes());
        clientSocket.send(dg);
        System.out.println("Disconnected: " + serverAddress + ":" + serverPort);

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

    private ArrayList<Boolean> createSeqNFrame(int size) {

        ArrayList<Boolean> frame = new ArrayList<>(size);
        for (int cnt = 0; cnt < size; cnt++) {
            frame.add(false);
        }

        return frame;
    }

}