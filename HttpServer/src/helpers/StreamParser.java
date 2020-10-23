package helpers;

import core.Request2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

public class StreamParser {

    public static String readStream(BufferedReader reader) throws IOException {
        //StringBuffer stringBuilder = new StringBuffer();
        StringBuilder stringBuilder = new StringBuilder();

        String line = reader.readLine();

        while (!line.isEmpty()) {
            stringBuilder.append(line);
            stringBuilder.append(System.lineSeparator());
            line = reader.readLine();
        }

        //Checks for body

        return stringBuilder.toString();
    }

    public static Request2 buildHttpRequest(BufferedReader reader) throws IOException {

       	String line = reader.readLine();
       	
       	System.out.println("All" + line);
       	String[] requestLine = line.split(" ");
       	/*if (requestLine[1].contains(" ")) {
           	String[] path = requestLine[1].split(" ");
           	System.out.println(path[0]);
       	}*/
       	for(int i=0; i <requestLine.length; i++) {
       		System.out.println("Test" + requestLine[i]);
       	}

        Request2 request = new Request2(requestLine[0], requestLine[1].replaceFirst("/",""), requestLine[2]);

        //System.out.println("test"+ request.getHeader("Content-Length"));


        //Parse Headers
        line = reader.readLine();
        while (!line.isEmpty()) {
        	System.out.println(line);
            String[] headerContent = line.split(":", 2);
            //System.out.println(headerContent);
            request.addHeader(headerContent[0].trim(), headerContent[1].trim());
            line = reader.readLine();
        }

        if (request.getHeader("Content-Length") != null) {
            int contentLength = Integer.parseInt(request.getHeader("Content-Length"));
            char[] bodyBytes = new char[contentLength];

            reader.read(bodyBytes);
            request.setBody(String.valueOf(bodyBytes));
            System.out.println(request.getBody());
        }

        //Checks for body
        //if (request.g)

        return request;
    }


}
