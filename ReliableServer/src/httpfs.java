import cmd.CommandLine;

import java.io.IOException;

public class httpfs {


    public static void main(String[] args) {

        CommandLine cmd = new CommandLine(args, false);
        cmd.createBoolean("-v");
        cmd.createOption("-p", "80");
        cmd.createOption("-d", "./");
        cmd.parseArgs();

        try {
            int portNumber = Integer.parseInt(cmd.getOption("-p"));

            if (portNumber > 65535 || portNumber < 0) {

                System.out.println("Port number is not between [0,65535]");
                System.exit(-1);
            }

            HttpServer server = new HttpServer(portNumber, cmd.getOption("-d"), cmd.getBoolFlag("-v"));
            server.start();

        } catch(NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Invalid port number format");
            System.exit(-1);
        } catch(IOException e) {
            e.printStackTrace();
        }

//        try {
//            HttpServer server = new HttpServer(80, "./docs/", false);
//            server.start();
//        } catch(IOException e) {
//
//        }


    }
}
