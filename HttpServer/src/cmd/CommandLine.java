package cmd;

import java.util.HashMap;
import java.util.HashSet;

public class CommandLine {

    private String[] args;
    private HashMap<String, String> options;
    private HashSet<String> commands;

    public CommandLine(String[] args) {
        this.args = args;
    }

    private void parseArguments(String[] args, String[] commands, String[] options) {

    }

    public boolean isOptionPresent(String option) {

        for (int i = 0; i < args.length; i++) {
            if (option.equals(args[i])) {
                return true;
            }
        }

        return false;
    }

    public String optionValue(String option) {




        return null;

    }

}
