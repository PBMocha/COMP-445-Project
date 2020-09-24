package core;

import java.util.Scanner;
import java.io.*;
import java.util.HashMap;

public class Response {

    private HashMap<String, String> headers;
    private HashMap<String, String> data;
    private InputStreamReader input;

    public Response(InputStream in)
    {
        parseResponse(in);
    }

    private void parseResponse(InputStream _in)
    {
        
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(_in));
            StringBuilder response = new StringBuilder();
            String line;

            //Store Headers
            while((line = in.readLine()) != null) {
                System.out.println(line);

                if (line.isBlank())
                {
                    break;
                }
            }

        } catch(IOException e) {

        }
    }

    public String toString()
    {
        return "";
    }

}
