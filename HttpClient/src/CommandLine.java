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

    public void parse()
    {

        if (this.optionPresent("-v"))
        {
            //Activate Verbose Option
        }

        if (this.optionPresent("-h"))
        {
            //Store all header Key:Value pairs into HashMap
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
