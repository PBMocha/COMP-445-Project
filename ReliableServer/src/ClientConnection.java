import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ClientConnection implements Runnable {

    private DatagramPacket packet;
    private DatagramSocket socket;
    private BufferedReader in;
    private int port;

    public ClientConnection(DatagramSocket socket) throws IOException {

        byte[] buffer = new byte[256];
        this.port = port;
        this.socket = socket;
        this.packet = new DatagramPacket(buffer,buffer.length);
    }

    @Override
    public void run() {

        try {
            socket.receive(packet);
            String data = new String(packet.getData(), 0, packet.getLength());
            System.out.println(data);

        } catch (IOException e) {

        }


    }
}
