package helpers;

import core.Request;

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

    public static Request buildHttpRequest(BufferedReader reader) throws IOException {

        String line = reader.readLine();
        String[] requestLine = line.split(" ");

        Request request = new Request(requestLine[0], requestLine[1], requestLine[2]);

        //Parse Headers
        line = reader.readLine();
        while (!line.isEmpty()) {
            String[] headerContent = line.split(":", 2);
            request.addHeader(headerContent[0].trim(), headerContent[1].trim());
            line = reader.readLine();
        }

        if (request.getHeader("Content-Length") != null) {
            int contentLength = Integer.parseInt(request.getHeader("Content-Length"));
            char[] bodyBytes = new char[contentLength];

            reader.read(bodyBytes);
            request.setBody(String.valueOf(bodyBytes));
        }

        //Checks for body

        return request;
    }


}
