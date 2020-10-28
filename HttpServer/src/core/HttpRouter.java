package core;

import core.Response;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/*
    Used to handle mapping to a specific http resource in this case files
 */
public class HttpRouter {

    private final File rootDir;
    private HashMap<String, File> routingTable;

    //State root directory to read and write files from.
    public HttpRouter(File rootDirectory) {

        rootDir = rootDirectory;
        routingTable = new HashMap<>();
    }


    public HttpRouter mapResource(String file) {

        File resource = new File(rootDir, file);

        routingTable.put("/" + file, resource);
        return this;
    }

    public HttpRouter mapResource(String location, String file) {

        routingTable.put(location, new File(rootDir, file));
        return this;
    }

    public String getResource(String location) throws IOException{

        BufferedReader reader = new BufferedReader(new FileReader(routingTable.get(location)));

        String line;
        StringBuilder data = new StringBuilder();

        while((line = reader.readLine()) != null) {
            data.append(line);
            //data.append(System.lineSeparator());
        }

        return data.toString();
    }

    //Use this for testing
    public void printRouteTable() {

        routingTable.forEach((String key, File value) -> System.out.println(key + ": " + value.toString()));
    }



}
