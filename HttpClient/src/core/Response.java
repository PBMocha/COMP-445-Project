package core;

import java.util.Scanner;
import java.io.*;
import java.util.HashMap;

public class Response {

    private String statusCode;
    private HashMap<String, String> headers;
    private String dataRaw;
    private HashMap<String, String> data;
    private BufferedReader input;

    public Response(InputStream in)
    {
        headers = new HashMap<String, String>();
        data = new HashMap<>();
        dataRaw = "";

        parseResponse(in);
    }

    private void parseResponse(InputStream _in)
    {

        try {
            input = new BufferedReader(new InputStreamReader(_in));
            StringBuilder response = new StringBuilder();
            String line;

            statusCode = input.readLine();

            //Store Headers
            while((line = input.readLine()) != null) {

                //End of header content
                if (line.isBlank())
                {
                    break;
                }

                String[] headerPair = line.split(":", 2);

                headers.put(headerPair[0], headerPair[1]);

            }

            //Store Body Data
            while((line = input.readLine()) != null) {
                dataRaw += line;
                dataRaw += System.lineSeparator();
            }


        } catch(IOException e) {

        } finally {
            closeConnection();
        }
    }

    private void closeConnection()
    {
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        stringBuilder.append(statusCode);
        stringBuilder.append(System.lineSeparator());

        for (String key : headers.keySet()) {
            stringBuilder.append(key + ": " + headers.get(key));
            stringBuilder.append(System.lineSeparator());
        }

        stringBuilder.append(dataRaw);

        return stringBuilder.toString();
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public HashMap<String, String> getData() {
        return data;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getHeaderValue(String key)
    {
        return headers.get(key);
    }

    public String getDataRaw()
    {
        return dataRaw;
    }





}
