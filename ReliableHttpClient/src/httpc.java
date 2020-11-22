import core.HttpMethod;
import core.Request;
import core.Url;

public class httpc {

    public static void main(String[] args) {
        
//        CommandLine scuffedCurl = new CommandLine(args);
//        scuffedCurl.executeCommand();

        HttpClient client = new HttpClient();

        Request req = (new Request.RequestBuilder(HttpMethod.GET, new Url("127.0.0.1/"))).build();
        client.send(req);
    }
}