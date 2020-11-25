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

    public void closeConnection()
    {

        clientSocket.close();
    }
    
    public class Countdown { //when activated tracks time delay until having to resend linked packet
    	Timer timer;
    	
    	public Countdown(DatagramPacket  p, int sec) {
    		timer = new Timer();
    		timer.schedule(new RemindTask(p, sec), sec*1000);
    	}
    	
    	private class RemindTask extends TimerTask {
    		DatagramPacket p;
    		int sec;
    		
    		public RemindTask(DatagramPacket p, int sec) {
    			this.p = p;
    			this.sec = sec;
    		}
			public void run() {
    			timer.cancel();
    			try {
					clientSocket.send(p);
					timer.schedule(new RemindTask(p, sec), sec*1000);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Timer was not given a valid packet");
					e.printStackTrace();
				}
    			
    		}
    	}
    }
    
    public Packet sendto(Request request, short port) {

    	try {
            InetAddress address = InetAddress.getByName(request.getHost());
            clientSocket = new DatagramSocket();
            
            byte[] requestByte = request.toString().getBytes();
            
            handshake(address, port);
            
            byte[] messagebody = request.getMessageBody().getBytes();
            Packet[] packetList = null;
            Packet p = null;
            

        	
        	if (messagebody.length > 1013) {
        		
        		int numPackets = messagebody.length / 1013;
        		System.out.println(messagebody);
        		
        		byte[][] allPackets = new byte[numPackets][1013];
        		
        		for (int i=0; i<numPackets; i++) {
        			allPackets[i] = Arrays.copyOfRange(messagebody, i*1013, i*1013+1013); //do if else statement here in case final bit is less than 1013?
        		}
        		
        		packetList = new Packet[numPackets];
        		for (int i=0; i<numPackets; i++) {
        			//packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).build();
        			packetList[i] = new Packet.PacketBuilder().setPayload(allPackets[i]).setPeerAddress(address).setPeerPort(port).setSeqNumber(i).setType((byte)0).build();
        		}
        	}
        	else {
            	//p = new Packet.PacketBuilder().setPayload(messagebody).setPeerAddress(address).setPeerPort(port).setSeqNumber(0).build();
        		p = new Packet.PacketBuilder().setPayload(messagebody).setPeerAddress(address).setPeerPort(port).setSeqNumber(0).setType((byte)0).build();
        		System.out.println(p);

        	}
        	
        	int counter = (int)(packetList[packetList.length - 1].getSeqNumber()/2)-1; //counter to keep track of what is being sent
        	DatagramPacket dgp;
        	
            while (true) {
            	
            	if (p == null){
            		int window = (int)packetList[packetList.length - 1].getSeqNumber()/2; //naturally rounded down due to being an int
            		int[] inTransit = new int[window];
            		
            		//send initial bundle
            		for (int i=0; i<window; i++) {
            			//byte[] bytes = packetList[i].toBytes();
            			//packetList[i].toBuffer().get(bytes);
            			inTransit[i] = i;
            			dgp = new DatagramPacket(packetList[i].toBytes(), packetList[i].toBytes().length, address, port); 
            			clientSocket.send(dgp); //check that packet contains info once sent
            			new Countdown(dgp, 30); //timer + resend mechanism when timer expires            			
            		}
            		
            		//wait for ACKS
            		byte[] buf = new byte[1024];
            		DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
            		clientSocket.receive(receivedPacket);
            		
            		Packet receivedStuff = Packet.fromBytes(ByteBuffer.wrap(receivedPacket.getData()));
            		if (receivedStuff.getType() == 0) { //requested data
            			System.out.print("Received requested packet: " + receivedStuff.toString());
            		}
            		else {
            			if (receivedStuff.getType() == 1) { //received ACK
            				if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if ACK is valid
            					inTransit[receivedStuff.getSeqNumber()] = counter+1;
            					counter++;
            					dgp = new DatagramPacket(packetList[counter].toBytes(), packetList[counter].toBytes().length, address, port); 
                    			clientSocket.send(dgp);
                    			new Countdown(dgp, 30);          					
            				}
            				else {
            					System.out.println("Unrecognized ACK has been received, possible duplicate.");
            				}
            			}
            			else if (receivedStuff.getType() == 2) { //received NACK
            				if (IntStream.of(inTransit).anyMatch(x -> x == receivedStuff.getSeqNumber())) { //if NACK is valid
            					dgp = new DatagramPacket(packetList[receivedStuff.getSeqNumber()].toBytes(), packetList[receivedStuff.getSeqNumber()].toBytes().length, address, port); 
                    			clientSocket.send(dgp);
                    			new Countdown(dgp, 30);          					
            				}
            				else {
            					System.out.println("Unrecognized NACK has been received, possible duplicate.");
            				}
            			}
            			else {
            				System.out.println("Unidentifiable packet received, please ask server to check type.");
            			}
            		}
            		String data = new String(receivedPacket.getData(),0, receivedPacket.getLength());
            		
            
            		
            	}
            	
            	
            	
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


        } catch (IOException e)
        {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    	return null;
    }

    /**
     * Send a custom request
     * @param request
     * @return Response
     */
    public Response send(Request request)
    {        
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

        //2: Receive SYN+ACK
        clientSocket.receive(dgPck);
        synPck = Packet.fromBytes(ByteBuffer.wrap(dgPck.getData()));
        System.out.println("received (2): " + synPck.toString());

        synPck.setType((byte)3);
        dgPck.setData(synPck.toBytes());
        //3: Send ACK
        clientSocket.send(dgPck);
        System.out.println("sending (3): " + synPck.toString());

    }
    
    public static void main(String[] args) throws IOException {
    	
    }

}