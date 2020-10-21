import java.io.*;
import java.net.*;

public class TestConnection {



    public static void main(String[] args) {

        String serverName = "127.0.0.1";
        int port = 80;
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);

            System.out.println("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            //DataOutputStream out = new DataOutputStream(new BufferedOutputStream(outToServer));
            OutputStreamWriter out = new OutputStreamWriter(outToServer);

            out.write("Hello from " + client.getLocalSocketAddress() + "\r\nWhats up\r\nlolol\r\n\rn\r\n");
            out.flush();

            InputStream inFromServer = client.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inFromServer));

            System.out.println("Server says " + in.readLine());
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
