import core.HttpMethod;
import core.Request;
import core.Response;
import core.Url;

import java.io.IOException;

public class httpc {

    public static void main(String[] args) {

        try {
        CommandLine scuffedCurl = new CommandLine(args);
        scuffedCurl.executeCommand();

//            HttpClient client = new HttpClient();
//
//            Request req = (new Request.RequestBuilder(HttpMethod.GET, new Url("127.0.0.1/")))
//                    .header("Content-Type", "html/text")
//                    .header("Keep-Alive", "Yes pls")
//                    .body(new String((new byte[4000])))
//                    .build();
//            client.send(req, (short) 80);
//            System.out.println("received everything");
//            client.receive();
//            Response response = new Response(client.getInputBuffer());
//
//            System.out.println(response.toString());

        } catch (IOException e) {

        }
    }
}