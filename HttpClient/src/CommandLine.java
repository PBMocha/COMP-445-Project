import java.io.*;
import java.util.HashMap;

import core.HttpMethod;
import core.Request;
import core.Response;
import core.Url;

public class CommandLine {

    private String[] args;

    private boolean isVerbose;

    public CommandLine(String[] args) {

        this.args = args;

    }

    /**
     * Checks for option presence in arguments
     * @param option
     * @return boolean: true if option is present in list of arguments
     */
    public boolean optionPresent(String option)
    {
        for (String str : args)
        {
            if (option.equals(str))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets "Singular" value of existing option eg. cmd -v 123 will return 123 if -v is the input
     * Only works if option can only have a 1 value. DO NOT USE WITH HEADER OPTION
     * @param option
     * @return String value, otherwise null if option does not exist
     */
    public String optionValue(String option) {

        for (int i = 0; i < args.length; i++) {
            if (option.equals(args[i])) {
                return args[i+1];
            }
        }

        return null;

    }

    //Search, validate and store header values into request from argument array
    public void handleHeaders(Request request) throws IllegalArgumentException
    {

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {

                String[] headerParts = args[i+1].split(":");

                //Need to validate header parts input -> check headerParts is length == 2
                if (headerParts.length != 2)
                    throw new IllegalArgumentException("Invalid header format: Index [" + (i+1) + "] " + args[i+1]);

                request.addHeader(headerParts[0], headerParts[1]);
            }
        }
    }

    public String url()
    {
        return args[args.length-1];
    }

    public void handleRequest(Request request)
    {

        if (this.optionPresent("-h"))
        {
            //Store all header Key:Value pairs into HashMap
            try {
                handleHeaders(request);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }

        if (this.optionPresent("-d"))
        {
            //Get inline data from args
            request.setMessageBody(optionValue("-d"));
        }

        if (this.optionPresent("-f"))
        {
            //Get inline data from file
            try {
                BufferedReader fileReader = new BufferedReader(new FileReader(new File(optionValue("-f"))));

                StringBuilder contents = new StringBuilder();
                String line;

                while((line = fileReader.readLine()) != null) {
                    contents.append(line);
                    if (line != null)
                    {
                        contents.append(System.lineSeparator());
                    }
                }

                fileReader.close();
                request.setMessageBody(contents.toString());

            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    //The root of the program | the Highest layer
    public void executeCommand()
    {
        if (args.length < 1) {
            System.out.println("Invalid number of arguments");
            return;
        }

        if (args[0].equals("help")) {
            helpMe();
            return;
        }

        Url url = new Url(url());
        HttpClient client = new HttpClient();
        Request request = new Request.RequestBuilder(HttpMethod.GET, url).header("User-Agent", "httpc/1.0").build();

        if (args[0].equals("get")) {

            if (optionPresent("-d") || optionPresent("-f")) {
                System.out.println("Cannot have content body for HTTP GET request.");
                //System.exit(1);
                return;
            }
        }
        else if (args[0].equals("post")) {
            request.setMethod(HttpMethod.POST);
        }

        //Inject argument values into request
        handleRequest(request);

        //Send request
        Response response = client.send(request);

        if (optionPresent("-v")) {
            System.out.println(response);
            return;
        }

        System.out.println(response.getDataRaw());

    }

    public void helpMe() {

        //If only help
        if (args.length == 1) {
            System.out.println("httpc is a curl-like application but supports HTTP protocol only.\n" +
                    "Usage:\n\thttpc command [arguments]\nThe commands are:\n\n" +
                    "\tget\texecutes a HTTP GET request and prints the response.\n" +
                    "\tpost\texecutes a HTTP POST request and prints the response.\n" +
                    "\thelp\tprints this screen.\n\n" +
                    "Use \"httpc help [command]\" for more information about a command.\n");
            return;
        }

        if (args[1].equals("get")) {
            System.out.println("Usage: httpc get [-v] [-h key:value] URL\n" +
                    "Get executes a HTTP GET request for a given URL.\n" +
                    "\t-v\tPrints the detail of the response such as protocol, status, and headers.\n\n" +
                    "\t-h key:value\t Associates headers to HTTP Request with the format 'key:value'.");
        }
        else if (args[1].equals("post")) {
            System.out.println("Usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL\n" +
                    "Post executes a HTTP POST request for a given URL with inline data of from file.\n\n" +
                    "\t-v\tPrints the detail of the response such as protocol, status, and headers.\n" +
                    "\t-h key:value\t Associates headers to HTTP Request with the format 'key:value'.\n" +
                    "\t-d string\tAssociates an inline data to the body HTTP POST request.\n" +
                    "\t-f file\tAssociates the content of a file to the body HTTP POST request.\n\n" +
                    "Either [-d] or [-f] can be used but not both.");
        }


    }


    public static void main(String[] args) {

        for (String arg : args) {
            System.out.println(arg);
        }

        System.out.println();

        CommandLine scuffedCurl = new CommandLine(args);
        scuffedCurl.executeCommand();
    }

}
