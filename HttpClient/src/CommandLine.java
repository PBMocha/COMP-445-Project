import java.util.ArrayList;
import java.util.HashMap;

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

    public void handleBody()
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



    }


}
