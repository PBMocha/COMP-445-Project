import java.util.ArrayList;
import java.util.HashMap;

import core.HttpMethod;
import core.Request;
import core.Url;

public class CommandLine {

    private String[] args;

    private HashMap<String, String> headers;

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
     * Gets value of existing option eg. cmd -v 123 will return 123 if -v is the input
     * @param option
     * @return String value
     */
    public String optionValue(String option) {
        if (!optionPresent(option)) {
            return null;
        }

        for (int i = 0; i < args.length; i++) {
            if (option.equals(args[i])) {
                return args[i+1];
            }
        }

        return null;

    }

    public void handleBody(Request request)
    {

    }

    //Find and store header values from args
    public boolean handleHeaders()
    {
        HashMap<String, String> headers = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-h")) {

                String[] headerParts = args[i+1].split(":");

                //Need to validate header parts input -> check headerParts is length == 2
                if (headerParts.length != 2) return false;

                headers.put(headerParts[0], headerParts[1]);
            }
        }

        return true;
    }

    public String url()
    {
        return args[args.length - 1];
    }

    public void handleRequest()
    {

        if (this.optionPresent("-h"))
        {
            //Store all header Key:Value pairs into HashMap
            handleHeaders();
        }

        if (this.optionPresent("-d"))
        {
            //Handle inline data
        }

        if (this.optionPresent("-f"))
        {
            //Handle file stuff
        }

    }

    public void executeCommand()
    {
        if (args[0].equals("help")) {
            helpMe();
            return;
        }

        HttpClient client = new HttpClient();
        Request request = new Request.RequestBuilder(HttpMethod.GET, new Url(this.url())).build();

        if (args[0].equals("get")) {

        } else if (args[0].equals("post")) {

        }
    }

    public void helpMe() {
        System.out.println("Help me please");
    }


}
