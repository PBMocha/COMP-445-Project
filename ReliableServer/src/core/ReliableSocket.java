package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReliableSocket {

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


    private DatagramSocket socket;

    /**
     * Datagram socket wrapper for reliable data transfer using
     * Selective repeat protocol
     * @param socket
     */
    public ReliableSocket(DatagramSocket socket) {
        this.socket = socket;
    }

    //Uses Selective Repeat protocol to receive packets
    //
    public void receive() throws IOException {
        byte[] buf = new byte[Packet.MAX_BYTES];
        DatagramPacket dg = new DatagramPacket(buf, buf.length);

        socket.receive(dg);

        handshake(dg);

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
            socket.receive(dg);
            Packet packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            int recSeqNum = packet.getSeqNumber();
            System.out.println("received pkt: " + packet.toString());

            //Send NACK if packet was not received correctly


            //If packet was already ack by receiver, resend ack
            //Send back ACK
            packet.setType(PckType.ACK.value);
            dg.setData(packet.toBytes());
            socket.send(dg);
            seqN.set(recSeqNum, true);
            System.out.println("sent pkt: " + packet.toString());
            if (!buffer.stream().anyMatch( pck -> pck.getSeqNumber() == recSeqNum)) {
                buffer.add(packet);
            }

            if (seqN.get(recSeqNum) && winEnd < seqN.size()) {
                //Shift window if curSeqNumber is oldest packet
                int i = recSeqNum;
                //Keep moving window to next unreceived packet
                while (seqN.get(i) && winEnd != seqN.size()) {
                    winBeg = i++;
                    winEnd = winBeg + windowSize;
                }
                System.out.println("New Window: " + winBeg + ", " + winEnd);
            }

        }
        buffer.forEach(x -> System.out.print(x.toString() + "payload: " + x.getPayload().length));

    }


    public void send(String message) {

        HashMap<Integer, Packet> packets = partitionPacket(message);

    }

    private HashMap<Integer, Packet> partitionPacket(String msg) {

        HashMap<Integer, Packet> packets = new HashMap<>();



        return packets;
    }

    // 3-Handshake
    // After establishing connection, return initial seq number for first packet
    private void handshake(DatagramPacket dg) {

        try {
            //1: SYN from client
            Packet packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            System.out.println("received (1): " + packet.toString());

            //2: SYN+ACK
            packet.setType((byte)2);
            System.out.println("sending (2): " + packet.toString());
            dg.setData(packet.toBytes());
            socket.send(dg);

            //3: ACK from client
            socket.receive(dg);
            packet = Packet.fromBytes(ByteBuffer.wrap(dg.getData()));
            System.out.println("received (3): " + packet.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<Boolean> createSeqNFrame(int size) {

        ArrayList<Boolean> frame = new ArrayList<>(size);
        for (int cnt = 0; cnt < size; cnt++) {
            frame.add(false);
        }

        return frame;
    }




}