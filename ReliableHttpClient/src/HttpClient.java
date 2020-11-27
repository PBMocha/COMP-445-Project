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
    
    private boolean fullyAck(Packet[] packets) {

        for (Packet p : packets) {
            //System.out.println("type "+ p.getType());
        	if(p.getType() != PckType.ACK.value) {
                return false;
            }
        }
        return true;
    }

    public class Countdown { //when activated tracks time delay until having to resend linked packet
        Timer timer;
        
        public Countdown(DatagramPacket  p, int[] inTransit, int sec) {
            timer = new Timer();
            timer.schedule(new RemindTask(p, inTransit, sec), sec*1000);
        }
        
        public void cancelTimer() {
        	//System.out.println("Shutting timer");
        	timer.cancel();
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

    public Packet sendto(Request request, short port) {
    	
        try {
            InetAddress address = InetAddress.getByName(request.getHost());
            clientSocket = new DatagramSocket();

            byte[] requestByte = request.toString().getBytes();
            System.out.println("test request byte length: " + requestByte.length);

            handshake(address, port);

            
            Packet[] packetList = null;
            Packet p = null;
            int numPackets = 0;

            if (requestByte.length > Packet.MAX_PAYLOAD) {

                numPackets = requestByte.length / Packet.MAX_PAYLOAD+1;

                byte[][] allPackets = new byte[numPackets][Packet.MAX_PAYLOAD];

                for (int i=0; i<numPackets; i++) {
                    allPackets[i] = Arrays.copyOfRange(requestByte, i*Packet.MAX_PAYLOAD, i*Packet.MAX_PAYLOAD+Packet.MAX_PAYLOAD); //do if else statement here in case final bit is less than 1013?
                }

                packetList = new Packet[numPackets];
                for (int i=0; i<numPackets; i++) {
                    packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).setType(PckType.DATA.value).build();
                }
            }
            else {
                p = new Packet.PacketBuilder().setPayload(requestByte).setPeerAddress(address).setPeerPort(port).setSeqNumber(0).setType(PckType.DATA.value).build();
                //System.out.println(p);

            }

        	Countdown[] packetTimer = new Countdown[numPackets];
            int counter = (int)(packetList[packetList.length - 1].getSeqNumber()/2)-1; //counter to keep track of what is being sent
            DatagramPacket dgp;
            int[] inTransit = new int[packetList.length];
            int window = (int)packetList[packetList.length - 1].getSeqNumber()/2; //naturally rounded down due to being an int


            if (p == null){                

                //send initial bundle
                for (int i=0; i<window; i++) {
                    //byte[] bytes = packetList[i].toBytes();
                    //packetList[i].toBuffer().get(bytes);
                    inTransit[i] = i;
                    dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, address, port);
                    clientSocket.send(dgp); //check that packet contains info once sent
                    packetTimer[i] = new Countdown(dgp, inTransit, 10); //timer + resend mechanism when timer expires
                }                                
            }
            else { //dealing with uno packet
            	dgp = new DatagramPacket(p.toBytes(), p.toBytes().length, address, port);
                clientSocket.send(dgp); //check that packet contains info once sent
                packetTimer[0] = new Countdown(dgp, inTransit, 10); //timer + resend mechanism when timer expires
                
                Packet[] tempList = {p};
                do {
                	//wait for ACKS
                    byte[] buf = new byte[Packet.MAX_BYTES];
                    DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                    clientSocket.receive(receivedPacket);

                    Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
                    
                    if (receivedStuff.getType() == PckType.DATA.value) //requested data
                        System.out.print("Received requested packet: " + receivedStuff.toString());
                    
                    else if (receivedStuff.getType() == PckType.ACK.value) { //received ACK
                        if (p.getSeqNumber() == receivedStuff.getSeqNumber()) { //if ACK is valid
                        	
                        	p.setType(PckType.ACK.value);
                        	packetTimer[0].cancelTimer();                                               	                                                	                            
                        }
                        
                        else 
                            System.out.println("Unrecognized ACK has been received, possible duplicate.");
                    
                    }
                    
                    else if (receivedStuff.getType() == PckType.NACK.value) { //received NACK
                        if (p.getSeqNumber() == receivedStuff.getSeqNumber()) //if NACK is valid
                            p.setType(PckType.NACK.value);                    
                        else 
                            System.out.println("Unrecognized NACK has been received, possible duplicate.");
                    }
                    else
                        System.out.println("Unidentifiable packet received, please ask server to check type.");
                    
                
            	} while(fullyAck(tempList));
                
                closeConnection();
                System.exit(0);
            }  
            
            //System.out.println("Packets length " + packetList.length);
            //System.out.println("inTransit length " + inTransit.length);
            
            do {           	
            	
                //wait for ACKS
                byte[] buf = new byte[Packet.MAX_BYTES];
                DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
                clientSocket.receive(receivedPacket);

                Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
                
                if (receivedStuff.getType() == PckType.DATA.value) { //requested data
                    System.out.print("Received requested packet: " + receivedStuff.toString());
                }
                else {
                    if (receivedStuff.getType() == PckType.ACK.value) { //received ACK
                        if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if ACK is valid
                        	
                        	packetList[receivedStuff.getSeqNumber()].setType(PckType.ACK.value);
                        	packetTimer[receivedStuff.getSeqNumber()].cancelTimer();
                        	
                        	if ((counter+1 < packetList.length)) { //check if there are other packets to send
                            	//System.out.println("COUNTER: " + counter + "SEQUENCE NUMBER: " + receivedStuff.getSeqNumber());
                                
                            	counter++;
                        		inTransit[receivedStuff.getSeqNumber()] = counter;
                                dgp = new DatagramPacket(packetList[counter].toBytes(), packetList[counter].toBytes().length, address, port);
                                clientSocket.send(dgp);
                                packetTimer[counter] = new Countdown(dgp, inTransit, 10);                                                                        
                            }                                                 	                            
                        }
                        
                        else {
                            System.out.println("Unrecognized ACK has been received, possible duplicate.");
                        }
                    }
                    else if (receivedStuff.getType() == PckType.NACK.value) { //received NACK
                        if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if NACK is valid
                            packetList[receivedStuff.getSeqNumber()].setType(PckType.NACK.value);
                        	dgp = new DatagramPacket(packetList[receivedStuff.getSeqNumber()].toBytes(), packetList[receivedStuff.getSeqNumber()].toBytes().length, address, port);
                            clientSocket.send(dgp);
                            packetTimer[receivedStuff.getSeqNumber()] = new Countdown(dgp, inTransit, 10);
                        }
                        else {
                            System.out.println("Unrecognized NACK has been received, possible duplicate.");
                        }
                    }
                    else {
                        System.out.println("Unidentifiable packet received, please ask server to check type.");
                    }
                }
            } while(!fullyAck(packetList));

    } catch (IOException e)
    {
        e.printStackTrace();
    } finally {
        System.out.println("Closing connection");        
    	closeConnection();
    }
    return null;
    }

    private void handshake(InetAddress address, int port) throws IOException{

        Packet synPck = new Packet.PacketBuilder()
                .setType(PckType.SYN_ACK.value)
                .setSeqNumber(0)
                .setPeerAddress(address)
                .setPeerPort((short)port)
                .setPayload(new byte[Packet.MAX_PAYLOAD])
                .build();

        DatagramPacket dgPck = new DatagramPacket(synPck.toBytes(), synPck.toBytes().length, address, port);

        //1: Send SYN
        System.out.println("sending (1): " + synPck.toString());
        clientSocket.send(dgPck);

        //2: Receive SYN+ACK
        clientSocket.receive(dgPck);
        synPck = Packet.fromBytes(ByteBuffer.wrap(dgPck.getData()));
        System.out.println("received (2): " + synPck.toString());

        synPck.setType(PckType.ACK.value);
        dgPck.setData(synPck.toBytes());
        //3: Send ACK
        clientSocket.send(dgPck);
        System.out.println("sending (3): " + synPck.toString());

    }

}