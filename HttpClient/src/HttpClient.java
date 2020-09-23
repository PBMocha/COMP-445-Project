import java.io.*;
import java.net.*;
import java.util.HashMap;

public class HttpClient
{
    private Socket clientSocket;
    private OutputStreamWriter out;
    private BufferedReader in;

    public void startHttpConnection(String url)
    {

        try {
            clientSocket = new Socket(url, 80);

            out = new OutputStreamWriter(clientSocket.getOutputStream());

            out.write("GET /status/418 HTTP/1.0\r\nUser-Agent: Hello\r\n\r\n");
            out.flush();
            StringBuilder response = new StringBuilder();
            String line;

            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while((line = in.readLine()) != null) {
                response.append(line);
                response.append(System.lineSeparator());
            }

            System.out.println(response.toString());



        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }

    }

    public void closeConnection()
    {
        try{    
            out.close();
            in.close();
            clientSocket.close();

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void send()
    {

    }

    public void parseHttp()
    {

    }

    public static void main(String[] args)
    {
        HttpClient client = new HttpClient();

        client.startHttpConnection("www.httpbin.org");
    }

}