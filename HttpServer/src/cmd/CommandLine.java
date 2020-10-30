package cmd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

public class CommandLine {

    private String[] args;

    private HashMap<String, Boolean> boolOption;
    private HashMap<String, String> options;
    private HashSet<String> commands;

    private boolean targetExist;
    private String target;

    public CommandLine(String[] args, boolean useTarget) {
        this.args = args;
        this.target = "";
        this.options = new HashMap<>();
        this.boolOption = new HashMap<>();
        this.targetExist = useTarget;
    }

    public void createBoolean(String option) {
        boolOption.put(option, false);
    }

    public Boolean getBoolFlag(String flag) {
        return boolOption.get(flag);
    }

    public void createOption(String option) {
        options.put(option, null);
    }

    public void createOption(String option, String defaultValue) {
        options.put(option, defaultValue);
    }

    public String getOption(String option) {
        return options.get(option);
    }

    public boolean optionValueExist(String option) {
        return options.get(option).isEmpty();
    }


    public void parseArgs() {

        //Extract Target
        if (targetExist) {
            target = this.args[this.args.length - 1];
        }
        //Extract boolean options
        for (String flag : boolOption.keySet()) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(flag)) {
                    //Set option value
                    boolOption.put(flag,Boolean.TRUE);
                    break;
                }
            }
        }

        //Extract option having singular values
        for (String option : options.keySet()) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(option)) {
                    options.put(option, args[i+1]);
                    break;
                }
            }
        }
    }

}
